package com.forestglass.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "study_stats")
data class StudyStatEntity(
    @PrimaryKey val lessonId: String,
    val subjectId: String,
    val watchSeconds: Long = 0L,
    val completedCount: Int = 0,
    val masteryScore: Float = 0f,
    val lastStudiedAtMs: Long = 0L,
    val nextReviewAtMs: Long = 0L,
    val easeFactor: Float = 2.5f
)

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_stats")
    fun observeAll(): Flow<List<StudyStatEntity>>

    @Query("SELECT * FROM study_stats WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getStat(lessonId: String): StudyStatEntity?

    @Upsert
    suspend fun upsert(stat: StudyStatEntity)

    @Query("DELETE FROM study_stats")
    suspend fun clearAll()
}

@Database(entities = [StudyStatEntity::class], version = 1, exportSchema = false)
abstract class ForestDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var instance: ForestDatabase? = null

        fun get(context: Context): ForestDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ForestDatabase::class.java,
                    "forest_glass.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
