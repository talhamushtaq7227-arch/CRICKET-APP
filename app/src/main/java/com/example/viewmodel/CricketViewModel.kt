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
import com.example.domain.engine.PlayerDetailedStatistics
import com.example.domain.engine.PlayerStatisticsCalculator
import com.example.domain.engine.PlayerStats
import com.example.video.ScoreboardVideoExporter
import com.example.video.VideoExportState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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

    // Temporary scoring selections
    val selectedStrikerName = MutableStateFlow("")
    val selectedNonStrikerName = MutableStateFlow("")
    val selectedBowlerName = MutableStateFlow("")

    // Event flow for snacks / dialogs
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Cricket-specific animation events (Boundaries 4/6, Wicket, Milestone)
    private val _cricketAnimationEvent = MutableSharedFlow<com.example.ui.components.CricketAnimationType>(extraBufferCapacity = 2)
    val cricketAnimationEvent: SharedFlow<com.example.ui.components.CricketAnimationType> = _cricketAnimationEvent.asSharedFlow()

    fun triggerCricketAnimation(event: com.example.ui.components.CricketAnimationType) {
        _cricketAnimationEvent.tryEmit(event)
    }

    init {
        // Observe deliveries for current match
        viewModelScope.launch {
            _currentMatch.collectLatest { match ->
                if (match != null) {
                    repository.getDeliveriesForMatch(match.id).collectLatest { deliveries ->
                        _currentMatchDeliveries.value = deliveries
                        recalculateLiveState(match, deliveries)
                    }
                } else {
                    _currentMatchDeliveries.value = emptyList()
                    _inningsLiveState.value = InningsLiveState()
                }
            }
        }

        // Auto-update match sourceVideoPath when camera recording is finalized
        viewModelScope.launch {
            cameraManager.recordingStatus.collect { status ->
                if (status is com.example.camera.RecordingStatus.Completed) {
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
            val match = repository.getMatchById(matchId)
            _currentMatch.value = match
        }
    }

    private fun recalculateLiveState(match: MatchEntity, deliveries: List<BallDeliveryEntity>) {
        val state = CricketRulesEngine.calculateInningsState(
            match = match,
            inningsNumber = match.currentInnings,
            deliveries = deliveries,
            initialStrikerName = selectedStrikerName.value.ifEmpty { null },
            initialNonStrikerName = selectedNonStrikerName.value.ifEmpty { null },
            initialBowlerName = selectedBowlerName.value.ifEmpty { null }
        )
        _inningsLiveState.value = state

        // Update selected names from live state if not manually set
        state.currentStriker?.let { selectedStrikerName.value = it.playerName }
        state.currentNonStriker?.let { selectedNonStrikerName.value = it.playerName }
        state.currentBowler?.let { selectedBowlerName.value = it.bowlerName }

        // Auto update match completion status if finished
        if (state.isMatchComplete && !match.isCompleted) {
            viewModelScope.launch {
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
            dismissedPlayerName = if (isWicket) (dismissedPlayerName ?: striker) else null,
            fielderPlayerName = fielderPlayerName,
            videoTimestampMs = if (videoTimeMs > 0) videoTimeMs else null,
            commentary = commentary
        )

        viewModelScope.launch {
            repository.insertDelivery(delivery)

            // If wicket and incoming batter provided, update active striker
            if (isWicket && !newIncomingBatterName.isNullOrBlank()) {
                val dismissed = dismissedPlayerName ?: striker
                if (dismissed == striker) {
                    selectedStrikerName.value = newIncomingBatterName
                } else {
                    selectedNonStrikerName.value = newIncomingBatterName
                }
            }

            // Update match timestamp
            repository.updateMatch(match.copy(updatedAt = System.currentTimeMillis()))

            // Trigger visual cricket celebration animations
            if (runsOffBat == 4) {
                triggerCricketAnimation(com.example.ui.components.CricketAnimationType.Four)
            } else if (runsOffBat == 6) {
                triggerCricketAnimation(com.example.ui.components.CricketAnimationType.Six)
            } else if (isWicket) {
                val desc = if (!dismissedPlayerName.isNullOrBlank()) "$dismissedPlayerName OUT!" else "WICKET!"
                triggerCricketAnimation(com.example.ui.components.CricketAnimationType.Wicket(desc))
            }
        }
    }

    fun undoLastBall() {
        val match = _currentMatch.value ?: return
        viewModelScope.launch {
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
            repository.updateMatch(updated)
            _currentMatch.value = updated
            selectedStrikerName.value = ""
            selectedNonStrikerName.value = ""
            selectedBowlerName.value = ""
            _userMessage.emit("Innings 1 Complete. Target: $target")
        }
    }

    fun endMatchManually() {
        val match = _currentMatch.value ?: return
        val state = _inningsLiveState.value
        viewModelScope.launch {
            val updated = match.copy(
                isCompleted = true,
                winnerTeam = state.resultSummary ?: "Match Ended",
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
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
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
            loadMatch(id)
            onCreated(id)
        }
    }

    fun insertPlayer(player: PlayerEntity) {
        viewModelScope.launch {
            repository.insertPlayer(player)
            _userMessage.emit("Player ${player.fullName} added to squad")
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
        viewModelScope.launch {
            val player = PlayerEntity(
                fullName = name.trim(),
                jerseyNumber = jersey,
                role = role,
                battingHand = battingHand,
                bowlingStyle = bowlingStyle,
                notes = notes?.trim()
            )
            repository.insertPlayer(player)
            _userMessage.emit("Player $name added to squad")
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
                    _userMessage.emit("Welcome, ${user.displayName ?: "Cricket Fan"}!")
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
                val result = cloudRepository.restoreDataFromCloud()
                if (result.isSuccess) {
                    onComplete(true, "Successfully connected to Cloud Firestore (${result.getOrNull()} cloud records available).")
                } else {
                    onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Restore failed")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Failed to restore data")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.release()
    }
}

