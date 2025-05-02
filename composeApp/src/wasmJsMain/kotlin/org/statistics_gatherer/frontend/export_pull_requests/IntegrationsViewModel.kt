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
        val project: String,
        val company: String,
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
        localStorage.setItem("project", _state.value.bitbucketApiKey?.project ?: "")
        localStorage.setItem("organization", _state.value.bitbucketApiKey?.company ?: "")
    }

    private fun load() {
        val apiKey = localStorage.getItem("apiKey")
        val project = localStorage.getItem("project")
        val organization = localStorage.getItem("organization")

        if (!apiKey.isNullOrBlank() && !project.isNullOrBlank() && !organization.isNullOrBlank()) {
            _state.value = _state.value.copy(
                bitbucketApiKey = ExportState.ApiKey(
                    key = apiKey,
                    project = project,
                    company = organization,
                    lastSyncDate = "",
                    status = ""
                )
            )
        }
    }

    fun applyBitbucketApiKey(
        apiKey: String,
        project: String,
        company: String
    ) = viewModelScope.launch {
        _isLoading.value = true

        try {
            pullRequestsService.applyBitbucketApiKey(
                apiKey = apiKey,
                project = project,
                company = company
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
                    project = project,
                    company = company
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
                    project = _state.value.bitbucketApiKey?.project ?: "",
                    company = _state.value.bitbucketApiKey?.company ?: ""
                )
            )
        } catch (e: Exception) {
            // Do nothing
        } finally {
            _isLoading.value = false
        }
    }
}