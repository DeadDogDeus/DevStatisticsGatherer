package org.statistics_gatherer.frontend.export_pull_requests

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class PullRequest(
    val id: String,
    val title: String,
    @SerialName("comment_count")
    val commentCount: Int,
    val author: Author,
    @SerialName("created_on")
    val createdDate: String,
    val state: String
) {

    @Serializable
    @JsonIgnoreUnknownKeys
    data class Author(
        @SerialName("display_name")
        val displayName: String
    )
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class PullRequestResponse(
    val values: List<PullRequest>,
    val pagelen: Int,
    val size: Int,
    val page: Int,
    val next: String? = null
)

class PullRequestsService {
    private val _apiKey = MutableStateFlow<String?>(null)
    private val _project = MutableStateFlow<String?>(null)
    private val _company = MutableStateFlow<String?>(null)

    private val _pullRequests = MutableStateFlow<List<PullRequest>>(emptyList())
    val pullRequests get() = _pullRequests

    private val client = HttpClient {
        install(Logging)
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun applyBitbucketApiKey(apiKey: String, project: String, company: String) {
        _apiKey.value = apiKey
        _project.value = project
        _company.value = company

        var result = client.get("https://api.bitbucket.org/2.0/repositories/$company/$project/pullrequests") {
            url {
                parameters.append("state", "\"\"")
            }
            headers {
                append(HttpHeaders.Authorization, "Basic $apiKey")
                append(HttpHeaders.Accept, "application/json")
            }
        }.body<PullRequestResponse>()

        _pullRequests.value = result.values

        while (result.next != null) {
            result = client.get(result.next!!) {
                headers {
                    append(HttpHeaders.Authorization, "Basic $apiKey")
                    append(HttpHeaders.Accept, "application/json")
                }
            }.body<PullRequestResponse>()

            _pullRequests.value += result.values
        }
    }

    suspend fun syncPullRequests() {
        window.alert("Coming soon:)")
    }

    suspend fun deleteBitbucketApiKey() {
        _apiKey.value = null
        _project.value = null
        _company.value = null

        _pullRequests.value = emptyList()
    }
}