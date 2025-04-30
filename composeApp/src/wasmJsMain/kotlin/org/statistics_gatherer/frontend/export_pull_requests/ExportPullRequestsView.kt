package org.statistics_gatherer.frontend.export_pull_requests

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.request
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExportPullRequestsViewModel: ViewModel() {
    private val client = HttpClient()

    private var _state: MutableStateFlow<String> = MutableStateFlow("")
    val state: StateFlow<String> get() = _state

    fun updateState() {
        _state.value = "Updated State"

        viewModelScope.launch {
            val response = client.get("http://localhost:8081/test")
            _state.value = response.body()
        }
    }
}

@Composable
fun ExportPullRequestsView(
    viewModel: ExportPullRequestsViewModel = viewModel { ExportPullRequestsViewModel() }
) {
    val state by viewModel.state.collectAsState()

    Column {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Export Pull Requests",
            style = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
        )

        Text(state, style = MaterialTheme.typography.h6.copy(textAlign = TextAlign.Center))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.updateState() }
        ) {
            Text("Get Data from Server")
        }
    }
}