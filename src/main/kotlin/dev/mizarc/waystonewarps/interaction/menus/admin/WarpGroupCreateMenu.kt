package dev.mizarc.waystonewarps.interaction.menus.admin

import dev.mizarc.waystonewarps.application.actions.groups.CreateWarpGroup
import dev.mizarc.waystonewarps.application.actions.groups.CreateWarpGroupResult
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.common.TextInputMenu
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpGroupCreateMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val createWarpGroup: CreateWarpGroup by inject()

    override fun open() {
        menuNavigator.replaceMenu(TextInputMenu(player, menuNavigator, localizationProvider,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_CREATE_TITLE)) { name ->
            when (createWarpGroup.execute(player.uniqueId, name)) {
                CreateWarpGroupResult.SUCCESS -> { menuNavigator.goBack(); null }
                CreateWarpGroupResult.NAME_BLANK -> localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_BLANK)
                CreateWarpGroupResult.NAME_TAKEN -> localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_RENAME_NAME_TAKEN)
            }
        })
    }
}
