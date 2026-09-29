package dev.mizarc.waystonewarps.interaction.menus.management

import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.common.TextInputMenu
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PlayerSearchMenu(private val player: Player, private val menuNavigator: MenuNavigator) : Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()

    override fun open() {
        menuNavigator.replaceMenu(TextInputMenu(player, menuNavigator, localizationProvider,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_PLAYER_SEARCH_TITLE)) { input ->
            menuNavigator.goBackWithData(input)
            null
        })
    }
}
