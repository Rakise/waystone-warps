package dev.mizarc.waystonewarps.interaction.menus.bedrock

import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpSkin
import dev.mizarc.waystonewarps.application.results.UpdateWarpSkinResult
import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.infrastructure.mappers.toLocation
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.menus.common.ConfirmationMenu
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
import dev.mizarc.waystonewarps.interaction.utils.getWarpMoveTool
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Touch alternatives for the physical item interactions used by Java players. */
class BedrockWorldMenu(private val player: Player, private val navigator: MenuNavigator,
                       private val warp: Warp) : Menu, KoinComponent {
    private val repository: WarpRepository by inject()
    private val config: ConfigService by inject()
    private val updateSkin: UpdateWarpSkin by inject()
    private val localization: LocalizationProvider by inject()
    private fun text(key: String) = localization.get(player.uniqueId, "bedrock.$key")

    override fun open() {
        if (repository.getById(warp.id) == null) { navigator.goBack(); return }
        val actions = mutableListOf<Pair<String, () -> Unit>>()
        if (player.uniqueId == warp.playerId) actions += text("change_skin") to { showSkins() }
        if (PermissionHelper.canRelocate(player, warp.playerId)) actions += text("move_tool") to {
            val tool = getWarpMoveTool(warp)
            if (player.inventory.contents.none { it?.itemMeta == tool.itemMeta }) {
                if (player.inventory.firstEmpty() < 0) player.sendMessage(text("inventory_full"))
                else player.inventory.addItem(tool)
            }
            BedrockSupport.simple(player, navigator, this, text("move_tool"), text("move_help"),
                listOf(text("back")), onClose = { open() }) { open() }
        }
        if (player.uniqueId == warp.playerId || player.hasPermission("waystonewarps.bypass.break"))
            actions += text("break_once") to {
                navigator.openMenu(ConfirmationMenu(navigator, player, text("break_confirm")) {
                    val current = repository.getById(warp.id)
                    if (current != null && nearby(current) &&
                        (current.playerId == player.uniqueId || player.hasPermission("waystonewarps.bypass.break"))) {
                        // Real Paper break: fires protection events and preserves the existing break counter/drops.
                        player.breakBlock(current.position.toLocation(player.world).block)
                    } else player.sendMessage(text("nearby_required"))
                })
            }
        actions += text("back") to { navigator.goBack() }
        BedrockSupport.simple(player, navigator, this, text("world_edit"), text("world_help"),
            actions.map { it.first }, onClose = { navigator.goBack() }) { actions[it].second() }
    }

    private fun nearby(current: Warp): Boolean = player.world.uid == current.worldId &&
        player.location.distanceSquared(current.position.toLocation(player.world).add(0.5, 0.5, 0.5)) <= 36

    private fun showSkins() {
        val valid = config.getAllSkinTypes().toSet()
        val candidates = player.inventory.storageContents.mapIndexedNotNull { slot, item ->
            item?.takeIf { it.type.name in valid }?.let { slot to it.clone() }
        }
        BedrockSupport.simple(player, navigator, this, text("change_skin"), text("skin_help"),
            candidates.map { "${it.first + 1}: ${it.second.type.name.lowercase().replace('_', ' ')}" } + text("back"),
            onClose = { open() }) { index ->
            if (index == candidates.size) { open(); return@simple }
            val (slot, selected) = candidates[index]
            navigator.openMenu(ConfirmationMenu(navigator, player, text("change_skin")) {
                val current = repository.getById(warp.id)
                val item = player.inventory.getItem(slot)
                if (current == null || current.playerId != player.uniqueId || !nearby(current) ||
                    item == null || !item.isSimilar(selected)) {
                    player.sendMessage(text("nearby_required"))
                } else {
                    val old = Material.valueOf(current.block)
                    if (updateSkin.execute(current.id, item.type.name) == UpdateWarpSkinResult.SUCCESS) {
                        if (player.gameMode != GameMode.CREATIVE) {
                            item.amount -= 1
                            player.inventory.setItem(slot, item)
                        }
                        player.world.dropItem(current.position.toLocation(player.world), ItemStack(old))
                    }
                }
            })
        }
    }
}
