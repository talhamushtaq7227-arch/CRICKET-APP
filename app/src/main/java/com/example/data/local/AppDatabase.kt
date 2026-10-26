package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BallDeliveryEntity
import com.example.data.model.BattingHand
import com.example.data.model.BowlingStyle
import com.example.data.model.MatchEntity
import com.example.data.model.PlayerEntity
import com.example.data.model.PlayerRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.room.migration.Migration

@Database(
    entities = [PlayerEntity::class, MatchEntity::class, BallDeliveryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao
    abstract fun ballDeliveryDao(): BallDeliveryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE players ADD COLUMN isCaptain INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE players ADD COLUMN isViceCaptain INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE matches ADD COLUMN season TEXT NOT NULL DEFAULT '2026'")
            }
        }

        val INITIAL_ROSTER = listOf(
            PlayerEntity(fullName = "Talha", jerseyNumber = 7, role = PlayerRole.ALL_ROUNDER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.RIGHT_ARM_MEDIUM, isCaptain = true),
            PlayerEntity(fullName = "Faheem", jerseyNumber = 10, role = PlayerRole.BOWLER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.RIGHT_ARM_FAST, isViceCaptain = true),
            PlayerEntity(fullName = "Abrar", jerseyNumber = 17, role = PlayerRole.BATTER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.NONE),
            PlayerEntity(fullName = "Mujtaba", jerseyNumber = 23, role = PlayerRole.ALL_ROUNDER, battingHand = BattingHand.LEFT_HAND, bowlingStyle = BowlingStyle.LEFT_ARM_SPIN),
            PlayerEntity(fullName = "Israr", jerseyNumber = 9, role = PlayerRole.BATTER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.NONE),
            PlayerEntity(fullName = "Mudassir", jerseyNumber = 11, role = PlayerRole.WICKET_KEEPER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.NONE),
            PlayerEntity(fullName = "Sami", jerseyNumber = 8, role = PlayerRole.BOWLER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.RIGHT_ARM_SPIN),
            PlayerEntity(fullName = "Haris", jerseyNumber = 3, role = PlayerRole.BATTER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.NONE),
            PlayerEntity(fullName = "Muhammad", jerseyNumber = 1, role = PlayerRole.ALL_ROUNDER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.RIGHT_ARM_FAST),
            PlayerEntity(fullName = "Khizzar", jerseyNumber = 14, role = PlayerRole.BOWLER, battingHand = BattingHand.LEFT_HAND, bowlingStyle = BowlingStyle.LEFT_ARM_FAST),
            PlayerEntity(fullName = "Noor Ahmad", jerseyNumber = 15, role = PlayerRole.BOWLER, battingHand = BattingHand.LEFT_HAND, bowlingStyle = BowlingStyle.LEFT_ARM_SPIN),
            PlayerEntity(fullName = "Iqbal", jerseyNumber = 22, role = PlayerRole.BATTER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.NONE),
            PlayerEntity(fullName = "Huzaifa", jerseyNumber = 18, role = PlayerRole.BATTER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.NONE),
            PlayerEntity(fullName = "Hanan", jerseyNumber = 5, role = PlayerRole.ALL_ROUNDER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.RIGHT_ARM_MEDIUM),
            PlayerEntity(fullName = "Qeizar", jerseyNumber = 12, role = PlayerRole.BATTER, battingHand = BattingHand.LEFT_HAND, bowlingStyle = BowlingStyle.NONE),
            PlayerEntity(fullName = "Zeeshan", jerseyNumber = 6, role = PlayerRole.BOWLER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.RIGHT_ARM_FAST),
            PlayerEntity(fullName = "Ammar", jerseyNumber = 4, role = PlayerRole.ALL_ROUNDER, battingHand = BattingHand.RIGHT_HAND, bowlingStyle = BowlingStyle.RIGHT_ARM_SPIN)
        )

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rajpoot_cricket_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.playerDao()?.insertPlayers(INITIAL_ROSTER)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
