package com.dhun.music.ui.viewmodel

import com.dhun.music.ui.components.DhunNavTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationStackReducerTest {
    @Test
    fun playlistBackReturnsToPreviousTab() {
        val home = NavigationStackReducer.selectTab(DhunNavTab.HOME)
        val library = NavigationStackReducer.selectTab(DhunNavTab.LIBRARY)
        val playlist = NavigationStackReducer.openPlaylist(library, 42L)
        assertEquals(42L, playlist.last().playlistId)
        assertEquals(DhunNavTab.LIBRARY, NavigationStackReducer.back(playlist)?.last()?.tab)
        assertEquals(home, NavigationStackReducer.back(NavigationStackReducer.openPlaylist(home, 42L))?.dropLast(0)?.let { listOf(it.first()) })
    }

    @Test
    fun queueAndPlayerBackPopOneOverlayAtATime() {
        val home = NavigationStackReducer.selectTab(DhunNavTab.HOME)
        val player = NavigationStackReducer.pushOverlay(home, "PLAYER")
        val queue = NavigationStackReducer.pushOverlay(player, "QUEUE")
        assertEquals("PLAYER", NavigationStackReducer.back(queue)?.last()?.overlay)
        assertEquals("ROOT", NavigationStackReducer.back(player)?.last()?.overlay)
    }

    @Test
    fun tabSelectionDoesNotAccumulateBackStackEntries() {
        val stack = NavigationStackReducer.selectTab(DhunNavTab.HOME)
        val switched = NavigationStackReducer.selectTab(DhunNavTab.SETTINGS)
        assertEquals(1, switched.size)
        assertEquals(DhunNavTab.SETTINGS, switched.last().tab)
        assertEquals(listOf(NavigationEntry(DhunNavTab.HOME)), NavigationStackReducer.back(switched))
        assertNull(NavigationStackReducer.back(stack))
    }

    @Test
    fun routeStateCanBeEncodedAndRestored() {
        val entry = NavigationEntry(DhunNavTab.LIBRARY, 91L, "PLAYER")
        assertEquals(entry, NavigationEntry.decode(entry.encode()))
        assertTrue(NavigationEntry.decode("NOT_A_TAB|-|ROOT") == null)
    }
}
