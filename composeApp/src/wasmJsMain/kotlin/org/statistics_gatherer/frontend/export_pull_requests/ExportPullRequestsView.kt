package org.statistics_gatherer.frontend.export_pull_requests

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PullRequest(
    val id: String,
    val title: String,
    val author: String,
    val state: String
) {
    companion object {
        fun random(id: Int): PullRequest {
            return PullRequest(
                id = "ECIOS-${id}",
                title = "Title ${(1..1000).random()}",
                author = "Author ${(1..1000).random()}",
                state = "open"
            )
        }
    }
}

class ExportPullRequestsViewModel: ViewModel() {
    private val client = HttpClient {
        install(Logging)
    }

    private var _state: MutableStateFlow<List<PullRequest>> = MutableStateFlow(emptyList())
    val state: StateFlow<List<PullRequest>> get() = _state

    fun updateState() {
        _state.value = emptyList()

        viewModelScope.launch {
//            val response = client.get("http://localhost:8081/test")
//            _state.value = response.body()

            val pullRequests = List(1000) { PullRequest.random(it) }
            _state.value = pullRequests
        }
    }
}

@Composable
fun ExportPullRequestsView(
    viewModel: ExportPullRequestsViewModel = viewModel { ExportPullRequestsViewModel() }
) {
    val pullRequests by viewModel.state.collectAsState()

    Column {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Export Pull Requests",
            style = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
        )

        Button(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            onClick = { viewModel.updateState() }
        ) {
            Text("Get Data from Server")
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(pullRequests, key = { it.id }) { pullRequest ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = pullRequest.id,
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = pullRequest.title,
                        style = MaterialTheme.typography.body1
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "",
                        tint = Color.Green,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.LightGray,
                    thickness = 1.dp
                )
            }
        }
    }
}