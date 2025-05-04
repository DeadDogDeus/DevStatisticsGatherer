package org.statistics_gatherer.frontend.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import org.statistics_gatherer.frontend.export_pull_requests.PullRequestsService

data class PullRequestByYear(
    val year: Int,
    val count: Int
)

data class UserPullRequests(
    val user: String,
    val pullRequests: List<PullRequestByYear>
)

class StatisticsViewModel(
    private val pullRequestService: PullRequestsService
): ViewModel() {
    private val _allByYear: MutableStateFlow<List<PullRequestByYear>> = MutableStateFlow(emptyList())
    val allByYear: StateFlow<List<PullRequestByYear>> get() = _allByYear

    private val _userPullRequests: MutableStateFlow<List<UserPullRequests>> = MutableStateFlow(emptyList())
    val userPullRequests: StateFlow<List<UserPullRequests>> get() = _userPullRequests

    @OptIn(FlowPreview::class)
    fun initViewModel() {
        viewModelScope.launch {
            pullRequestService.integrations
                .sample(3000)
                .collect { integrations ->
                    val pullRequests = integrations.firstOrNull()?.pullRequests ?: emptyList()

                    val allByYear = mutableMapOf<Int, Int>()

                    pullRequests.forEach { pullRequest ->
                        allByYear[pullRequest.year] = (allByYear[pullRequest.year] ?: 0) + 1
                    }

                    _allByYear.value = allByYear.map { (year, count) ->
                        PullRequestByYear(year, count)
                    }.sortedBy { it.year }

                    val users = mutableMapOf<String, MutableMap<Int, Int>>()

                    pullRequests.forEach { pullRequest ->
                        val user = pullRequest.author.displayName
                        users[user] = users[user] ?: mutableMapOf()
                        users[user]!![pullRequest.year] = (users[user]!![pullRequest.year] ?: 0) + 1
                    }

                    users.forEach {
                        allByYear.keys.forEach { year ->
                            it.value[year] = it.value[year] ?: 0
                        }
                    }

                    _userPullRequests.value = users.map { (user, years) ->
                        UserPullRequests(
                            user,
                            years.map { (year, count) ->
                                PullRequestByYear(year, count)
                            }.sortedBy { it.year }
                        )
                    }.sortedBy { it.user }
            }
        }
    }
}