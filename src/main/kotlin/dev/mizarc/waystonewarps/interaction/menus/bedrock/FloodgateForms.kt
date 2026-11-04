package dev.mizarc.waystonewarps.interaction.menus.bedrock

import org.bukkit.entity.Player
import org.geysermc.floodgate.api.FloodgateApi
import org.geysermc.cumulus.form.CustomForm
import org.geysermc.cumulus.form.ModalForm
import org.geysermc.cumulus.form.SimpleForm
import org.geysermc.cumulus.form.Form

/** Uses the current Cumulus result handlers documented by GeyserMC. */
internal class FloodgateForms(
    private val send: (Player, Form) -> Boolean = { player, form ->
        FloodgateApi.getInstance().sendForm(player.uniqueId, form)
    }
) : BedrockForms {
    private val api get() = FloodgateApi.getInstance()
    override fun isBedrock(player: Player) = api.isFloodgatePlayer(player.uniqueId)

    override fun simple(player: Player, title: String, content: String, buttons: List<String>,
                        action: (Int) -> Unit, close: () -> Unit) {
        val form = SimpleForm.builder().title(title).content(content)
        buttons.forEach { form.button(it) }
        form.validResultHandler { response -> action(response.clickedButtonId()) }
            .closedOrInvalidResultHandler(java.util.function.Consumer { _ -> close() })
        if (!send(player, form.build())) close()
    }

    override fun input(player: Player, title: String, content: String, value: String,
                       action: (String) -> Unit, close: () -> Unit) {
        val form = CustomForm.builder().title(title)
            .label(content).input(title, "", value)
            .validResultHandler { response -> action(response.asInput(1) ?: "") }
            .closedOrInvalidResultHandler(java.util.function.Consumer { _ -> close() })
        if (!send(player, form.build())) close()
    }

    override fun confirm(player: Player, title: String, yes: String, no: String,
                         action: (Boolean) -> Unit, close: () -> Unit) {
        val form = ModalForm.builder().title(title).content(title).button1(yes).button2(no)
            .validResultHandler { response -> action(response.clickedFirst()) }
            .closedOrInvalidResultHandler(java.util.function.Consumer { _ -> close() })
        if (!send(player, form.build())) close()
    }
}
