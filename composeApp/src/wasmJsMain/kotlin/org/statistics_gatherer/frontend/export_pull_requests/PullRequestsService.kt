package org.statistics_gatherer.frontend.export_pull_requests

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.internal.JSJoda.OffsetDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.modules.SerializersModule

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

object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        return OffsetDateTime.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.toString())
    }
}

class PullRequestsService {
    private val _apiKey = MutableStateFlow<String?>(null)
    private val _id = MutableStateFlow<String?>(null)

    private val _pullRequests = MutableStateFlow<List<PullRequest>>(emptyList())
    val pullRequests get() = _pullRequests

    private val client = HttpClient {
        install(Logging)
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun applyBitbucketApiKey(apiKey: String, id: String) : Flow<Progress> = flow {
        _apiKey.value = apiKey
        _id.value = id

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

        _pullRequests.emit(result.values)

        emit(Progress(_pullRequests.value.size, result.size))

        while (result.next != null) {
            result = client.get(result.next!!) {
                headers {
                    append(HttpHeaders.Authorization, "Basic $apiKey")
                    append(HttpHeaders.Accept, "application/json")
                }
            }.body<PullRequestResponse>()

            _pullRequests.emit(_pullRequests.value + result.values)

            emit(Progress(_pullRequests.value.size, result.size))
        }
    }.flowOn(Dispatchers.Default)

    suspend fun syncPullRequests() {
        window.alert("Coming soon:)")
    }

    suspend fun deleteBitbucketApiKey() {
        _apiKey.value = null
        _id.value = null

        _pullRequests.value = emptyList()
    }
}