package com.example.data.model

enum class PlayerRole(val displayName: String) {
    BATTER("Batter"),
    BOWLER("Bowler"),
    ALL_ROUNDER("All-Rounder"),
    WICKET_KEEPER("Wicketkeeper")
}

enum class BattingHand(val displayName: String) {
    RIGHT_HAND("Right Hand"),
    LEFT_HAND("Left Hand")
}

enum class BowlingStyle(val displayName: String) {
    RIGHT_ARM_FAST("Right-arm Fast"),
    RIGHT_ARM_MEDIUM("Right-arm Medium"),
    RIGHT_ARM_SPIN("Right-arm Spin"),
    LEFT_ARM_FAST("Left-arm Fast"),
    LEFT_ARM_MEDIUM("Left-arm Medium"),
    LEFT_ARM_SPIN("Left-arm Spin"),
    NONE("None")
}

enum class MatchFormat(val displayName: String, val defaultOvers: Int) {
    T10("T10 (10 Overs)", 10),
    T20("T20 (20 Overs)", 20),
    OVERS_30("30 Overs", 30),
    OVERS_40("40 Overs", 40),
    OVERS_50("ODI (50 Overs)", 50),
    CUSTOM("Custom Overs", 15)
}

enum class TossDecision(val displayName: String) {
    BAT("Bat First"),
    BOWL("Bowl First")
}

enum class ExtrasType(val code: String, val displayName: String) {
    NONE("", "None"),
    WIDE("wd", "Wide"),
    NO_BALL("nb", "No Ball"),
    BYE("b", "Bye"),
    LEG_BYE("lb", "Leg Bye"),
    PENALTY("pen", "Penalty")
}

enum class WicketType(val displayName: String, val isBowlerWicket: Boolean) {
    NONE("None", false),
    BOWLED("Bowled", true),
    CAUGHT("Caught", true),
    LBW("LBW", true),
    RUN_OUT("Run Out", false),
    STUMPED("Stumped", true),
    HIT_WICKET("Hit Wicket", true),
    RETIRED_HURT("Retired Hurt", false),
    RETIRED_OUT("Retired Out", false),
    TIMED_OUT("Timed Out", false),
    OBSTRUCTING_FIELD("Obstructing Field", false)
}

enum class OverlayStyle(val displayName: String) {
    LOWER_THIRD("Broadcast Lower-Third"),
    WIDE_BANNER("Wide TV Header"),
    COMPACT_CORNER("Compact Mobile Corner")
}
