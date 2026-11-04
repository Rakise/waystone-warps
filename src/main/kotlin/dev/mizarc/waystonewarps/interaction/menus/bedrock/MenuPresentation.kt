package dev.mizarc.waystonewarps.interaction.menus.bedrock

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.use.WarpMenu
import dev.mizarc.waystonewarps.interaction.menus.admin.WarpGroupManagementMenu
import dev.mizarc.waystonewarps.interaction.menus.management.WarpPlayerMenu
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** A second renderer for the same visible menu items and their existing business actions. */
object MenuPresentation : KoinComponent {
    private val localization: LocalizationProvider by inject()
    private val plain = PlainTextComponentSerializer.plainText()

    fun show(gui: NamedGui, player: Player, navigator: MenuNavigator, menu: Menu) {
        if (!BedrockSupport.isBedrock(player)) {
            gui.show(player)
            return
        }
        // Render the current page off-screen. No container is opened for Bedrock.
        gui.items.forEach { it.applyUUID() }
        gui.update()
        val inventory = when (gui) {
            is ChestGui -> gui.inventory
            is HopperGui -> gui.inventory
            else -> error("Unsupported menu renderer: ${gui.javaClass.name}")
        }
        val items = gui.items.toList()
        val visible = inventory.contents.mapIndexedNotNull { slot, stack ->
            if (stack == null || stack.type.isAir) return@mapIndexedNotNull null
            items.firstOrNull { it.isVisible && it.item.isSimilar(stack) }?.let { slot to it }
        }.filter { (_, item) -> label(item).isNotBlank() }

        BedrockSupport.simple(player, navigator, menu, gui.title, "", visible.map { label(it.second) }) { index ->
            val (slot, item) = visible[index]
            fun invoke(click: ClickType) {
                val revision = navigator.currentPresentation()
                // Call only the bound action, never the inventory event bus or inventory transfer logic.
                val event = InventoryClickEvent(player.openInventory, InventoryType.SlotType.CONTAINER,
                    0, click, InventoryAction.NOTHING)
                item.callAction(event)
                if (navigator.isCurrent(menu, revision) &&
                    !(menu is WarpMenu && slot in 18..44 && click == ClickType.LEFT)) menu.open()
            }
            val secondary = when {
                menu is WarpMenu && slot in 18..44 -> "bedrock.options"
                menu is WarpPlayerMenu && slot in 18..44 -> "bedrock.revoke"
                menu is WarpGroupManagementMenu && slot in 18..44 -> "bedrock.delete_group"
                else -> null
            }
            val mouseHints = listOf("menu.warp.item.warp.lore.left_click", "menu.warp.item.warp.lore.right_click",
                "menu.warp_player.item.player.lore.toggle_whitelist", "menu.warp_player.item.player.lore.revoke_access",
                "menu.warp_group_management.item.actions").map { text(player, it) }
            val lore = item.item.itemMeta?.lore()?.map { plain.serialize(it) }
                ?.filterNot { line -> mouseHints.any { hint -> line.contains(hint) } }?.joinToString("\n").orEmpty()
            if (secondary != null || lore.isNotBlank()) {
                val primary = when {
                    menu is WarpMenu && slot in 18..44 -> text(player, "bedrock.teleport")
                    menu is WarpPlayerMenu && slot in 18..44 -> text(player, "bedrock.whitelist")
                    menu is WarpGroupManagementMenu && slot in 18..44 -> text(player, "bedrock.rename")
                    else -> label(item)
                }
                val buttons = listOfNotNull(primary, secondary?.let { text(player, it) },
                    text(player, "bedrock.back"))
                BedrockSupport.simple(player, navigator, menu, label(item), lore, buttons,
                    onClose = { menu.open() }) { choice ->
                    when {
                        choice == 0 -> invoke(ClickType.LEFT)
                        secondary != null && choice == 1 -> invoke(ClickType.RIGHT)
                        else -> menu.open()
                    }
                }
            } else invoke(ClickType.LEFT)
        }
    }

    fun refresh(gui: NamedGui, player: Player, navigator: MenuNavigator, menu: Menu) {
        if (BedrockSupport.isBedrock(player)) show(gui, player, navigator, menu) else gui.update()
    }

    private fun text(player: Player, key: String) = localization.get(player.uniqueId, key)
    private fun label(item: GuiItem): String = item.item.itemMeta?.displayName()?.let { plain.serialize(it) }
        ?: item.item.type.name.lowercase().replace('_', ' ')
}
