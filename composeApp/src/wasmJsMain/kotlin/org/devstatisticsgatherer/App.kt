package org.devstatisticsgatherer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.devstatisticsgatherer.export_pull_requests.ExportPullRequestsView
import org.devstatisticsgatherer.statistics.StatisticsView

enum class Screens {
    EXPORT_PULL_REQUESTS,
    STATISTICS
}

@Composable
fun App() {
    MaterialTheme {
        var screenType by remember { mutableStateOf(Screens.EXPORT_PULL_REQUESTS) }
        var expanded by remember { mutableStateOf(false) }

        when (screenType) {
            Screens.EXPORT_PULL_REQUESTS -> ExportPullRequestsView()
            Screens.STATISTICS -> StatisticsView()
        }

        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.Menu, contentDescription = "Screens")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    enabled = screenType != Screens.EXPORT_PULL_REQUESTS,
                    content = { Text("Export Pull Requests") },
                    onClick = { screenType = Screens.EXPORT_PULL_REQUESTS }
                )

                DropdownMenuItem(
                    enabled = screenType != Screens.STATISTICS,
                    content = { Text("Statistics") },
                    onClick = { screenType = Screens.STATISTICS }
                )
            }
        }
    }
}