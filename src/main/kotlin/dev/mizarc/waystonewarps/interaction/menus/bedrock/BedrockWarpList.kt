package dev.mizarc.waystonewarps.interaction.menus.bedrock

import dev.mizarc.waystonewarps.domain.warps.Warp
import java.util.Locale
import java.util.UUID

/** One complete scrollable list. Inventory pages and their controls never enter this model. */
internal fun bedrockWarpDestinations(warps: List<Warp>, groupId: UUID? = null): List<Warp> =
    warps.distinctBy { it.id }
        .filter { groupId == null || it.groupId == groupId }
        .sortedWith(compareBy<Warp> { it.name.lowercase(Locale.ROOT) }.thenBy { it.id })

/** Built-in Bedrock textures; unsupported/custom Java models get a visible waystone fallback. */
internal object BedrockIcons {
    const val WAYSTONE = "textures/blocks/lodestone_top"
    const val TELEPORT = "textures/items/ender_pearl"
    const val BACK = "textures/items/arrow"

    fun forMaterial(material: String): String = when (material.uppercase(Locale.ROOT)) {
        "COMPASS", "RECOVERY_COMPASS" -> "textures/items/compass_item"
        "DIAMOND", "DIAMOND_BLOCK" -> "textures/items/diamond"
        "EMERALD", "EMERALD_BLOCK" -> "textures/items/emerald"
        "GOLD_INGOT", "GOLD_BLOCK" -> "textures/items/gold_ingot"
        "IRON_INGOT", "IRON_BLOCK" -> "textures/items/iron_ingot"
        "NETHERITE_INGOT", "NETHERITE_BLOCK" -> "textures/items/netherite_ingot"
        "ENDER_PEARL", "ENDER_EYE" -> TELEPORT
        "NETHER_STAR" -> "textures/items/nether_star"
        "PAPER", "NAME_TAG" -> "textures/items/paper"
        "BOOK", "BOOKSHELF", "WRITABLE_BOOK", "ENCHANTED_BOOK" -> "textures/items/book_normal"
        "ARROW", "SPECTRAL_ARROW" -> BACK
        "STONE" -> "textures/blocks/stone"
        "GRASS_BLOCK" -> "textures/blocks/grass_side"
        "DIRT" -> "textures/blocks/dirt"
        "NETHERRACK" -> "textures/blocks/netherrack"
        "END_STONE" -> "textures/blocks/end_stone"
        "OBSIDIAN", "CRYING_OBSIDIAN" -> "textures/blocks/obsidian"
        else -> WAYSTONE
    }
}
