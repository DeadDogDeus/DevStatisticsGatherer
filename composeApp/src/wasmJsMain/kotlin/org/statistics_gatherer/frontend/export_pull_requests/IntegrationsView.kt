package org.statistics_gatherer.frontend.export_pull_requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.delay
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
            delay(2000)
            _isLoading.value = false

            val now = Clock.System.now()
            val dateString = now.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

            _state.value = _state.value.copy(
                bitbucketApiKey = ExportState.ApiKey(
                    key = apiKey,
                    status = "Synced $pullRequestsCount pull requests",
                    lastSyncDate = dateString,
                )
            )
        }
    }

    fun deleteBitbucketApiKey() {
        _isLoading.value = true

        viewModelScope.launch {
            delay(2000)

            _isLoading.value = false
            _state.value = _state.value.copy(bitbucketApiKey = null)
        }
    }

    fun syncBitbucketApiKey() {
        _isLoading.value = true

        viewModelScope.launch {
            delay(2000)

            _isLoading.value = false

            pullRequestsCount += (1..100).random()

            val now = Clock.System.now()
            val dateString = now.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

            _state.value = _state.value.copy(
                bitbucketApiKey = ExportState.ApiKey(
                    key = _state.value.bitbucketApiKey?.key ?: "",
                    status = "Synced $pullRequestsCount pull requests",
                    lastSyncDate = dateString,
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Integrations",
            style = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val isLoading by viewModel.isLoading.collectAsState()

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.bitbucketApiKey != null) {
                BitbucketApiKeyView(viewModel)
            } else {
                AddBitBucketKeyView(state, viewModel)
            }
        }
    }
}

@Composable
private fun BitbucketApiKeyView(viewModel: IntegrationsViewModel) {
    Column(
        Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth()
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.deleteBitbucketApiKey() },
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
        ) {
            Text("Delete Bitbucket API Key")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.syncBitbucketApiKey() }
        ) {
            Text("Sync Bitbucket API Key")
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Last Sync Date: ${viewModel.state.value.bitbucketApiKey?.lastSyncDate}",
            style = MaterialTheme.typography.caption
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = viewModel.state.value.bitbucketApiKey?.status ?: "",
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun AddBitBucketKeyView(
    state: ExportState,
    viewModel: IntegrationsViewModel
) {
    var textValue by remember(state.bitbucketApiKey?.key) {
        mutableStateOf(state.bitbucketApiKey?.key ?: "")
    }

    Column(
        Modifier
        .widthIn(max = 400.dp)
        .fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = textValue,
            onValueChange = { textValue = it },
            label = { Text("Bitbucket API Key") }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = textValue.isNotEmpty(),
            onClick = { viewModel.applyBitbucketApiKey(textValue) }
        ) {
            Text("Apply")
        }
    }
}