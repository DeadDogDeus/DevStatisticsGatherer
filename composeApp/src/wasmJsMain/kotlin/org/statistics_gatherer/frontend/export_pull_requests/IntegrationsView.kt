package org.statistics_gatherer.frontend.export_pull_requests

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class ExportState(
    val bitbucketApiKey: ApiKey? = null
) {
    data class ApiKey(
        val key: String = "",
        val lastSyncDate: String = "",
        val status: String = ""
    )
}

class IntegrationsViewModel: ViewModel() {
    private val client = HttpClient {
        install(Logging)
    }

    private var pullRequestsCount = 100

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _state: MutableStateFlow<ExportState> = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> get() = _state

    fun applyBitbucketApiKey(apiKey: String) {
        _isLoading.value = true

        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _isLoading.value = false

            val now = Clock.System.now()
            val dateString = now.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

            _state.value = _state.value.copy(
                bitbucketApiKey = ExportState.ApiKey(
                    key = apiKey,
                    status = "Synced $pullRequestsCount pull requests",
                    lastSyncDate = "Last sync date: $dateString",
                )
            )
        }
    }

    fun deleteBitbucketApiKey() {
        _state.value = _state.value.copy(bitbucketApiKey = null)
    }

    fun syncBitbucketApiKey() {
        _isLoading.value = true

        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)

            _isLoading.value = false

            pullRequestsCount += (1..100).random()

            val now = Clock.System.now()
            val dateString = now.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

            _state.value = _state.value.copy(
                bitbucketApiKey = ExportState.ApiKey(
                    status = "Synced $pullRequestsCount pull requests",
                    lastSyncDate = "Last sync date: $dateString",
                )
            )
        }
    }
}

@Composable
fun IntegrationsView(
    viewModel: IntegrationsViewModel = viewModel { IntegrationsViewModel() }
) {
    val state by viewModel.state.collectAsState()

    Column {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Integrations",
            style = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
//            Button(
//                colors = ,
//                modifier = Modifier.fillMaxWidth().padding(32.dp),
//                onClick = { viewModel.updateState() }
//            ) {
//                Text("Get Data from Server")
//            }
        }
    }
}