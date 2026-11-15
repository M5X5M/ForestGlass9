package com.forestglass

import com.forestglass.data.PhysicsIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhysicsIndexTest {
    @Test
    fun normalizeArabic_stripsDiacriticsAndUnifiesLetters() {
        val raw = "قَانُونُ أُومٍ وَالمُقَاوَمَةُ"
        assertEquals("قانون اوم والمقاومه", PhysicsIndex.normalizeArabic(raw))
    }

    @Test
    fun search_findsOhmLawWithSimplifiedArabic() {
        val results = PhysicsIndex.search("اوم", PhysicsIndex.fallbackLaws)
        assertTrue(results.isNotEmpty())
        assertEquals("law_ohm", results.first().law.id)
    }
}
