package com.forestglass.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

data class ReviewSuggestion(
    val lesson: Lesson,
    val statusLabel: String,
    val mastery: Float
)

class StudyTracker(private val context: Context) {

    private val dao by lazy { ForestDatabase.get(context).studyDao() }
    private val examWeights: Map<String, Int> by lazy { loadExamWeights() }

    val statsFlow: Flow<List<StudyStatEntity>> =
        dao.observeAll().catch { emit(emptyList()) }

    fun pickSuggestedReview(
        stats: List<StudyStatEntity>,
        nowMs: Long = System.currentTimeMillis()
    ): ReviewSuggestion {
        val map = stats.associateBy { it.lessonId }
        val scored = LessonRepository.allLessons.map { lesson ->
            val stat = map[lesson.id]
            val weight = (examWeights[lesson.title] ?: 3).toFloat()
            val priority = if (stat == null || stat.lastStudiedAtMs == 0L) {
                100f + weight * 4f
            } else {
                val overdueDays = max(0f, (nowMs - stat.nextReviewAtMs) / 86_400_000f)
                val weakness = (1f - stat.masteryScore) * 40f
                overdueDays * 25f + weakness + weight * 3f
            }
            Triple(lesson, stat, priority)
        }.maxByOrNull { it.third }

        val chosen = scored?.first ?: LessonRepository.allLessons.first()
        val stat = scored?.second
        val label = when {
            stat == null || stat.lastStudiedAtMs == 0L -> "لم تبدأ به بعد"
            stat.masteryScore >= 0.8f -> "مراجعة للحفاظ على الإتقان (${(stat.masteryScore * 100).toInt()}%)"
            else -> "يستحق المراجعة · الإتقان ${(stat.masteryScore * 100).toInt()}%"
        }
        return ReviewSuggestion(chosen, label, stat?.masteryScore ?: 0f)
    }

    suspend fun recordStudyProgress(
        lesson: Lesson,
        addedSeconds: Long,
        markCompleted: Boolean = false
    ) {
        runCatching {
            val now = System.currentTimeMillis()
            val cur = dao.getStat(lesson.id) ?: StudyStatEntity(
                lessonId = lesson.id,
                subjectId = lesson.subject.id
            )
            val totalSec = cur.watchSeconds + max(0L, addedSeconds)
            val newCompletions = if (markCompleted) cur.completedCount + 1 else cur.completedCount
            val gain = if (markCompleted) 0.35f else min(0.15f, addedSeconds / 300f)
            val mastery = min(1f, max(cur.masteryScore, 0.1f) + gain)
            val ease = if (markCompleted) min(3.0f, cur.easeFactor + 0.1f) else cur.easeFactor
            val intervalDays = when {
                newCompletions <= 0 -> 1f
                newCompletions == 1 -> 2f
                else -> (newCompletions * ease)
            }
            val nextReview = now + (intervalDays * 86_400_000f).toLong()

            dao.upsert(
                cur.copy(
                    watchSeconds = totalSec,
                    completedCount = newCompletions,
                    masteryScore = mastery,
                    lastStudiedAtMs = now,
                    nextReviewAtMs = nextReview,
                    easeFactor = ease
                )
            )
        }
    }

    suspend fun resetAll() {
        runCatching { dao.clearAll() }
    }

    private fun loadExamWeights(): Map<String, Int> = runCatching {
        val text = context.assets.open("exam_weights.json").bufferedReader().use { it.readText() }
        val json = JSONObject(text)
        buildMap {
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                put(k, json.optInt(k, 1))
            }
        }
    }.getOrElse { emptyMap() }
}
