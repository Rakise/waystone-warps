package dev.mizarc.waystonewarps.interaction.menus.common

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.util.Slot

/** Child coordinates are local to the paginated pane, regardless of its position in the chest. */
internal fun menuPages(items: List<GuiItem>, width: Int = 7, height: Int = 3): PaginatedPane {
    require(width > 0 && height > 0)
    val pages = PaginatedPane(width, height)
    for (chunk in items.chunked(width * height).ifEmpty { listOf(emptyList()) }) {
        val pane = StaticPane(width, height)
        chunk.forEachIndexed { index, item -> pane.addItem(item, index % width, index / width) }
        pages.addPage(Slot.fromXY(0, 0), pane)
    }
    return pages
}

internal fun PaginatedPane.restorePage(requestedPage: Int): Int {
    val selectedPage = requestedPage.coerceIn(1, pages.coerceAtLeast(1))
    page = selectedPage - 1
    return selectedPage
}
