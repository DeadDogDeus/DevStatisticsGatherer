package org.statistics_gatherer.frontend.export_pull_requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IntegrationsViewModel(
    private val pullRequestsService: PullRequestsService
): ViewModel() {
    data class Integration(
        val key: String,
        val id: String,
        val status: String
    )

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _loadingProgress: MutableStateFlow<Progress> = MutableStateFlow(Progress(0, 0))
    val loadingProgress: StateFlow<Progress> get() = _loadingProgress

    private val _state: MutableStateFlow<List<Integration>> = MutableStateFlow(emptyList())
    val state: StateFlow<List<Integration>> get() = _state

    fun initViewModel() = viewModelScope.launch {
        pullRequestsService.integrations.collect { integrations ->
            _state.value = integrations.map { integration ->
                Integration(
                    key = integration.apiKey,
                    id = integration.id,
                    status = "Synced ${integration.pullRequests.size} pull requests"
                )
            }
        }
    }

    fun syncBitbucketApiKey(
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
        } catch (e: Exception) {
            window.alert("Error applying Bitbucket API key: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    fun deleteBitbucketApiKey(id: String) = viewModelScope.launch {
        _isLoading.value = true
        pullRequestsService.deleteBitbucketApiKey(id = id)

        _isLoading.value = false
    }

    fun demo() = viewModelScope.launch {
        _isLoading.value = true

        try {
            pullRequestsService.demo().collect {
                _loadingProgress.value = it
            }
        } catch (e: Exception) {
            window.alert("Error applying Bitbucket API key: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }
}