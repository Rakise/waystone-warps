package dev.mizarc.waystonewarps.interaction.menus.common

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.FurnaceGui
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import kotlin.test.assertNotNull

class InventoryConstructionTest {
    @Test
    fun `construct every inventory type used by the plugin on Paper 26_2`() {
        val plugin = mock(Plugin::class.java)
        val manager = mock(PluginManager::class.java)
        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<String> { Bukkit.getBukkitVersion() }.thenReturn("26.2.build.121-stable")
            bukkit.`when`<PluginManager> { Bukkit.getPluginManager() }.thenReturn(manager)
            assertNotNull(ChestGui(6, "Warps", plugin))
            assertNotNull(ChestGui(1, "Management", plugin))
            assertNotNull(HopperGui("Confirm", plugin))
            assertNotNull(FurnaceGui("Icon", plugin))
        }
    }
}
