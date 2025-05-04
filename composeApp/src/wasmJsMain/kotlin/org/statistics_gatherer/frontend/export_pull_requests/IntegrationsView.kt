package org.statistics_gatherer.frontend.export_pull_requests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.browser.localStorage
import org.statistics_gatherer.frontend.pullRequestService

@Composable
fun IntegrationsView(
    viewModel: IntegrationsViewModel = viewModel { IntegrationsViewModel(pullRequestService) }
) {
    LaunchedEffect(Unit) {
        viewModel.initViewModel()
    }

    val state by viewModel.state.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Integrations",
            style = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AddBitBucketKeyView(viewModel)

                state.forEach { integration ->
                    Divider(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .widthIn(max = 400.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
                    )

                    BitbucketApiKeyView(
                        integration = integration,
                        sync = {
                            viewModel.syncBitbucketApiKey(
                                apiKey = integration.key,
                                id = integration.id
                            )
                        },
                        delete = {
                            viewModel.deleteBitbucketApiKey(integration.id)
                        }
                    )
                }
            }

            val isLoading by viewModel.isLoading.collectAsState()
            val loadingProgress by viewModel.loadingProgress.collectAsState()

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()

                        Text(
                            text = "${loadingProgress.has} / ${loadingProgress.from}",
                            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BitbucketApiKeyView(
    integration: IntegrationsViewModel.Integration,
    sync: () -> Unit,
    delete: () -> Unit) {
    Column(
        Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = integration.id,
            style = MaterialTheme.typography.body1
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = integration.status,
            style = MaterialTheme.typography.body1
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { delete() },
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
        ) {
            Text("Delete")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { sync() }
        ) {
            Text("Sync")
        }
    }
}

@Composable
private fun AddBitBucketKeyView(viewModel: IntegrationsViewModel) {
    val state by viewModel.state.collectAsState()

    var apiKey by remember { mutableStateOf(localStorage.getItem("apiKey") ?: "") }
    var id by remember { mutableStateOf("") }

    Column(
        Modifier
        .widthIn(max = 400.dp)
        .fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = apiKey,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            onValueChange = { apiKey = it },
            label = { Text("Bitbucket API Key") },
            visualTransformation = PasswordVisualTransformation()
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = id,
            onValueChange = { id = it },
            label = { Text("organization/project") }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = apiKey.isNotEmpty() && id.isNotEmpty() && !state.any { it.id == id },
            onClick = {
                viewModel.syncBitbucketApiKey(
                    apiKey = apiKey,
                    id = id
                )

                localStorage.setItem("apiKey", apiKey)

                id = ""
            }
        ) {
            Text("Add")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.demo() }
        ) {
            Text("Demo")
        }
    }
}