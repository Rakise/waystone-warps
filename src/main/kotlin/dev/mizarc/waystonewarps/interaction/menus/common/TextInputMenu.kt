package dev.mizarc.waystonewarps.interaction.menus.common

import dev.mizarc.waystonewarps.WaystoneWarps
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

/** Text entry through Paper's public API, without version-specific anvil containers. */
class TextInputMenu(
    private val player: Player,
    private val navigator: MenuNavigator,
    private val localization: LocalizationProvider,
    private val title: String,
    private val initialValue: String = "",
    private val description: String? = null,
    private val onSubmit: (String) -> String?
) : Menu {
    override fun open() = show(initialValue)

    private fun show(value: String, error: String? = null) {
        val plugin = JavaPlugin.getPlugin(WaystoneWarps::class.java)
        val serializer = LegacyComponentSerializer.legacySection()
        val consumed = AtomicBoolean()
        val options = ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(10)).build()

        fun action(submit: Boolean) = DialogAction.customClick({ response, audience ->
            if (plugin.isEnabled && audience is Player && audience.uniqueId == player.uniqueId && consumed.compareAndSet(false, true)) {
                val input = response.getText("value") ?: ""
                // Inventory navigation and world/database mutations must run on the server thread.
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    if (!player.isOnline || !navigator.isCurrent(this)) return@Runnable
                    if (!submit) {
                        navigator.goBack()
                    } else {
                        val message = onSubmit(input)
                        if (message != null && navigator.isCurrent(this)) show(input, message)
                    }
                })
            }
        }, options)

        val dialog = Dialog.create { builder ->
            builder.empty()
                .base(DialogBase.builder(serializer.deserialize(title))
                    .canCloseWithEscape(true)
                    .afterAction(DialogBase.DialogAfterAction.CLOSE)
                    .body(listOfNotNull(description, error).map { DialogBody.plainMessage(serializer.deserialize(it)) })
                    .inputs(listOf(DialogInput.text("value", serializer.deserialize(title))
                        .initial(value).maxLength(50).build()))
                    .build())
                .type(DialogType.confirmation(
                    ActionButton.builder(serializer.deserialize(localization.get(player.uniqueId,
                        LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME))).action(action(true)).build(),
                    ActionButton.builder(serializer.deserialize(localization.get(player.uniqueId,
                        LocalizationKeys.MENU_COMMON_ITEM_BACK_NAME))).action(action(false)).build()
                ))
        }
        // Defer opening when this was requested by an inventory click.
        Bukkit.getScheduler().runTask(plugin, Runnable {
            if (player.isOnline && navigator.isCurrent(this)) {
                player.closeInventory()
                player.showDialog(dialog)
            }
        })
    }
}
