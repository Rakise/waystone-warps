package dev.mizarc.waystonewarps.interaction.menus.use

import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.common.TextInputMenu
import org.bukkit.entity.Player

class WarpSearchMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val localizationProvider: LocalizationProvider
) : Menu {
    override fun open() {
        menuNavigator.replaceMenu(TextInputMenu(player, menuNavigator, localizationProvider,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_SEARCH_TITLE)) { input ->
            menuNavigator.goBackWithData(input)
            null
        })
    }
}
