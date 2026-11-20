package com.dhun.music.ui.viewmodel

import com.dhun.music.ui.components.DhunNavTab

internal data class NavigationEntry(
    val tab: DhunNavTab,
    val playlistId: Long? = null,
    val overlay: String = "ROOT"
) {
    fun encode(): String = "${tab.name}|${playlistId ?: "-"}|$overlay"

    companion object {
        fun decode(value: String): NavigationEntry? {
            val parts = value.split("|")
            if (parts.size != 3) return null
            val tab = runCatching { DhunNavTab.valueOf(parts[0]) }.getOrNull() ?: return null
            val playlistId = parts[1].takeIf { it != "-" }?.toLongOrNull()
            if (parts[1] != "-" && playlistId == null) return null
            if (parts[2] !in setOf("ROOT", "PLAYER", "QUEUE")) return null
            return NavigationEntry(tab, playlistId, parts[2])
        }
    }
}

internal object NavigationStackReducer {
    fun selectTab(tab: DhunNavTab): List<NavigationEntry> =
        listOf(NavigationEntry(tab))

    fun openPlaylist(stack: List<NavigationEntry>, playlistId: Long): List<NavigationEntry> {
        val current = stack.lastOrNull() ?: NavigationEntry(DhunNavTab.HOME)
        return stack.dropLast(1) + current.copy(overlay = "ROOT") +
            NavigationEntry(current.tab, playlistId, "ROOT")
    }

    fun pushOverlay(stack: List<NavigationEntry>, overlay: String): List<NavigationEntry> {
        require(overlay == "PLAYER" || overlay == "QUEUE")
        val current = stack.lastOrNull() ?: NavigationEntry(DhunNavTab.HOME)
        if (current.overlay == overlay) return stack
        return stack + current.copy(overlay = overlay)
    }

    fun back(stack: List<NavigationEntry>): List<NavigationEntry>? {
        if (stack.size > 1) return stack.dropLast(1)
        val currentTab = stack.lastOrNull()?.tab ?: DhunNavTab.HOME
        return if (currentTab != DhunNavTab.HOME) selectTab(DhunNavTab.HOME) else null
    }
}
