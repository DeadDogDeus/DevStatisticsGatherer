package org.statistics_gatherer.frontend.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
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
    private val _allByYear: MutableStateFlow<List<PullRequestByYear>> = MutableStateFlow(listOf(
        PullRequestByYear(2020, 100),
        PullRequestByYear(2021, 200),
        PullRequestByYear(2022, 190),
        PullRequestByYear(2023, 300),
        PullRequestByYear(2024, 400),
        PullRequestByYear(2025, 100)
    ))

    val allByYear: List<PullRequestByYear> get() = _allByYear.value

    private val _userPullRequests: MutableStateFlow<List<UserPullRequests>> = MutableStateFlow(listOf(
        UserPullRequests("User1", listOf(
            PullRequestByYear(2020, 10),
            PullRequestByYear(2021, 20),
            PullRequestByYear(2022, 30),
            PullRequestByYear(2023, 40),
            PullRequestByYear(2024, 50),
            PullRequestByYear(2025, 0)
        )),
        UserPullRequests("User2", listOf(
            PullRequestByYear(2020, 15),
            PullRequestByYear(2021, 25),
            PullRequestByYear(2022, 35),
            PullRequestByYear(2023, 10),
            PullRequestByYear(2024, 17),
            PullRequestByYear(2025, 65)
        )),
        UserPullRequests("User3", listOf(
            PullRequestByYear(2020, 0),
            PullRequestByYear(2021, 0),
            PullRequestByYear(2022, 0),
            PullRequestByYear(2023, 0),
            PullRequestByYear(2024, 55),
            PullRequestByYear(2025, 65)
        ))
    ))
    val userPullRequests: List<UserPullRequests> get() = _userPullRequests.value

    fun initViewModel() {
        viewModelScope.launch {
            pullRequestService.pullRequests.collect { pullRequests ->
                val allByYear = mutableMapOf<Int, Int>()

                pullRequests.forEach { pullRequest ->
                    allByYear[pullRequest.year] = (allByYear[pullRequest.year] ?: 0) + 1
                }

                _allByYear.value = allByYear.map { (year, count) ->
                    PullRequestByYear(year, count)
                }.sortedBy { it.year }
            }
        }
    }
}