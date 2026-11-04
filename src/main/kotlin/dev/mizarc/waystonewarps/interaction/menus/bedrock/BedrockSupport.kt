package dev.mizarc.waystonewarps.interaction.menus.bedrock

import dev.mizarc.waystonewarps.WaystoneWarps
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.atomic.AtomicBoolean

/** Keeps optional Floodgate/Cumulus classes out of the Java-only loading path. */
object BedrockSupport {
    private val forms: BedrockForms? by lazy {
        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) FloodgateForms() else null
    }

    fun isBedrock(player: Player): Boolean = forms?.isBedrock(player) == true

    fun simple(player: Player, navigator: MenuNavigator, menu: Menu, title: String,
               content: String, buttons: List<String>, onClose: () -> Unit = {}, action: (Int) -> Unit) {
        val guarded = guard(player, navigator, menu)
        forms?.simple(player, title, content, buttons,
            { index -> guarded { if (index in buttons.indices) action(index) } }, { guarded(onClose) })
    }

    fun input(player: Player, navigator: MenuNavigator, menu: Menu, title: String,
              content: String, value: String, action: (String) -> Unit) {
        val guarded = guard(player, navigator, menu)
        forms?.input(player, title, content, value,
            { input -> guarded { action(input) } }, { guarded { navigator.goBack() } })
    }

    fun confirm(player: Player, navigator: MenuNavigator, menu: Menu, title: String,
                yes: String, no: String, action: () -> Unit) {
        val guarded = guard(player, navigator, menu)
        forms?.confirm(player, title, yes, no, { accepted -> guarded {
            if (accepted) action() else navigator.goBack()
        } }, { guarded { navigator.goBack() } })
    }

    private fun guard(player: Player, navigator: MenuNavigator, menu: Menu): (() -> Unit) -> Unit {
        val plugin = JavaPlugin.getPlugin(WaystoneWarps::class.java)
        val revision = navigator.beginPresentation()
        val consumed = AtomicBoolean()
        val permissions = player.effectivePermissions.map { it.permission to it.value }.toSet()
        return { action ->
            if (plugin.isEnabled && consumed.compareAndSet(false, true)) {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    if (!player.isOnline || !navigator.isCurrent(menu, revision)) return@Runnable
                    if (permissions != player.effectivePermissions.map { it.permission to it.value }.toSet()) {
                        menu.open()
                    } else action()
                })
            }
        }
    }
}

internal interface BedrockForms {
    fun isBedrock(player: Player): Boolean
    fun simple(player: Player, title: String, content: String, buttons: List<String>,
               action: (Int) -> Unit, close: () -> Unit)
    fun input(player: Player, title: String, content: String, value: String,
              action: (String) -> Unit, close: () -> Unit)
    fun confirm(player: Player, title: String, yes: String, no: String,
                action: (Boolean) -> Unit, close: () -> Unit)
}
