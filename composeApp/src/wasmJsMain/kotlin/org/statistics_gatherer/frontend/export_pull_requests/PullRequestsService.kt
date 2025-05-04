package org.statistics_gatherer.frontend.export_pull_requests

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class PullRequest(
    val id: Int,
    val title: String,
    @SerialName("comment_count")
    val commentCount: Int,
    val author: Author,
    @SerialName("created_on")
    val createdDate: Instant,
    val state: String
) {

    @Serializable
    @JsonIgnoreUnknownKeys
    data class Author(
        @SerialName("display_name")
        val displayName: String
    )

    val year: Int
        get() = createdDate.toLocalDateTime(TimeZone.currentSystemDefault()).year
}

data class Integration(
    val id: String,
    val apiKey: String,
    val pullRequests: List<PullRequest>
)

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

data class Progress(
    val has: Int,
    val from: Int
)

class PullRequestsService {
    val integrations = MutableStateFlow<List<Integration>>(emptyList())

    private val client = HttpClient {
        install(Logging)
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun applyBitbucketApiKey(apiKey: String, id: String) : Flow<Progress> = flow {
        integrations.value = integrations.value.toMutableList().apply {
            if (!any { it.id == id }) {
                add(Integration(id, apiKey, emptyList()))
            }
        }

        var integration = integrations.value.first { it.id == id }

        emit(Progress(0, 0))

        var result = client.get("https://api.bitbucket.org/2.0/repositories/$id/pullrequests") {
            url {
                parameters.append("state", "\"\"")
                parameters.append("pagelen", "50")
            }
            headers {
                append(HttpHeaders.Authorization, "Basic $apiKey")
                append(HttpHeaders.Accept, "application/json")
            }
        }.body<PullRequestResponse>()

        integration = integration.copy(pullRequests = result.values)
        integrations.value = integrations.value.toMutableList().apply {
            removeAll { it.id == id }
            add(integration)
        }

        emit(Progress(integration.pullRequests.size, result.size))

        while (result.next != null) {
            result = client.get(result.next!!) {
                headers {
                    append(HttpHeaders.Authorization, "Basic $apiKey")
                    append(HttpHeaders.Accept, "application/json")
                }
            }.body<PullRequestResponse>()

            integration = integration.copy(pullRequests = integration.pullRequests + result.values)
            integrations.value = integrations.value.toMutableList().apply {
                removeAll { it.id == id }
                add(integration)
            }

            emit(Progress(integration.pullRequests.size, result.size))
        }
    }.flowOn(Dispatchers.Default)

    suspend fun deleteBitbucketApiKey(id: String) {
        integrations.value = integrations.value.toMutableList().apply {
            removeAll { it.id == id }
        }
    }
}