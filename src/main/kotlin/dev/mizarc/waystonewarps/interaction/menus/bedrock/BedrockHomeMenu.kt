package dev.mizarc.waystonewarps.interaction.menus.bedrock

import dev.mizarc.waystonewarps.application.actions.administration.ListInvalidWarps
import dev.mizarc.waystonewarps.application.actions.management.GetOwnedWarps
import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.interaction.commands.InvalidsCommand
import dev.mizarc.waystonewarps.interaction.commands.WarpCreateCommand
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.admin.WarpGroupManagementMenu
import dev.mizarc.waystonewarps.interaction.menus.common.ConfirmationMenu
import dev.mizarc.waystonewarps.interaction.menus.common.TextInputMenu
import dev.mizarc.waystonewarps.interaction.menus.management.WarpManagementMenu
import dev.mizarc.waystonewarps.interaction.menus.use.WarpMenu
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Mobile entry point, including operations otherwise exposed only through commands. */
class BedrockHomeMenu(private val player: Player, private val navigator: MenuNavigator) : Menu, KoinComponent {
    private val localization: LocalizationProvider by inject()
    private val owned: GetOwnedWarps by inject()
    private val invalids: ListInvalidWarps by inject()
    private val config: ConfigService by inject()
    private fun text(key: String) = localization.get(player.uniqueId, "bedrock.$key")

    override fun open() {
        val actions = mutableListOf<Pair<String, () -> Unit>>()
        actions += text("warps") to { navigator.openMenu(WarpMenu(player, navigator, localization)) }
        actions += text("manage") to { navigator.openMenu(OwnedMenu()) }
        if (player.hasPermission("waystonewarps.create")) actions += text("create") to {
            navigator.openMenu(TextInputMenu(player, navigator, localization, text("create"),
                description = text("create_help")) { name ->
                if (player.hasPermission("waystonewarps.create")) WarpCreateCommand().onWarpCreate(player, name)
                navigator.goBack()
                null
            })
        }
        if (config.warpGroupsEnabled() && player.hasPermission("waystonewarps.admin.manage_groups")) {
            actions += text("groups") to {
                if (player.hasPermission("waystonewarps.admin.manage_groups"))
                    navigator.openMenu(WarpGroupManagementMenu(player, navigator, localization))
            }
        }
        if (player.hasPermission("waystonewarps.admin.invalids.list"))
            actions += text("invalids") to { navigator.openMenu(InvalidMenu()) }
        actions += text("close") to { navigator.clearMenuStack() }
        BedrockSupport.simple(player, navigator, this, "Waystone Warps", text("home_help"), actions.map { it.first }) {
            actions[it].second()
        }
    }

    private inner class OwnedMenu : Menu {
        private var page = 0
        override fun open() {
            val warps = owned.execute(player.uniqueId).sortedBy { it.name }
            page = page.coerceIn(0, ((warps.size - 1) / 12).coerceAtLeast(0))
            val visible = warps.drop(page * 12).take(12)
            val actions = visible.map { warp -> warp.name to {
                navigator.openMenu(WarpManagementMenu(player, navigator, warp))
            } }.toMutableList()
            if (page > 0) actions += text("previous") to { page--; open() }
            if ((page + 1) * 12 < warps.size) actions += text("next") to { page++; open() }
            actions += text("back") to { navigator.goBack() }
            BedrockSupport.simple(player, navigator, this, text("manage"),
                if (warps.isEmpty()) text("no_owned") else "", actions.map { it.first },
                onClose = { navigator.goBack() }) { actions[it].second() }
        }
    }

    private inner class InvalidMenu : Menu {
        private var page = 0
        override fun open() {
            if (!player.hasPermission("waystonewarps.admin.invalids.list")) { navigator.goBack(); return }
            val worlds = invalids.listAllInvalidWarps().groupBy { it.worldId }.entries.sortedBy { it.key }
            page = page.coerceIn(0, ((worlds.size - 1) / 8).coerceAtLeast(0))
            val visible = worlds.drop(page * 8).take(8)
            val actions = mutableListOf<Pair<String, () -> Unit>>()
            for ((world, warps) in visible) actions += "$world (${warps.size})" to {
                if (player.hasPermission("waystonewarps.admin.invalids.remove"))
                    navigator.openMenu(ConfirmationMenu(navigator, player, "${text("remove_world")} $world") {
                        if (player.hasPermission("waystonewarps.admin.invalids.remove"))
                            InvalidsCommand().onRemoveInvalids(player, world.toString())
                    })
                else open()
            }
            if (player.hasPermission("waystonewarps.admin.invalids.removeall") && worlds.isNotEmpty())
                actions += text("remove_all") to {
                    navigator.openMenu(ConfirmationMenu(navigator, player, text("remove_all")) {
                        if (player.hasPermission("waystonewarps.admin.invalids.removeall"))
                            InvalidsCommand().onRemoveAllInvalids(player)
                    })
                }
            if (page > 0) actions += text("previous") to { page--; open() }
            if ((page + 1) * 8 < worlds.size) actions += text("next") to { page++; open() }
            actions += text("back") to { navigator.goBack() }
            BedrockSupport.simple(player, navigator, this, text("invalids"),
                if (worlds.isEmpty()) text("no_invalids") else text("invalids_help"), actions.map { it.first },
                onClose = { navigator.goBack() }) { actions[it].second() }
        }
    }
}
