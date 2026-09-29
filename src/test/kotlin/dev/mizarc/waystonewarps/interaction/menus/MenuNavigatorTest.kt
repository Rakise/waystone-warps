package dev.mizarc.waystonewarps.interaction.menus

import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MenuNavigatorTest {
    private class RecordingMenu : Menu {
        var opens = 0
        var data: Any? = null
        override fun open() { opens++ }
        override fun passData(data: Any?) { this.data = data }
    }

    @Test
    fun `opening another compass or waystone session invalidates the old prompt`() {
        val player = mock(Player::class.java)
        val oldNavigator = MenuNavigator(player)
        val oldPrompt = RecordingMenu()
        oldNavigator.openMenu(oldPrompt)
        val newNavigator = MenuNavigator(player)
        val newMenu = RecordingMenu()
        newNavigator.openMenu(newMenu)
        assertFalse(oldNavigator.isCurrent(oldPrompt))
        assertTrue(newNavigator.isCurrent(newMenu))
    }

    @Test
    fun `replacing an input menu preserves the parent and sends search data back once`() {
        val navigator = MenuNavigator(mock(Player::class.java))
        val parent = RecordingMenu()
        val entry = RecordingMenu()
        val prompt = RecordingMenu()
        navigator.openMenu(parent)
        navigator.openMenu(entry)
        navigator.replaceMenu(prompt)
        assertFalse(navigator.isCurrent(entry))
        assertTrue(navigator.isCurrent(prompt))
        navigator.goBackWithData("Home")
        assertEquals("Home", parent.data)
        assertEquals(2, parent.opens)
        assertTrue(navigator.isCurrent(parent))
        assertFalse(navigator.isCurrent(prompt))
    }

    @Test
    fun `successful creation replaces its prompt so back cannot create a second warp`() {
        val player = mock(Player::class.java)
        val navigator = MenuNavigator(player)
        val prompt = RecordingMenu()
        navigator.openMenu(prompt)
        navigator.replaceMenu(RecordingMenu())
        navigator.goBack()
        verify(player).closeInventory()
        assertEquals(1, prompt.opens)
        assertFalse(navigator.isCurrent(prompt))
    }
}
