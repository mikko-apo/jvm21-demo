package fi.iki.apo

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KotlinExamplesTest {
    @Test
    fun pmap() {
        assertEquals(listOf(1, 2).pmap { it + 1 }, listOf(2, 3))
    }

}