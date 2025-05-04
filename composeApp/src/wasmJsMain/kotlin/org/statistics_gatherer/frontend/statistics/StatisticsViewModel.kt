package org.statistics_gatherer.frontend.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import org.statistics_gatherer.frontend.export_pull_requests.Integration
import org.statistics_gatherer.frontend.export_pull_requests.PullRequestsService

data class PullRequestByYear(
    val year: Int,
    val count: Int
)

data class UserPullRequests(
    val user: String,
    val pullRequests: List<PullRequestByYear>
)

data class AllStatisticsState(
    val years: List<Year>,
) {
    data class Year(
        val year: Int,
        val counts: List<Pair<String, Int>>
    )
}

class StatisticsViewModel(
    private val pullRequestService: PullRequestsService
): ViewModel() {
    private val _allByYear: MutableStateFlow<AllStatisticsState> = MutableStateFlow(AllStatisticsState(emptyList()))
    val allByYear: StateFlow<AllStatisticsState> get() = _allByYear

    private val _userPullRequests: MutableStateFlow<List<UserPullRequests>> = MutableStateFlow(emptyList())
    val userPullRequests: StateFlow<List<UserPullRequests>> get() = _userPullRequests

    private val _keysForUserPRs: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val keysForUserPRs: StateFlow<List<String>> get() = _keysForUserPRs

    private val _selectedKeyForUserPRs: MutableStateFlow<String?> = MutableStateFlow(null)
    val selectedKeyForUserPRs: StateFlow<String?> get() = _selectedKeyForUserPRs

    @OptIn(FlowPreview::class)
    fun initViewModel() {
        viewModelScope.launch {
            pullRequestService.integrations
                .sample(3000)
                .collect { integrations ->
                    update(integrations)
            }
        }
    }

    fun selectKeyForUserPRs(key: String?) {
        _selectedKeyForUserPRs.value = key
        update(pullRequestService.integrations.value)
    }

    private fun update(integrations: List<Integration>) {
        _keysForUserPRs.value = integrations.map { it.id }
        _selectedKeyForUserPRs.value = if (integrations.any { it.id == _selectedKeyForUserPRs.value }) {
            _selectedKeyForUserPRs.value
        } else {
            integrations.firstOrNull()?.id
        }

        val years = HashSet<Int>()

        val allByYear = mutableMapOf<Int, Int>()

//        pullRequests.forEach { pullRequest ->
//            allByYear[pullRequest.year] = (allByYear[pullRequest.year] ?: 0) + 1
//            years.add(pullRequest.year)
//        }

//                    _allByYear.value = allByYear.map { (year, count) ->
//                        PullRequestByYear(year, count)
//                    }.sortedBy { it.year }

        val pullRequests = if (integrations.any { it.id == _selectedKeyForUserPRs.value }) {
            integrations.first { it.id == _selectedKeyForUserPRs.value }.pullRequests
        } else {
            _selectedKeyForUserPRs.value = null
            integrations.flatMap { it.pullRequests }
        }

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