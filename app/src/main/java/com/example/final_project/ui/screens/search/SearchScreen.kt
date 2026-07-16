package com.example.final_project.ui.screens.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.final_project.R
import com.example.final_project.ui.components.EmptyState

/** Step 1 placeholder — Step 2 owner implements live search + history. */
@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    EmptyState(
        titleRes = R.string.tab_search,
        subtitleRes = R.string.search_hint,
        modifier = modifier,
    )
}
