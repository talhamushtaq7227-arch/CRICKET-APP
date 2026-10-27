package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.camera.CameraManager
import com.example.camera.RecordingStatus
import com.example.data.local.AppDatabase
import com.example.data.model.BallDeliveryEntity
import com.example.data.model.BattingHand
import com.example.data.model.BowlingStyle
import com.example.data.model.ExtrasType
import com.example.data.model.MatchEntity
import com.example.data.model.MatchFormat
import com.example.data.model.OverlayStyle
import com.example.data.model.PlayerEntity
import com.example.data.model.PlayerRole
import com.example.data.model.TossDecision
import com.example.data.model.WicketType
import com.example.data.repository.CloudCricketRepository
import com.example.data.repository.CricketRepository
import com.example.data.repository.PreferencesRepository
import com.example.domain.engine.CricketRulesEngine
import com.example.domain.engine.InningsLiveState
import com.example.domain.engine.PlayerStats
import com.example.video.ScoreboardVideoExporter
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CricketViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = CricketRepository(db.playerDao(), db.matchDao(), db.ballDeliveryDao())
    val preferencesRepository = PreferencesRepository(application)
    val cameraManager = CameraManager(application)
    val videoExporter = ScoreboardVideoExporter(application)
    val authManager = AuthManager(application)
    val cloudRepository = CloudCricketRepository(application)

    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser
    val isAuthenticating: StateFlow<Boolean> = authManager.isAuthenticating
    val authStatusMessage: StateFlow<String?> = authManager.authStatusMessage

    // Data streams
    val allPlayers: StateFlow<List<PlayerEntity>> = repository.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlayers: StateFlow<List<PlayerEntity>> = repository.getActivePlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<MatchEntity>> = repository.getAllMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeliveries: StateFlow<List<BallDeliveryEntity>> = repository.getAllDeliveries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctSeasons: StateFlow<List<String>> = repository.getDistinctSeasons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("2026"))

    val distinctTournaments: StateFlow<List<String>> = repository.getDistinctTournaments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestUnfinishedMatch: StateFlow<MatchEntity?> = repository.getLatestUnfinishedMatch()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val featuredMatchStates: StateFlow<Pair<InningsLiveState?, InningsLiveState?>> = combine(
        latestUnfinishedMatch,
        allMatches
    ) { unfinished, all ->
        unfinished ?: all.firstOrNull()
    }
        .distinctUntilChanged()
        .flatMapLatest { featured ->
            if (featured == null) {
                flowOf(null to null)
            } else {
                repository.getDeliveriesForMatch(featured.id).map { matchBalls ->
                    val inn1 = CricketRulesEngine.calculateInningsState(featured, 1, matchBalls)
                    val inn2 = CricketRulesEngine.calculateInningsState(featured, 2, matchBalls)
                    inn1 to inn2
                }
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null to null)

    val rankings: StateFlow<List<PlayerStats>> = repository.getPlayerRankingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teamNameSetting: StateFlow<String> = preferencesRepository.teamNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Rajpoot Cricket Boys")

    val overlayStyleSetting: StateFlow<OverlayStyle> = preferencesRepository.overlayStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverlayStyle.LOWER_THIRD)

    val overlayScaleSetting: StateFlow<Float> = preferencesRepository.overlayScaleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val muteAudioSetting: StateFlow<Boolean> = preferencesRepository.muteAudioFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val glassThemeSetting: StateFlow<com.example.data.repository.GlassThemeMode> = preferencesRepository.glassThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.repository.GlassThemeMode.DARK)

    // Active Match & Live Scoring State
    private val _currentMatch = MutableStateFlow<MatchEntity?>(null)
    val currentMatch: StateFlow<MatchEntity?> = _currentMatch.asStateFlow()

    private val _currentMatchDeliveries = MutableStateFlow<List<BallDeliveryEntity>>(emptyList())
    val currentMatchDeliveries: StateFlow<List<BallDeliveryEntity>> = _currentMatchDeliveries.asStateFlow()

    private val _inningsLiveState = MutableStateFlow<InningsLiveState>(InningsLiveState())
    val inningsLiveState: StateFlow<InningsLiveState> = _inningsLiveState.asStateFlow()

    // Active scoring player selections
    val selectedStrikerName = MutableStateFlow("")
    val selectedNonStrikerName = MutableStateFlow("")
    val selectedBowlerName = MutableStateFlow("")
    private var bowlerSelectedForNextOver = false

    // Event flow for snacks / dialogs
    private val _userMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Cricket-specific animation events (Boundaries 4/6, Wicket, Milestone)
    private val _cricketAnimationEvent = MutableSharedFlow<com.example.ui.components.CricketAnimationType>(extraBufferCapacity = 2)
    val cricketAnimationEvent: SharedFlow<com.example.ui.components.CricketAnimationType> = _cricketAnimationEvent.asSharedFlow()

    fun triggerCricketAnimation(event: com.example.ui.components.CricketAnimationType) {
        _cricketAnimationEvent.tryEmit(event)
    }

    init {
        viewModelScope.launch {
            _currentMatch
                .map { it?.id }
                .distinctUntilChanged()
                .collectLatest { matchId ->
                    if (matchId != null) {
                        repository.getDeliveriesForMatch(matchId).collectLatest { deliveries ->
                            _currentMatchDeliveries.value = deliveries
                            val match = _currentMatch.value
                            if (match != null && match.id == matchId) {
                                recalculateLiveState(match, deliveries)
                            }
                        }
                    } else {
                        _currentMatchDeliveries.value = emptyList()
                        _inningsLiveState.value = InningsLiveState()
                    }
                }
        }

        viewModelScope.launch {
            cameraManager.recordingStatus.collect { status ->
                if (status is RecordingStatus.Completed) {
                    val match = _currentMatch.value
                    if (match != null) {
                        val updated = match.copy(sourceVideoPath = status.file.absolutePath)
                        repository.updateMatch(updated)
                        _currentMatch.value = updated
                    }
                }
            }
        }
    }

    fun loadMatch(matchId: Long) {
        viewModelScope.launch {
            val current = _currentMatch.value
            val match = repository.getMatchById(matchId)
            if (match != null && current?.id != match.id) {
                selectedStrikerName.value = ""
                selectedNonStrikerName.value = ""
                selectedBowlerName.value = ""
                bowlerSelectedForNextOver = false
            }
            _currentMatch.value = match
        }
    }

    /**
     * Ensures a player name entered during live scoring or setup exists in the squad database
     * so no duplicate or orphaned players occur.
     */
    private suspend fun ensurePlayerInSquad(name: String, defaultRole: PlayerRole = PlayerRole.ALL_ROUNDER) {
        val cleanName = name.trim()
        if (cleanName.isEmpty() ||
            cleanName.equals("Striker", ignoreCase = true) ||
            cleanName.equals("Non-striker", ignoreCase = true) ||
            cleanName.equals("Batter 1", ignoreCase = true) ||
            cleanName.equals("Batter 2", ignoreCase = true) ||
            cleanName.equals("Bowler 1", ignoreCase = true) ||
            cleanName.equals("New Batter", ignoreCase = true)
        ) {
            return
        }
        val exists = allPlayers.value.any { it.fullName.equals(cleanName, ignoreCase = true) }
        if (!exists) {
            val nextJersey = (allPlayers.value.maxOfOrNull { it.jerseyNumber } ?: 0) + 1
            repository.insertPlayer(
                PlayerEntity(
                    fullName = cleanName,
                    jerseyNumber = nextJersey,
                    role = defaultRole,
                    battingHand = BattingHand.RIGHT_HAND,
                    bowlingStyle = BowlingStyle.RIGHT_ARM_MEDIUM
                )
            )
        }
    }

    /**
     * Immediately updates the active bowler for the current/next over and recalculates live state
     * so the BowlerSelectionDialog closes right away and never loops.
     */
    fun selectBowler(bowlerName: String) {
        val cleanBowler = bowlerName.trim()
        if (cleanBowler.isEmpty()) return

        viewModelScope.launch {
            ensurePlayerInSquad(cleanBowler, PlayerRole.BOWLER)
            selectedBowlerName.value = cleanBowler
            bowlerSelectedForNextOver = true
            val match = _currentMatch.value
            if (match != null) {
                recalculateLiveState(match, _currentMatchDeliveries.value)
            }
            _userMessage.emit("Active bowler set to $cleanBowler")
        }
    }

    /**
     * Updates active striker and non-striker during a match, preventing duplicate player selection
     * and dismissal conflicts, and immediately updates the live scoreboard.
     */
    fun selectBatters(strikerName: String, nonStrikerName: String) {
        val cleanStriker = strikerName.trim()
        val cleanNonStriker = nonStrikerName.trim()

        if (cleanStriker.isEmpty() || cleanNonStriker.isEmpty()) {
            viewModelScope.launch {
                _userMessage.emit("Both Striker and Non-Striker must be selected.")
            }
            return
        }

        if (cleanStriker.equals(cleanNonStriker, ignoreCase = true)) {
            viewModelScope.launch {
                _userMessage.emit("Striker and Non-Striker cannot be the same player.")
            }
            return
        }

        val dismissedNames = _inningsLiveState.value.battingCard
            .filter { it.isOut }
            .map { it.playerName.lowercase() }
            .toSet()

        if (dismissedNames.contains(cleanStriker.lowercase()) || dismissedNames.contains(cleanNonStriker.lowercase())) {
            viewModelScope.launch {
                _userMessage.emit("Cannot select a batsman who is already out in this innings.")
            }
            return
        }

        viewModelScope.launch {
            ensurePlayerInSquad(cleanStriker, PlayerRole.BATTER)
            ensurePlayerInSquad(cleanNonStriker, PlayerRole.BATTER)
            selectedStrikerName.value = cleanStriker
            selectedNonStrikerName.value = cleanNonStriker
            val match = _currentMatch.value
            if (match != null) {
                recalculateLiveState(match, _currentMatchDeliveries.value)
            }
            _userMessage.emit("Batting pair updated: $cleanStriker* & $cleanNonStriker")
        }
    }

    /**
     * Manually swaps striker and non-striker ends.
     */
    fun swapStrikeManually() {
        val state = _inningsLiveState.value
        val s = selectedStrikerName.value.ifEmpty { state.currentStriker?.playerName.orEmpty() }
        val ns = selectedNonStrikerName.value.ifEmpty { state.currentNonStriker?.playerName.orEmpty() }
        if (s.isNotEmpty() && ns.isNotEmpty() && !s.equals(ns, ignoreCase = true)) {
            selectedStrikerName.value = ns
            selectedNonStrikerName.value = s
            val match = _currentMatch.value
            if (match != null) {
                viewModelScope.launch {
                    recalculateLiveState(match, _currentMatchDeliveries.value)
                    _userMessage.emit("Strike rotated: $ns is now on strike")
                }
            }
        }
    }

    private suspend fun recalculateLiveState(match: MatchEntity, deliveries: List<BallDeliveryEntity>) {
        // Default initial players from squad if no balls have been bowled and none selected yet
        val inningsDeliveries = deliveries.filter { it.inningsNumber == match.currentInnings }
        if (inningsDeliveries.isEmpty()) {
            val squadPlayers = allPlayers.value
            if (selectedStrikerName.value.isEmpty() && squadPlayers.isNotEmpty()) {
                selectedStrikerName.value = squadPlayers[0].fullName
            }
            if (selectedNonStrikerName.value.isEmpty() && squadPlayers.size >= 2) {
                val second = squadPlayers.firstOrNull { !it.fullName.equals(selectedStrikerName.value, ignoreCase = true) }
                if (second != null) selectedNonStrikerName.value = second.fullName
            }
            if (selectedBowlerName.value.isEmpty() && squadPlayers.size >= 3) {
                val bowlerCandidate = squadPlayers.lastOrNull {
                    !it.fullName.equals(selectedStrikerName.value, ignoreCase = true) &&
                        !it.fullName.equals(selectedNonStrikerName.value, ignoreCase = true)
                }
                if (bowlerCandidate != null) selectedBowlerName.value = bowlerCandidate.fullName
            }
        }

        val state = withContext(Dispatchers.Default) {
            CricketRulesEngine.calculateInningsState(
                match = match,
                inningsNumber = match.currentInnings,
                deliveries = deliveries,
                initialStrikerName = selectedStrikerName.value.ifEmpty { null },
                initialNonStrikerName = selectedNonStrikerName.value.ifEmpty { null },
                initialBowlerName = selectedBowlerName.value.ifEmpty { null },
                bowlerSelectedForNextOver = bowlerSelectedForNextOver,
                activeStrikerOverride = selectedStrikerName.value.ifEmpty { null },
                activeNonStrikerOverride = selectedNonStrikerName.value.ifEmpty { null },
                activeBowlerOverride = selectedBowlerName.value.ifEmpty { null }
            )
        }
        _inningsLiveState.value = state

        // Synchronize active names from calculated live state
        state.currentStriker?.playerName?.takeIf { it.isNotEmpty() }?.let {
            selectedStrikerName.value = it
        }
        state.currentNonStriker?.playerName?.takeIf { it.isNotEmpty() }?.let {
            selectedNonStrikerName.value = it
        }
        state.currentBowler?.bowlerName?.takeIf { it.isNotEmpty() }?.let {
            selectedBowlerName.value = it
        }

        if (state.isMatchComplete && !match.isCompleted) {
            val updated = match.copy(
                isCompleted = true,
                winnerTeam = state.resultSummary,
                winMargin = state.resultSummary,
                isDraft = false,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateMatch(updated)
            _currentMatch.value = updated
        }
    }

    // Scoring actions
    fun scoreBall(
        runsOffBat: Int,
        extrasType: ExtrasType = ExtrasType.NONE,
        extrasRuns: Int = 0,
        isWicket: Boolean = false,
        wicketType: WicketType = WicketType.NONE,
        dismissedPlayerName: String? = null,
        fielderPlayerName: String? = null,
        newIncomingBatterName: String? = null
    ) {
        val match = _currentMatch.value ?: return
        val currentDeliveries = _currentMatchDeliveries.value
        val state = _inningsLiveState.value

        val striker = selectedStrikerName.value.ifEmpty { state.currentStriker?.playerName ?: "Batter 1" }
        val nonStriker = selectedNonStrikerName.value.ifEmpty { state.currentNonStriker?.playerName ?: "Batter 2" }
        val bowler = selectedBowlerName.value.ifEmpty { state.currentBowler?.bowlerName ?: "Bowler 1" }

        val isLegal = extrasType != ExtrasType.WIDE && extrasType != ExtrasType.NO_BALL
        val legalBallInOver = if (isLegal) (state.ballsInCurrentOver + 1).coerceAtMost(6) else 0
        val overNum = state.legalBalls / 6
        val totalBallRuns = runsOffBat + extrasRuns
        val videoTimeMs = cameraManager.getRecordingElapsedMs()

        val commentary = buildCommentary(
            bowler = bowler,
            striker = striker,
            runsOffBat = runsOffBat,
            extrasType = extrasType,
            extrasRuns = extrasRuns,
            isWicket = isWicket,
            wicketType = wicketType
        )

        val actualDismissed = if (isWicket) (dismissedPlayerName?.trim()?.ifEmpty { striker } ?: striker) else null

        val delivery = BallDeliveryEntity(
            matchId = match.id,
            inningsNumber = match.currentInnings,
            overNumber = overNum,
            legalBallInOver = legalBallInOver,
            ballSequence = currentDeliveries.size + 1,
            bowlerName = bowler,
            strikerName = striker,
            nonStrikerName = nonStriker,
            runsOffBat = runsOffBat,
            extrasType = extrasType,
            extrasRuns = extrasRuns,
            totalBallRuns = totalBallRuns,
            isLegalDelivery = isLegal,
            isWicket = isWicket,
            wicketType = wicketType,
            dismissedPlayerName = actualDismissed,
            fielderPlayerName = fielderPlayerName?.trim()?.ifEmpty { null },
            videoTimestampMs = if (videoTimeMs > 0) videoTimeMs else null,
            commentary = commentary
        )

        viewModelScope.launch {
            // Compute post-delivery striker & non-striker BEFORE inserting delivery so recalculateLiveState
            // immediately knows the incoming batter and exact strike rotation!
            var postStriker = striker
            var postNonStriker = nonStriker

            if (isWicket) {
                val incoming = newIncomingBatterName?.trim().orEmpty()
                if (incoming.isNotEmpty()) {
                    ensurePlayerInSquad(incoming, PlayerRole.BATTER)
                }
                val replacement = incoming.ifEmpty { "Batter ${state.totalWickets + 3}" }
                if (actualDismissed.equals(postStriker, ignoreCase = true)) {
                    postStriker = replacement
                } else if (actualDismissed.equals(postNonStriker, ignoreCase = true)) {
                    postNonStriker = replacement
                } else {
                    postStriker = replacement
                }
            }

            // Physical runs rotation
            val physicalRuns = when (extrasType) {
                ExtrasType.BYE, ExtrasType.LEG_BYE -> extrasRuns
                ExtrasType.WIDE -> maxOf(0, extrasRuns - 1)
                else -> runsOffBat
            }
            if (physicalRuns % 2 != 0) {
                val tmp = postStriker
                postStriker = postNonStriker
                postNonStriker = tmp
            }

            // End of over rotation (if this legal ball finishes the 6-ball over)
            val willFinishOver = isLegal && ((state.ballsInCurrentOver + 1) >= 6)
            if (willFinishOver) {
                val tmp = postStriker
                postStriker = postNonStriker
                postNonStriker = tmp
            }

            selectedStrikerName.value = postStriker
            selectedNonStrikerName.value = postNonStriker
            bowlerSelectedForNextOver = false

            repository.insertDelivery(delivery)
            if (currentDeliveries.isEmpty()) {
                val updated = match.copy(isDraft = false, updatedAt = System.currentTimeMillis())
                repository.updateMatch(updated)
                _currentMatch.value = updated
            }

            if (runsOffBat == 4) {
                triggerCricketAnimation(com.example.ui.components.CricketAnimationType.Four)
            } else if (runsOffBat == 6) {
                triggerCricketAnimation(com.example.ui.components.CricketAnimationType.Six)
            } else if (isWicket) {
                val desc = if (!actualDismissed.isNullOrBlank()) "$actualDismissed OUT!" else "WICKET!"
                triggerCricketAnimation(com.example.ui.components.CricketAnimationType.Wicket(desc))
            }
        }
    }

    fun undoLastBall() {
        val match = _currentMatch.value ?: return
        viewModelScope.launch {
            selectedStrikerName.value = ""
            selectedNonStrikerName.value = ""
            bowlerSelectedForNextOver = false
            repository.undoLastDelivery(match.id)
            _userMessage.emit("Last ball undone")
        }
    }

    fun switchInnings() {
        val match = _currentMatch.value ?: return
        val state = _inningsLiveState.value
        val target = state.totalRuns + 1

        viewModelScope.launch {
            val updated = match.copy(
                currentInnings = 2,
                targetRuns = target,
                updatedAt = System.currentTimeMillis()
            )
            selectedStrikerName.value = ""
            selectedNonStrikerName.value = ""
            selectedBowlerName.value = ""
            bowlerSelectedForNextOver = false
            repository.updateMatch(updated)
            _currentMatch.value = updated
            recalculateLiveState(updated, _currentMatchDeliveries.value)
            _userMessage.emit("Innings 1 Complete. Target: $target")
        }
    }

    fun endMatchManually() {
        val match = _currentMatch.value ?: return
        val state = _inningsLiveState.value
        viewModelScope.launch {
            val updated = match.copy(
                isCompleted = true,
                winnerTeam = state.resultSummary ?: "Match Completed",
                isDraft = false,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateMatch(updated)
            _currentMatch.value = updated
            _userMessage.emit("Match completed and saved")
        }
    }

    fun createMatch(
        title: String,
        tournament: String,
        season: String = "2026",
        homeTeam: String,
        awayTeam: String,
        venue: String,
        format: MatchFormat,
        overs: Int,
        playersPerSide: Int,
        tossWinner: String,
        tossDecision: TossDecision,
        battingFirst: String,
        selectedSquadIds: List<Long>,
        awayPlayers: List<String>,
        openingStriker: String = "",
        openingNonStriker: String = "",
        openingBowler: String = "",
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            if (openingStriker.isNotBlank()) ensurePlayerInSquad(openingStriker, PlayerRole.BATTER)
            if (openingNonStriker.isNotBlank()) ensurePlayerInSquad(openingNonStriker, PlayerRole.BATTER)
            if (openingBowler.isNotBlank()) ensurePlayerInSquad(openingBowler, PlayerRole.BOWLER)

            selectedStrikerName.value = openingStriker.trim()
            selectedNonStrikerName.value = openingNonStriker.trim()
            selectedBowlerName.value = openingBowler.trim()
            bowlerSelectedForNextOver = false

            val match = MatchEntity(
                matchTitle = title,
                tournamentName = tournament,
                season = season.ifBlank { "2026" },
                homeTeamName = homeTeam.ifEmpty { "Rajpoot Cricket Boys" },
                awayTeamName = awayTeam,
                venue = venue.ifEmpty { "RCB Cricket Ground" },
                format = format,
                totalOvers = overs,
                playersPerSide = playersPerSide,
                tossWinnerTeam = tossWinner,
                tossDecision = tossDecision,
                battingFirstTeam = battingFirst,
                homeSquadPlayerIds = selectedSquadIds.joinToString(","),
                awaySquadPlayerNames = awayPlayers.joinToString(","),
                currentInnings = 1,
                isDraft = true
            )
            val id = repository.insertMatch(match)
            val createdMatch = repository.getMatchById(id)
            _currentMatch.value = createdMatch
            onCreated(id)
        }
    }

    fun insertPlayer(player: PlayerEntity) {
        val cleanName = player.fullName.trim()
        if (cleanName.isEmpty()) return
        if (allPlayers.value.any { it.fullName.equals(cleanName, ignoreCase = true) && it.id != player.id }) {
            viewModelScope.launch {
                _userMessage.emit("Player '$cleanName' is already in the squad.")
            }
            return
        }
        viewModelScope.launch {
            repository.insertPlayer(player.copy(fullName = cleanName))
            _userMessage.emit("Player $cleanName added to squad")
        }
    }

    fun addPlayer(
        name: String,
        jersey: Int,
        role: PlayerRole,
        battingHand: BattingHand,
        bowlingStyle: BowlingStyle,
        notes: String?
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        if (allPlayers.value.any { it.fullName.equals(cleanName, ignoreCase = true) }) {
            viewModelScope.launch {
                _userMessage.emit("Player '$cleanName' already exists in the squad.")
            }
            return
        }
        viewModelScope.launch {
            val player = PlayerEntity(
                fullName = cleanName,
                jerseyNumber = jersey,
                role = role,
                battingHand = battingHand,
                bowlingStyle = bowlingStyle,
                notes = notes?.trim()
            )
            repository.insertPlayer(player)
            _userMessage.emit("Player $cleanName added to squad")
        }
    }

    fun updatePlayer(player: PlayerEntity) {
        viewModelScope.launch {
            repository.updatePlayer(player)
            _userMessage.emit("Player updated")
        }
    }

    fun deletePlayer(id: Long) {
        viewModelScope.launch {
            repository.deletePlayer(id)
            _userMessage.emit("Player removed from squad")
        }
    }

    fun setPlayerCaptain(playerId: Long) {
        viewModelScope.launch {
            val players = allPlayers.value
            players.forEach { p ->
                if (p.id == playerId) {
                    repository.updatePlayer(p.copy(isCaptain = true, isViceCaptain = false))
                } else if (p.isCaptain) {
                    repository.updatePlayer(p.copy(isCaptain = false))
                }
            }
            _userMessage.emit("Captain designated")
        }
    }

    fun setPlayerViceCaptain(playerId: Long) {
        viewModelScope.launch {
            val players = allPlayers.value
            players.forEach { p ->
                if (p.id == playerId) {
                    repository.updatePlayer(p.copy(isViceCaptain = true, isCaptain = false))
                } else if (p.isViceCaptain) {
                    repository.updatePlayer(p.copy(isViceCaptain = false))
                }
            }
            _userMessage.emit("Vice-Captain designated")
        }
    }

    fun updatePlayerPhoto(playerId: Long, photoUri: String?) {
        viewModelScope.launch {
            val player = repository.getPlayerById(playerId) ?: return@launch
            repository.updatePlayer(player.copy(photoUri = photoUri))
            _userMessage.emit(if (photoUri != null) "Player photo updated" else "Photo removed")
        }
    }

    fun setPlayerActive(playerId: Long, isActive: Boolean) {
        viewModelScope.launch {
            repository.setPlayerActive(playerId, isActive)
            _userMessage.emit(if (isActive) "Player marked active" else "Player marked inactive")
        }
    }

    fun setGlassTheme(mode: com.example.data.repository.GlassThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setGlassTheme(mode)
            _userMessage.emit("Theme changed to ${mode.displayName}")
        }
    }

    // Camera & Video Export Integration
    fun startCameraRecording(recordAudio: Boolean = true, onError: (String) -> Unit) {
        val file = cameraManager.startRecording(recordAudio = recordAudio, onError = onError)
        if (file != null) {
            val match = _currentMatch.value
            if (match != null) {
                viewModelScope.launch {
                    repository.updateMatch(match.copy(sourceVideoPath = file.absolutePath))
                }
            }
        }
    }

    fun stopCameraRecording() {
        cameraManager.stopRecording()
    }

    fun exportVideo(sourceFile: File) {
        val match = _currentMatch.value ?: return
        val deliveries = _currentMatchDeliveries.value
        val style = overlayStyleSetting.value
        val scale = overlayScaleSetting.value
        val mute = muteAudioSetting.value

        videoExporter.exportVideoWithScoreboard(
            scope = viewModelScope,
            sourceVideoFile = sourceFile,
            match = match,
            inningsNumber = match.currentInnings,
            allDeliveries = deliveries,
            overlayStyle = style,
            overlayScale = scale,
            muteAudio = mute
        )
    }

    private fun buildCommentary(
        bowler: String,
        striker: String,
        runsOffBat: Int,
        extrasType: ExtrasType,
        extrasRuns: Int,
        isWicket: Boolean,
        wicketType: WicketType
    ): String {
        return when {
            isWicket -> "$bowler to $striker, OUT! ${wicketType.displayName}!"
            runsOffBat == 6 -> "$bowler to $striker, SIX! High, handsome and into the stands!"
            runsOffBat == 4 -> "$bowler to $striker, FOUR! Pierces the gap to the boundary rope!"
            extrasType == ExtrasType.WIDE -> "$bowler to $striker, Wide ball (+$extrasRuns extras)."
            extrasType == ExtrasType.NO_BALL -> "$bowler to $striker, No ball (Free hit coming up)."
            extrasType == ExtrasType.BYE -> "$bowler to $striker, $extrasRuns Bye(s)."
            extrasType == ExtrasType.LEG_BYE -> "$bowler to $striker, $extrasRuns Leg bye(s)."
            runsOffBat == 0 -> "$bowler to $striker, no run, defended solidly."
            runsOffBat == 1 -> "$bowler to $striker, 1 run, pushed into the gap."
            else -> "$bowler to $striker, $runsOffBat runs taken."
        }
    }

    // Google Sign-In & Firebase Cloud Sync
    fun signInWithGoogle(activity: Activity) {
        authManager.signInWithGoogle(
            activity = activity,
            scope = viewModelScope,
            onSuccess = { user ->
                viewModelScope.launch {
                    _userMessage.emit("Welcome, ${user.displayName ?: "Cricket Captain"}!")
                }
            },
            onError = { err ->
                viewModelScope.launch {
                    _userMessage.emit(err)
                }
            }
        )
    }

    fun signOut() {
        authManager.signOut(viewModelScope) {
            viewModelScope.launch {
                _userMessage.emit("Signed out of Google account")
            }
        }
    }

    fun backupToCloud(onComplete: (Boolean, String) -> Unit) {
        val user = currentUser.value
        if (user == null) {
            onComplete(false, "Please sign in with Google first to backup your data.")
            return
        }

        viewModelScope.launch {
            try {
                val players = allPlayers.value
                val matches = allMatches.value
                val result = cloudRepository.backupAllDataToCloud(
                    players = players,
                    matches = matches,
                    fetchDeliveriesForMatch = { matchId ->
                        repository.getDeliveriesForMatchSnapshot(matchId)
                    }
                )
                if (result.isSuccess) {
                    onComplete(true, "Successfully backed up ${result.getOrNull() ?: 0} records to Cloud Firestore!")
                } else {
                    onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Backup failed")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Failed to backup data")
            }
        }
    }

    fun restoreFromCloud(onComplete: (Boolean, String) -> Unit) {
        val user = currentUser.value
        if (user == null) {
            onComplete(false, "Please sign in with Google first to restore your data.")
            return
        }

        viewModelScope.launch {
            try {
                val existingNames = allPlayers.value.map { it.fullName }.toSet()
                val result = cloudRepository.restoreDataFromCloud(
                    localRepository = repository,
                    existingPlayerNames = existingNames
                )
                if (result.isSuccess) {
                    onComplete(true, "Successfully synced ${result.getOrNull() ?: 0} records from Cloud Firestore.")
                } else {
                    onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Restore failed")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Failed to restore data")
            }
        }
    }
}
