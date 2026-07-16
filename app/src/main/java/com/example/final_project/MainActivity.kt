package com.example.final_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.final_project.ui.theme.FinalProjectTheme
import com.example.final_project.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // TODO (Phase 2): replace ThemeMode.SYSTEM with the value read
            //  from DataStore via a ViewModel/StateFlow.
            FinalProjectTheme(themeMode = ThemeMode.SYSTEM) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MelodifyPlaceholder(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/** Temporary placeholder shown until Phase 3 navigation is wired up. */
@Composable
private fun MelodifyPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MelodifyPreview() {
    FinalProjectTheme(themeMode = ThemeMode.DARK) {
        Scaffold { innerPadding ->
            MelodifyPlaceholder(modifier = Modifier.padding(innerPadding))
        }
    }
}