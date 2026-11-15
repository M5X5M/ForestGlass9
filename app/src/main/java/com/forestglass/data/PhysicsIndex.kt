package com.forestglass.data

import android.content.Context
import org.json.JSONArray
import kotlin.math.max

data class PhysicsLaw(
    val id: String,
    val chapter: String,
    val title: String,
    val formula: String,
    val variables: String,
    val notes: String,
    val lessonId: String? = null
)

data class SearchMatch(
    val law: PhysicsLaw,
    val score: Float
)

object PhysicsIndex {

    fun loadFromAssets(context: Context): List<PhysicsLaw> {
        val jsonText = runCatching {
            context.assets.open("physics_index.json").bufferedReader().use { it.readText() }
        }.getOrNull() ?: return fallbackLaws

        return runCatching {
            val arr = JSONArray(jsonText)
            buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        PhysicsLaw(
                            id = obj.optString("id", "law_$i"),
                            chapter = obj.optString("chapter", ""),
                            title = obj.optString("title", ""),
                            formula = obj.optString("formula", ""),
                            variables = obj.optString("variables", ""),
                            notes = obj.optString("notes", ""),
                            lessonId = obj.optString("lessonId").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }.ifEmpty { fallbackLaws }
        }.getOrElse { fallbackLaws }
    }

    fun normalizeArabic(raw: String): String {
        val sb = StringBuilder(raw.length)
        var lastWasSpace = true
        for (ch in raw) {
            when (ch) {
                in '\u064B'..'\u065F', '\u0670', '\u0640' -> Unit // حذف التشكيل والتطويل
                'أ', 'إ', 'آ', 'ٱ' -> { sb.append('ا'); lastWasSpace = false }
                'ة' -> { sb.append('ه'); lastWasSpace = false }
                'ى' -> { sb.append('ي'); lastWasSpace = false }
                'ؤ' -> { sb.append('و'); lastWasSpace = false }
                'ئ' -> { sb.append('ي'); lastWasSpace = false }
                else -> {
                    if (ch.isWhitespace()) {
                        if (!lastWasSpace) {
                            sb.append(' ')
                            lastWasSpace = true
                        }
                    } else {
                        sb.append(ch.lowercaseChar())
                        lastWasSpace = false
                    }
                }
            }
        }
        return sb.toString().trim()
    }

    fun search(query: String, laws: List<PhysicsLaw>, limit: Int = 25): List<SearchMatch> {
        val q = normalizeArabic(query)
        if (q.isEmpty()) return laws.take(limit).map { SearchMatch(it, 1f) }

        val tokens = q.split(' ').filter { it.isNotEmpty() }
        return laws.mapNotNull { law ->
            val titleNorm = normalizeArabic(law.title)
            val chapterNorm = normalizeArabic(law.chapter)
            val notesNorm = normalizeArabic(law.notes)
            val formulaNorm = law.formula.lowercase()
            val combined = "$titleNorm $chapterNorm $notesNorm $formulaNorm"

            var score = 0f
            if (titleNorm == q) score += 12f
            if (titleNorm.contains(q)) score += 6f
            if (formulaNorm.contains(q)) score += 5f
            if (chapterNorm.contains(q) || notesNorm.contains(q)) score += 3f

            var tokenHits = 0
            for (t in tokens) {
                when {
                    titleNorm.contains(t) -> { score += 3.5f; tokenHits++ }
                    combined.contains(t) -> { score += 2.0f; tokenHits++ }
                    else -> {
                        val fuzzy = bestTokenSimilarity(t, titleNorm)
                        if (fuzzy >= 0.72f) {
                            score += fuzzy * 2.2f
                            tokenHits++
                        }
                    }
                }
            }

            if (tokens.size > 1 && tokenHits == tokens.size) score += 2.5f
            if (score >= 1.5f) SearchMatch(law, score) else null
        }
            .sortedByDescending { it.score }
            .take(limit)
    }

    private fun bestTokenSimilarity(needle: String, haystack: String): Float {
        if (needle.length < 2) return 0f
        var best = 0f
        for (word in haystack.split(' ')) {
            if (word.isEmpty()) continue
            val dist = levenshtein(needle, word)
            val maxLen = max(needle.length, word.length)
            val sim = 1f - dist.toFloat() / maxLen.toFloat()
            if (sim > best) best = sim
        }
        return best
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val cur = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + cost)
                prev = cur
            }
        }
        return dp[b.length]
    }

    val fallbackLaws = listOf(
        PhysicsLaw("law_ohm", "الفصل الثالث", "قانون أوم", "V = I × R", "V: فرق الجهد (فولت) · I: التيار (أمبير) · R: المقاومة (أوم)", "أساس حسابات الدوائر الكهربائية", "phy_ohm"),
        PhysicsLaw("law_kcl", "الفصل الثالث", "قانون كيرشوف الأول (التيارات)", "ΣI_in = ΣI_out", "مجموع التيارات الداخلة للعقدة يساوي الخارجة منها", "يعبّر عن قانون حفظ الشحنة الكهربائية", "phy_kirchhoff"),
        PhysicsLaw("law_kvl", "الفصل الثالث", "قانون كيرشوف الثاني (الفولتيات)", "ΣΔV = 0", "المجموع الجبري لفروق الجهد في أي مسار مغلق يساوي صفراً", "يعبّر عن قانون حفظ الطاقة", "phy_kirchhoff"),
        PhysicsLaw("law_r_series", "الفصل الثالث", "المقاومة المكافئة (ربط التوالي)", "Req = R1 + R2 + ...", "Req: المقاومة المكافئة بالأوم", "التيار متساوٍ في جميع المقاومات", "phy_equiv_r"),
        PhysicsLaw("law_r_parallel", "الفصل الثالث", "المقاومة المكافئة (ربط التوازي)", "1/Req = 1/R1 + 1/R2 + ...", "فرق الجهد متساوٍ عبر جميع الفروع", "المكافئة أصغر من أصغر مقاومة", "phy_equiv_r"),
        PhysicsLaw("law_cap_q", "الفصل الأول", "السعة والشحنة الكهربائية", "C = Q / ΔV", "C: السعة (فاراد) · Q: الشحنة (كولوم) · ΔV: فرق الجهد", "تزداد السعة بوجود العازل بنسبة ثابت العزل k", "phy_capacitors"),
        PhysicsLaw("law_cap_energy", "الفصل الأول", "الطاقة المختزنة في المتسعة", "PE = ½ C · (ΔV)²", "PE: الطاقة الكهربائية بالجول", "تُعطى أيضاً بالصيغة ½ Q·ΔV أو ½ Q²/C", "phy_capacitors"),
        PhysicsLaw("law_faraday", "الفصل الثاني", "قانون فرداي في الحث", "ε_ind = -N · (ΔΦB / Δt)", "N: عدد اللفات · ΔΦB: التغير في الفيض المغناطيسي", "الإشارة السالبة وفق قانون لنز", "phy_induction"),
        PhysicsLaw("law_self_ind", "الفصل الثاني", "القوة الدافعة المحتثة الذاتية", "ε_ind = -L · (ΔI / Δt)", "L: معامل الحث الذاتي (هنري)", "تتناسب طردياً مع المعدل الزمني لتغير التيار", "phy_induction"),
        PhysicsLaw("law_power", "الفصل الثالث", "القدرة الكهربائية المستهلكة", "P = I² × R = V × I", "P: القدرة (واط)", "تُستخدم لحساب القدرة الضائعة كحرارة", "phy_ohm")
    )
}
