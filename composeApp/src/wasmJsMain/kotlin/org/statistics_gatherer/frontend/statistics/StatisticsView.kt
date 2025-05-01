package org.statistics_gatherer.frontend.statistics

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.internal.JSJoda.TextStyle

data class PullRequestByYear(
    val year: Int,
    val count: Int
)

data class UserPullRequests(
    val user: String,
    val pullRequests: List<PullRequestByYear>
)

class StatisticsViewModel: ViewModel() {
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
            PullRequestByYear(2023, 45),
            PullRequestByYear(2024, 55),
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
}

@Composable
fun StatisticsView(
    viewModel: StatisticsViewModel = viewModel { StatisticsViewModel() }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Statistics",
            style = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
        )

        Text(
            text = "Pull Requests by Year",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(top = 32.dp)
        )

        Row {
            AllPullRequestsByYearView(viewModel.allByYear, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(32.dp))
            UserPullRequestsByYearView(viewModel.userPullRequests, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AllPullRequestsByYearView(allByYear: List<PullRequestByYear>, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            text = "All",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(top = 32.dp)
        )
        val bars = allByYear.map {
            Bars(
                label = it.year.toString(),
                values = listOf(
                    Bars.Data(
                        value = it.count.toDouble(),
                        color = SolidColor(Color.Blue)
                    )
                ),
            )
        }

        ColumnChart(
            modifier = Modifier
                .heightIn(max = 300.dp)
                .fillMaxWidth(),
            data = bars,
            barProperties = BarProperties(
                cornerRadius = Bars.Data.Radius.Rectangle(topRight = 10.dp, topLeft = 10.dp),
                spacing = 4.dp,
                thickness = 20.dp
            ),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            labelHelperProperties = LabelHelperProperties(
                enabled = false
            ),
        )
    }
}

@Composable
private fun UserPullRequestsByYearView(userPullRequests: List<UserPullRequests>, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            text = "By User",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(top = 32.dp)
        )

        val colors = listOf(
            SolidColor(Color.Green),
            SolidColor(Color.Yellow),
            SolidColor(Color.Blue),
            SolidColor(Color.Cyan),
            SolidColor(Color.Magenta),
            SolidColor(Color.Gray),
            SolidColor(Color.Black)
        )

        val lines = userPullRequests.mapIndexed { index, value ->
            Line(
                label = value.user,
                values = value.pullRequests.map { it.count.toDouble() },
                color = colors[index % colors.size],
            )
        }

        val years = userPullRequests.firstOrNull()
            ?.pullRequests
            ?.map { it.year.toString() } ?: emptyList()

        LineChart(
            modifier = Modifier
                .heightIn(max = 300.dp)
                .fillMaxWidth(),
            data = lines,
            labelProperties = LabelProperties(
                enabled = true,
                labels = years,
            )
        )
    }
}