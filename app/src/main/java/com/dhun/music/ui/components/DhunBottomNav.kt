package com.dhun.music.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dhun.music.R
import com.dhun.music.ui.theme.DhunBackground
import com.dhun.music.ui.theme.DhunBorder
import com.dhun.music.ui.theme.DhunPink
import com.dhun.music.ui.theme.DhunSurface
import com.dhun.music.ui.theme.DhunTextPrimary
import com.dhun.music.ui.theme.DhunTextSecondary
import com.dhun.music.ui.theme.DhunViolet
import com.dhun.music.ui.theme.DhunVioletDark

enum class DhunNavTab(
    val titleRes: Int,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
) {
    HOME(R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
    SEARCH(R.string.nav_search, Icons.Filled.Search, Icons.Outlined.Search, "nav_tab_search"),
    LIBRARY(R.string.nav_library, Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic, "nav_tab_library"),
    SETTINGS(R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings, "nav_tab_settings")
}

@Composable
fun DhunBottomNav(
    selectedTab: DhunNavTab,
    onTabSelected: (DhunNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .drawBehind {
                drawLine(
                    color = DhunBorder,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .testTag("dhun_bottom_navigation"),
        containerColor = DhunBackground,
        tonalElevation = 0.dp
    ) {
        DhunNavTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
                        contentDescription = stringResource(tab.titleRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.titleRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) DhunTextPrimary else DhunTextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DhunPink,
                    unselectedIconColor = DhunTextSecondary,
                    selectedTextColor = DhunTextPrimary,
                    unselectedTextColor = DhunTextSecondary,
                    indicatorColor = DhunViolet.copy(alpha = 0.22f)
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
