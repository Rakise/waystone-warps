package dev.mizarc.waystonewarps.interaction.menus.common

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertNull

class MenuPagesTest {
    private fun items(count: Int) = List(count) {
        mock(GuiItem::class.java).also { item ->
            `when`(item.isVisible).thenReturn(true)
            `when`(item.copy()).thenReturn(item)
        }
    }

    @Test
    fun `every entry appears once at its expected visible position across page boundaries`() {
        for (count in listOf(0, 1, 3, 7, 10, 11, 20, 21, 22, 42, 43, 100)) {
            val items = items(count)
            val pages = menuPages(items)
            assertEquals(((count + 20) / 21).coerceAtLeast(1), pages.pages, "count=$count")
            for (page in 0 until pages.pages) {
                pages.page = page
                val display = pages.display()
                for (slot in 0 until 21) {
                    val index = page * 21 + slot
                    val shown = display.getItem(slot % 7, slot / 7)
                    if (index < count) assertSame(items[index], shown, "count=$count index=$index")
                    else assertNull(shown, "count=$count index=$index")
                }
            }
        }
    }

    @Test
    fun `returning to a menu restores content and clamps pages after entries disappear`() {
        val pages = menuPages(items(43))
        assertEquals(3, pages.restorePage(3))
        assertEquals(2, pages.page)
        val reducedPages = menuPages(items(10))
        assertEquals(1, reducedPages.restorePage(3))
        assertEquals(0, reducedPages.page)
        assertEquals(1, menuPages(emptyList()).restorePage(3))
    }

    @Test
    fun `filtering entries before pagination leaves no gaps`() {
        val visible = items(43).filterIndexed { index, _ -> index % 3 == 0 }
        val pages = menuPages(visible)
        val display = pages.display()
        visible.forEachIndexed { index, item -> assertSame(item, display.getItem(index % 7, index / 7)) }
        assertNull(display.getItem(1, 2))
    }
}
