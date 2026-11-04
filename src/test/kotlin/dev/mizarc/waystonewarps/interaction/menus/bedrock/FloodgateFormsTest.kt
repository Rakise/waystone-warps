package dev.mizarc.waystonewarps.interaction.menus.bedrock

import org.bukkit.entity.Player
import org.geysermc.cumulus.form.Form
import org.geysermc.cumulus.form.SimpleForm
import org.geysermc.cumulus.form.CustomForm
import org.geysermc.cumulus.form.ModalForm
import org.geysermc.cumulus.form.impl.simple.SimpleFormDefinition
import org.geysermc.cumulus.form.impl.custom.CustomFormDefinition
import org.geysermc.cumulus.form.impl.modal.ModalFormDefinition
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import kotlin.test.assertEquals

class FloodgateFormsTest {
    private val player = mock(Player::class.java)
    private lateinit var sent: Form
    private val forms = FloodgateForms { _, form -> sent = form; true }

    @Test
    fun `touch selection maps to the requested action and invalid button does not act`() {
        var selected = -1
        var closed = 0
        forms.simple(player, "Warps", "", listOf("Home", "Mine"), { selected = it }, { closed++ })
        SimpleFormDefinition.instance().handleFormResponse(sent as SimpleForm, "1")
        assertEquals(1, selected)
        SimpleFormDefinition.instance().handleFormResponse(sent as SimpleForm, "99")
        assertEquals(1, selected)
        assertEquals(1, closed)
    }

    @Test
    fun `custom form reads the input after its label and preserves Unicode`() {
        var input = ""
        forms.input(player, "Rename", "Description", "Old", { input = it }, {})
        CustomFormDefinition.instance().handleFormResponse(sent as CustomForm, "[null,\"Casa — 山\"]")
        assertEquals("Casa — 山", input)
    }

    @Test
    fun `cancel and malformed input never submit text`() {
        var submissions = 0
        var closes = 0
        forms.input(player, "Search", "", "", { submissions++ }, { closes++ })
        CustomFormDefinition.instance().handleFormResponse(sent as CustomForm, null)
        CustomFormDefinition.instance().handleFormResponse(sent as CustomForm, "[null,42]")
        assertEquals(0, submissions)
        assertEquals(2, closes)
    }

    @Test
    fun `destructive confirmation distinguishes confirm cancel and closing`() {
        val results = mutableListOf<Boolean>()
        var closes = 0
        forms.confirm(player, "Remove group?", "Remove", "Back", { results += it }, { closes++ })
        ModalFormDefinition.instance().handleFormResponse(sent as ModalForm, "false")
        ModalFormDefinition.instance().handleFormResponse(sent as ModalForm, "true")
        ModalFormDefinition.instance().handleFormResponse(sent as ModalForm, null)
        assertEquals(listOf(false, true), results)
        assertEquals(1, closes)
    }

    @Test
    fun `failed transport closes without submitting`() {
        var closes = 0
        var submissions = 0
        FloodgateForms { _, _ -> false }.input(player, "Search", "", "", { submissions++ }, { closes++ })
        assertEquals(1, closes)
        assertEquals(0, submissions)
    }
}
