package org.statistics_gatherer.frontend.export_pull_requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class IntegrationsViewModel(
    private val pullRequestsService: PullRequestsService
): ViewModel() {
    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _state: MutableStateFlow<ExportState> = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> get() = _state

    fun applyBitbucketApiKey(apiKey: String) = viewModelScope.launch {
        _isLoading.value = true

        try {
            pullRequestsService.applyBitbucketApiKey(apiKey)

            val now = Clock.System.now()
            val dateString = now.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

            _state.value = _state.value.copy(
                bitbucketApiKey = ExportState.ApiKey(
                    key = apiKey,
                    status = "Synced ${pullRequestsService.pullRequests.value.size} pull requests",
                    lastSyncDate = dateString,
                )
            )
        } catch (e: Exception) {
            // Do nothing
        } finally {
            _isLoading.value = false
        }
    }

    fun deleteBitbucketApiKey() = viewModelScope.launch {
        _isLoading.value = true
        pullRequestsService.deleteBitbucketApiKey()

        _isLoading.value = false
        _state.value = _state.value.copy(bitbucketApiKey = null)
    }

    fun syncBitbucketApiKey() = viewModelScope.launch {
        _isLoading.value = true

        try {
            pullRequestsService.syncPullRequests()

            val now = Clock.System.now()
            val dateString = now.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

            _state.value = _state.value.copy(
                bitbucketApiKey = ExportState.ApiKey(
                    key = _state.value.bitbucketApiKey?.key ?: "",
                    status = "Synced ${pullRequestsService.pullRequests.value.size} pull requests",
                    lastSyncDate = dateString,
                )
            )
        } catch (e: Exception) {
            // Do nothing
        } finally {
            _isLoading.value = false
        }
    }
}