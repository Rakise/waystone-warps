package dev.mizarc.waystonewarps.interaction.menus.management

import dev.mizarc.waystonewarps.application.actions.world.CreateWarp
import dev.mizarc.waystonewarps.application.results.CreateWarpResult
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.common.TextInputMenu
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpNamingMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val location: Location
) : Menu, KoinComponent {
    private val createWarp: CreateWarp by inject()
    private val localizationProvider: LocalizationProvider by inject()

    override fun open() {
        menuNavigator.replaceMenu(TextInputMenu(player, menuNavigator, localizationProvider,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_NAMING_TITLE),
            description = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_NAMING_ITEM_WARP_LORE,
                location.blockX.toString(), location.blockY.toString(), location.blockZ.toString())) { name ->
            val result = createWarp.execute(player.uniqueId, name, location.toPosition3D(), location.world.uid,
                location.clone().subtract(0.0, 1.0, 0.0).block.type.name)
            when (result) {
                is CreateWarpResult.Success -> {
                    location.world.playSound(player.location, Sound.BLOCK_VAULT_OPEN_SHUTTER,
                        SoundCategory.BLOCKS, 1.0f, 1.0f)
                    menuNavigator.replaceMenu(WarpManagementMenu(player, menuNavigator, result.warp))
                    null
                }
                CreateWarpResult.LimitExceeded -> localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_LIMIT)
                CreateWarpResult.NameAlreadyExists -> localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_EXISTING)
                CreateWarpResult.NameCannotBeBlank -> localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_BLANK)
            }
        })
    }
}
