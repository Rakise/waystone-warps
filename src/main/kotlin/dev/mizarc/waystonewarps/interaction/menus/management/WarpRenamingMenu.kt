package dev.mizarc.waystonewarps.interaction.menus.management

import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpName
import dev.mizarc.waystonewarps.application.results.UpdateWarpNameResult
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.common.TextInputMenu
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpRenamingMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val warp: Warp,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val updateWarpName: UpdateWarpName by inject()

    override fun open() {
        if (!PermissionHelper.canRename(player, warp.playerId)) {
            player.sendMessage(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_COMMON_NO_PERMISSION))
            menuNavigator.goBack()
            return
        }
        menuNavigator.replaceMenu(TextInputMenu(player, menuNavigator, localizationProvider,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_RENAMING_TITLE), warp.name) { name ->
            if (name == warp.name) {
                menuNavigator.goBack()
                null
            } else {
                when (updateWarpName.execute(warp.id, player.uniqueId, name, player.hasPermission("waystonewarps.bypass.rename"))) {
                    UpdateWarpNameResult.SUCCESS -> { menuNavigator.goBack(); null }
                    UpdateWarpNameResult.WARP_NOT_FOUND -> localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_NOT_FOUND)
                    UpdateWarpNameResult.NAME_ALREADY_TAKEN -> localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_EXISTING, name)
                    UpdateWarpNameResult.NAME_BLANK -> localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_BLANK)
                    UpdateWarpNameResult.NOT_AUTHORIZED -> localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_NO_PERMISSION)
                }
            }
        })
    }
}
