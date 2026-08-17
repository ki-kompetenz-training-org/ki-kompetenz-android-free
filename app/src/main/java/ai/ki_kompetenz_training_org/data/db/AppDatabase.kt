package ai.ki_kompetenz_training_org.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

// ── Content cache ───────────────────────────────────────────────────────────

@Entity(tableName = "lessons_cache")
data class LessonEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val lessonNumber: Int?,
    val duration: String?,
    val description: String,
    val objectivesJson: String,
    val body: String?,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Int,
    val tierTitle: String,
    val correctCount: Int,
    val totalQuestions: Int,
    val createdAt: Long = System.currentTimeMillis(),
)

// ── Gamification (lokal auf dem Gerät — DSGVO: kein Server-Sync) ────────────

@Entity(tableName = "gamification")
data class GamificationEntity(
    @PrimaryKey val id: Int = 1, // single row
    val xp: Int = 0,
    val streak: Int = 0,
    val lastCheckInDay: String? = null, // yyyy-MM-dd
    val lastCheckInAt: Long? = null,
    val badgesJson: String = "[]", // ["first_score", ...]
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val slug: String,
    val completedAt: Long = System.currentTimeMillis(),
)

@Dao
interface ContentDao {
    @Query("SELECT * FROM lessons_cache ORDER BY lessonNumber ASC")
    fun observeLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons_cache WHERE slug = :slug LIMIT 1")
    suspend fun getLesson(slug: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLesson(lesson: LessonEntity)

    @Query("SELECT COUNT(*) FROM lessons_cache")
    suspend fun count(): Int
}

@Dao
interface QuizResultDao {
    @Insert
    suspend fun insert(result: QuizResultEntity): Long

    @Query("SELECT * FROM quiz_results ORDER BY createdAt DESC LIMIT 20")
    fun observeResults(): Flow<List<QuizResultEntity>>
    // Note: Flow will be collected on Dispatchers.IO in the repository
}

@Dao
interface GamificationDao {
    @Query("SELECT * FROM gamification WHERE id = 1")
    fun observe(): Flow<GamificationEntity?>

    @Query("SELECT * FROM gamification WHERE id = 1")
    suspend fun get(): GamificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GamificationEntity)

    @Query("UPDATE gamification SET xp = :xp, updatedAt = :now WHERE id = 1")
    suspend fun updateXp(xp: Int, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM lesson_progress")
    fun observeLessonProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT COUNT(*) FROM lesson_progress")
    suspend fun completedLessonCount(): Int

    @Query("INSERT OR REPLACE INTO lesson_progress (slug, completedAt) VALUES (:slug, :completedAt)")
    suspend fun markLessonCompleted(slug: String, completedAt: Long = System.currentTimeMillis())
}

@Database(
    entities = [
        LessonEntity::class,
        QuizResultEntity::class,
        GamificationEntity::class,
        LessonProgressEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun gamificationDao(): GamificationDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS gamification (
                        id INTEGER NOT NULL PRIMARY KEY,
                        xp INTEGER NOT NULL DEFAULT 0,
                        streak INTEGER NOT NULL DEFAULT 0,
                        lastCheckInDay TEXT,
                        lastCheckInAt INTEGER,
                        badgesJson TEXT NOT NULL DEFAULT '[]',
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS lesson_progress (
                        slug TEXT NOT NULL PRIMARY KEY,
                        completedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("INSERT OR IGNORE INTO gamification (id, xp, streak, badgesJson, updatedAt) VALUES (1, 0, 0, '[]', 0)")
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kikompetenz.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()  // Allow destructive migration
                    .build().also { instance = it }
            }
    }
}