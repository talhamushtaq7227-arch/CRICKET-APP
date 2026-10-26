package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.BattingHand
import com.example.data.model.BowlingStyle
import com.example.data.model.ExtrasType
import com.example.data.model.MatchFormat
import com.example.data.model.PlayerRole
import com.example.data.model.TossDecision
import com.example.data.model.WicketType

class Converters {
    @TypeConverter
    fun fromPlayerRole(role: PlayerRole?): String = role?.name ?: PlayerRole.ALL_ROUNDER.name

    @TypeConverter
    fun toPlayerRole(value: String?): PlayerRole =
        value?.let { runCatching { PlayerRole.valueOf(it) }.getOrNull() } ?: PlayerRole.ALL_ROUNDER

    @TypeConverter
    fun fromBattingHand(hand: BattingHand?): String = hand?.name ?: BattingHand.RIGHT_HAND.name

    @TypeConverter
    fun toBattingHand(value: String?): BattingHand =
        value?.let { runCatching { BattingHand.valueOf(it) }.getOrNull() } ?: BattingHand.RIGHT_HAND

    @TypeConverter
    fun fromBowlingStyle(style: BowlingStyle?): String = style?.name ?: BowlingStyle.RIGHT_ARM_MEDIUM.name

    @TypeConverter
    fun toBowlingStyle(value: String?): BowlingStyle =
        value?.let { runCatching { BowlingStyle.valueOf(it) }.getOrNull() } ?: BowlingStyle.RIGHT_ARM_MEDIUM

    @TypeConverter
    fun fromMatchFormat(format: MatchFormat?): String = format?.name ?: MatchFormat.T20.name

    @TypeConverter
    fun toMatchFormat(value: String?): MatchFormat =
        value?.let { runCatching { MatchFormat.valueOf(it) }.getOrNull() } ?: MatchFormat.T20

    @TypeConverter
    fun fromTossDecision(decision: TossDecision?): String = decision?.name ?: TossDecision.BAT.name

    @TypeConverter
    fun toTossDecision(value: String?): TossDecision =
        value?.let { runCatching { TossDecision.valueOf(it) }.getOrNull() } ?: TossDecision.BAT

    @TypeConverter
    fun fromExtrasType(extras: ExtrasType?): String = extras?.name ?: ExtrasType.NONE.name

    @TypeConverter
    fun toExtrasType(value: String?): ExtrasType =
        value?.let { runCatching { ExtrasType.valueOf(it) }.getOrNull() } ?: ExtrasType.NONE

    @TypeConverter
    fun fromWicketType(wicket: WicketType?): String = wicket?.name ?: WicketType.NONE.name

    @TypeConverter
    fun toWicketType(value: String?): WicketType =
        value?.let { runCatching { WicketType.valueOf(it) }.getOrNull() } ?: WicketType.NONE
}
