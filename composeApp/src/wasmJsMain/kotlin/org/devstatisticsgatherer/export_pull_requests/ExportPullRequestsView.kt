package org.devstatisticsgatherer.export_pull_requests

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class ExportPullRequestsViewModel: ViewModel() {

}

@Composable
fun ExportPullRequestsView(
    viewModel: ExportPullRequestsViewModel = viewModel { ExportPullRequestsViewModel() }
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "Export Pull Requests",
        style = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
    )
}