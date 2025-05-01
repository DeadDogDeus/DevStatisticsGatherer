package org.statistics_gatherer.frontend.export_pull_requests

import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.flow.MutableStateFlow

data class PullRequest(
    val id: String,
    val title: String,
    val commentCount: Int,
    val author: String,
    val createdDate: String,
    val state: String
)

class PullRequestsService {
    private val _apiKey = MutableStateFlow<String?>(null)
    private val _pullRequests = MutableStateFlow<List<PullRequest>>(emptyList())
    val pullRequests get() = _pullRequests

    private val client = HttpClient {
        install(Logging)
    }

    suspend fun applyBitbucketApiKey(apiKey: String) {
    }

    suspend fun syncPullRequests() {
    }

    suspend fun deleteBitbucketApiKey() {
    }
}