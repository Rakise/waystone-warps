package dev.mizarc.waystonewarps.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.use.WarpMenu
import dev.mizarc.waystonewarps.interaction.menus.bedrock.BedrockSupport
import dev.mizarc.waystonewarps.interaction.menus.bedrock.BedrockHomeMenu
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@CommandAlias("warpmenu")
@CommandPermission("waystonewarps.command.warpmenu")
class WarpMenuCommand: BaseCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()

    @Default
    fun onWarp(player: Player, @Optional backCommand: String? = null) {
        val menuNavigator = MenuNavigator(player)
        if (BedrockSupport.isBedrock(player)) {
            menuNavigator.openMenu(BedrockHomeMenu(player, menuNavigator))
            return
        }
        menuNavigator.openMenu(WarpMenu(player, menuNavigator, localizationProvider))
    }
}
