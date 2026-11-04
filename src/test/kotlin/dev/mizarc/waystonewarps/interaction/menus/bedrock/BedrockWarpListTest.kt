package dev.mizarc.waystonewarps.interaction.menus.bedrock

import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpAccess
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BedrockWarpListTest {
    private fun warp(name: String) = Warp(UUID.randomUUID(), UUID.randomUUID(),
        Position3D(0, 64, 0), name, "STONE")

    @Test
    fun `all destinations remain visible beyond the old inventory page boundaries`() {
        for (count in listOf(0, 1, 10, 11, 21, 22, 43, 127, 300)) {
            val warps = List(count) { warp("Warp $it") }
            val visible = bedrockWarpDestinations(warps)
            assertEquals(count, visible.size)
            assertEquals(warps.map { it.id }.toSet(), visible.map { it.id }.toSet())
        }
    }

    @Test
    fun `same named destinations are preserved while duplicate identities are removed`() {
        val first = warp("Casa")
        val second = warp("Casa")
        val result = bedrockWarpDestinations(listOf(first, first, second))
        assertEquals(2, result.size)
        assertNotEquals(result[0].id, result[1].id)
        assertEquals(result.map { it.id }.sorted(), result.map { it.id })
    }

    @Test
    fun `the simple list has no usable only or world filter hiding known destinations`() {
        val private = warp("Private").also { it.accessLevel = WarpAccess.PRIVATE }
        val otherWorld = warp("Other world")
        assertEquals(setOf(private.id, otherWorld.id),
            bedrockWarpDestinations(listOf(private, otherWorld)).map { it.id }.toSet())
    }

    @Test
    fun `explicit group navigation still respects its requested group`() {
        val group = UUID.randomUUID()
        val first = warp("A").also { it.groupId = group }
        val second = warp("B")
        assertEquals(listOf(first), bedrockWarpDestinations(listOf(first, second), group))
    }

    @Test
    fun `unknown custom materials use a native waystone icon rather than an empty image`() {
        assertEquals(BedrockIcons.WAYSTONE, BedrockIcons.forMaterial("CUSTOM_MODEL"))
        assertEquals("textures/items/diamond", BedrockIcons.forMaterial("diamond"))
        assertTrue(BedrockIcons.forMaterial("LODESTONE").startsWith("textures/"))
    }
}
