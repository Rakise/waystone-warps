package dev.mizarc.waystonewarps.interaction.menus.common

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MenuSearchTest {
    private data class Entry(val id: Int, val name: String)

    @Test
    fun `same named warps remain distinct and results extend beyond the first page`() {
        val entries = List(43) { Entry(it, "Home") }
        assertEquals(entries, searchMenuEntries(entries, "home") { it.name })
    }

    @Test
    fun `blank search preserves all entries and unmatched entries are removed`() {
        val entries = listOf(Entry(1, "Home"), Entry(2, "zzzzzz"))
        assertEquals(entries, searchMenuEntries(entries, "   ") { it.name })
        assertEquals(listOf(entries.first()), searchMenuEntries(entries, "Home") { it.name })
    }
}
