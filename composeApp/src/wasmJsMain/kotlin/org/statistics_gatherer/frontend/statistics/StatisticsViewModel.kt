package org.statistics_gatherer.frontend.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
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

data class DropDownItem(
    val id: String,
    var selected: Boolean
)

class StatisticsViewModel(
    private val pullRequestService: PullRequestsService
): ViewModel() {
    private val _allByYear: MutableStateFlow<AllStatisticsState> = MutableStateFlow(AllStatisticsState(emptyList()))
    val allByYear: StateFlow<AllStatisticsState> get() = _allByYear

    private val _userPullRequests: MutableStateFlow<List<UserPullRequests>> = MutableStateFlow(emptyList())
    val userPullRequests: StateFlow<List<UserPullRequests>> get() = _userPullRequests

    val userPRsDropDownItems: MutableStateFlow<List<DropDownItem>> = MutableStateFlow(emptyList())
    val allPRsDropDownItems: MutableStateFlow<List<DropDownItem>> = MutableStateFlow(emptyList())

    @OptIn(FlowPreview::class)
    fun initViewModel() {
        viewModelScope.launch {
            pullRequestService.integrations
                .sample(5000)
                .collect { integrations ->
                    update(integrations)
            }
        }
    }

    fun selectKeyForAllPRs(key: String?) {
        allPRsDropDownItems.value = allPRsDropDownItems.value.map { item ->
            DropDownItem(
                item.id,
                if (item.id == key) !item.selected else item.selected
            )
        }

        update(pullRequestService.integrations.value)
    }

    fun selectKeyForUserPRs(key: String?) {
        userPRsDropDownItems.value = pullRequestService.integrations.value.map { integration ->
            DropDownItem(
                integration.id,
                integration.id == key
            )
        }
        update(pullRequestService.integrations.value)
    }

    private fun update(integrations: List<Integration>) {
        calculateAllPullRequests(integrations)

        calculateUserPullRequests(integrations)
    }

    private fun calculateAllPullRequests(integrations: List<Integration>) {
        allPRsDropDownItems.value = integrations.map { integration ->
            DropDownItem(
                integration.id,
                allPRsDropDownItems.value.firstOrNull { it.id == integration.id }?.selected == true
            )
        }

        val years = HashSet<Int>()

        integrations.forEach { integration ->
            integration.pullRequests.forEach { pullRequest ->
                years.add(pullRequest.year)
            }
        }

        val map: MutableMap<String, Map<Int, Int>> = integrations
//            .filter { integration -> allPRsDropDownItems.value.any { it.id == integration.id && it.selected } }
            .associate { integration ->
            val allByYear = mutableMapOf<Int, Int>()

            years.forEach {
                allByYear[it] = 0
            }

            integration.pullRequests.forEach { pullRequest ->
                allByYear[pullRequest.year] = allByYear[pullRequest.year]!! + 1
            }

            Pair(integration.id, allByYear)
        }.toMutableMap()

        val all = mutableMapOf<Int, Int>()

        map.forEach {
            it.value.forEach { (year, count) ->
                all[year] = (all[year] ?: 0) + count
            }
        }

        map["all"] = all

        allPRsDropDownItems.value.filter { !it.selected }.forEach {
            map.remove(it.id)
        }

        val allByYear = mutableMapOf<Int, MutableMap<String, Int>>()
        years.forEach { year ->
            allByYear[year] = mutableMapOf()
        }

        map.forEach { (key, value) ->
            value.forEach { (year, count) ->
                allByYear[year]!![key] = count
            }
        }

        _allByYear.value = AllStatisticsState(
            years.map { year ->
                AllStatisticsState.Year(
                    year,
                    allByYear[year]!!.toList().sortedBy { it.first }
                )
            }.sortedBy { it.year }
        )
    }

    private fun calculateUserPullRequests(integrations: List<Integration>) {
        val selectedItem = userPRsDropDownItems.value.firstOrNull { it.selected }

        userPRsDropDownItems.value = integrations.map { integration ->
            DropDownItem(
                integration.id,
                false
            )
        }

        if (integrations.any { it.id == selectedItem?.id }) {
            userPRsDropDownItems.value = userPRsDropDownItems.value.map { item ->
                DropDownItem(
                    item.id,
                    item.id == selectedItem?.id
                )
            }
        } else {
            userPRsDropDownItems.value = userPRsDropDownItems.value.toMutableList().apply {
                firstOrNull()?.selected = true
            }
        }

        val selectedId = userPRsDropDownItems.value.firstOrNull { it.selected }?.id

        val yearsByKey = HashSet<Int>()

        val pullRequests = integrations.first { it.id == selectedId }.pullRequests

        val users = mutableMapOf<String, MutableMap<Int, Int>>()

        pullRequests.forEach { pullRequest ->
            val user = pullRequest.author.displayName
            users[user] = users[user] ?: mutableMapOf()
            users[user]!![pullRequest.year] = (users[user]!![pullRequest.year] ?: 0) + 1
        }

        users.forEach {
            yearsByKey.forEach { year ->
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