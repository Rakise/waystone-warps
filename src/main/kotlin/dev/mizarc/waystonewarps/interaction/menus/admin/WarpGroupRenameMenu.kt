package dev.mizarc.waystonewarps.interaction.menus.admin

import dev.mizarc.waystonewarps.application.actions.groups.RenameWarpGroup
import dev.mizarc.waystonewarps.application.actions.groups.RenameWarpGroupResult
import dev.mizarc.waystonewarps.domain.warps.WarpGroup
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.common.TextInputMenu
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpGroupRenameMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val group: WarpGroup,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val renameWarpGroup: RenameWarpGroup by inject()

    override fun open() {
        menuNavigator.replaceMenu(TextInputMenu(player, menuNavigator, localizationProvider,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_RENAME_TITLE), group.name) { name ->
            when (renameWarpGroup.execute(group.id, name)) {
                RenameWarpGroupResult.SUCCESS, RenameWarpGroupResult.NOT_FOUND -> { menuNavigator.goBack(); null }
                RenameWarpGroupResult.NAME_BLANK -> localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_BLANK)
                RenameWarpGroupResult.NAME_TAKEN -> localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_RENAME_NAME_TAKEN)
            }
        })
    }
}
