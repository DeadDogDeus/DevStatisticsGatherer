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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val isLoading by viewModel.isLoading.collectAsState()
            val loadingProgress by viewModel.loadingProgress.collectAsState()

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()

                        Text(
                            text = "${loadingProgress.has} / ${loadingProgress.from}",
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (state.bitbucketApiKey?.status == "" || state.bitbucketApiKey?.status == null) {
                AddBitBucketKeyView(state, viewModel)
            } else {
                BitbucketApiKeyView(viewModel)
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
    var apiKey by remember(state.bitbucketApiKey?.key) {
        mutableStateOf(state.bitbucketApiKey?.key ?: "")
    }

    var company by remember(state.bitbucketApiKey?.company) {
        mutableStateOf(state.bitbucketApiKey?.company ?: "")
    }

    var project by remember(state.bitbucketApiKey?.project) {
        mutableStateOf(state.bitbucketApiKey?.project ?: "")
    }

    Column(
        Modifier
        .widthIn(max = 400.dp)
        .fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("Bitbucket API Key") }
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = company,
            onValueChange = { company = it },
            label = { Text("Bitbucket Organization") }
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = project,
            onValueChange = { project = it },
            label = { Text("Bitbucket Project") }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = apiKey.isNotEmpty() && company.isNotEmpty() && project.isNotEmpty(),
            onClick = { viewModel.applyBitbucketApiKey(
                apiKey = apiKey,
                project = project,
                company = company
            ) }
        ) {
            Text("Apply")
        }
    }
}