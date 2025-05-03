package org.statistics_gatherer.frontend.export_pull_requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.browser.localStorage
import kotlinx.browser.window
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
        val key: String,
        val id: String,
        val lastSyncDate: String,
        val status: String
    )
}

class IntegrationsViewModel(
    private val pullRequestsService: PullRequestsService
): ViewModel() {
    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _loadingProgress: MutableStateFlow<Progress> = MutableStateFlow(Progress(0, 0))
    val loadingProgress: StateFlow<Progress> get() = _loadingProgress

    private val _state: MutableStateFlow<ExportState> = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> get() = _state

    fun initViewModel() {
        load()
    }

    private fun save() {
        localStorage.setItem("apiKey", _state.value.bitbucketApiKey?.key ?: "")
        localStorage.setItem("id", _state.value.bitbucketApiKey?.id ?: "")
    }

    private fun load() {
        val apiKey = localStorage.getItem("apiKey")
        val id = localStorage.getItem("id")

        if (!apiKey.isNullOrBlank() && !id.isNullOrBlank()) {
            _state.value = _state.value.copy(
                bitbucketApiKey = ExportState.ApiKey(
                    key = apiKey,
                    id = id,
                    lastSyncDate = "",
                    status = ""
                )
            )
        }
    }

    fun applyBitbucketApiKey(
        apiKey: String,
        id: String,
    ) = viewModelScope.launch {
        _isLoading.value = true

        try {
            pullRequestsService.applyBitbucketApiKey(
                apiKey = apiKey,
                id = id
            ).collect {
                _loadingProgress.value = it
            }

            val now = Clock.System.now()
            val dateString = now.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

            _state.value = _state.value.copy(
                bitbucketApiKey = ExportState.ApiKey(
                    key = apiKey,
                    status = "Synced ${pullRequestsService.pullRequests.value.size} pull requests",
                    lastSyncDate = dateString,
                    id = id
                )
            )

            save()
        } catch (e: Exception) {
            // print exception
            window.alert("Error applying Bitbucket API key: ${e.message}")
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
                    id = _state.value.bitbucketApiKey?.id ?: "",
                )
            )
        } catch (e: Exception) {
            // Do nothing
        } finally {
            _isLoading.value = false
        }
    }
}