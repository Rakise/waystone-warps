package dev.mizarc.waystonewarps.interaction.menus.common

import me.xdrop.fuzzywuzzy.FuzzySearch

/** Keep each matching entry, including entries owned by different players with the same name. */
internal fun <T> searchMenuEntries(entries: List<T>, query: String, name: (T) -> String): List<T> {
    if (query.isBlank()) return entries
    return entries.filter { FuzzySearch.weightedRatio(query, name(it)) >= 60 }
}
