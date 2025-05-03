package org.statistics_gatherer.frontend.statistics

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.browser.window
import org.statistics_gatherer.frontend.pullRequestService

@Composable
fun rememberWindowSize(): State<Size> {
    val size = remember {
        mutableStateOf(
            Size(width = window.innerWidth.toFloat(), height = window.innerHeight.toFloat())
        )
    }

    DisposableEffect(Unit) {
        window.addEventListener("resize") {
            size.value = Size(
                width = window.innerWidth.toFloat(),
                height = window.innerHeight.toFloat()
            )
        }

        onDispose {
            window.removeEventListener("resize") {
                size.value = Size(
                    width = window.innerWidth.toFloat(),
                    height = window.innerHeight.toFloat()
                )
            }
        }
    }

    return size
}

@Composable
fun StatisticsView(
    viewModel: StatisticsViewModel = viewModel { StatisticsViewModel(pullRequestService = pullRequestService) }
) {
    LaunchedEffect(Unit) {
        viewModel.initViewModel()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp, top = 32.dp, bottom = 0.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Statistics",
            style = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
        )

        val scrollState = rememberScrollState(0)

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Text(
                text = "Pull Requests by Year",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(top = 32.dp)
            )

            val windowSize by rememberWindowSize()

            val allByYear by viewModel.allByYear.collectAsState()
            val userPullRequests by viewModel.userPullRequests.collectAsState()

            if (windowSize.width > 800) {
                Row {
                    AllPullRequestsByYearView(allByYear, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(32.dp))
                    UserPullRequestsByYearView(userPullRequests, Modifier.weight(1f))
                }
            } else {
                Column {
                    AllPullRequestsByYearView(allByYear, Modifier.height(400.dp))
                    Spacer(modifier = Modifier.height(32.dp))
                    UserPullRequestsByYearView(userPullRequests, Modifier.height(400.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
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
        val windowSize by rememberWindowSize()

        val bars by remember(allByYear, windowSize) {
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

            mutableStateOf(bars)
        }

        if (allByYear.isEmpty()) {
            Box(
                modifier = Modifier
                    .heightIn(min = 200.dp, max = 400.dp)
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            }

            return@Column
        }

        ColumnChart(
            modifier = Modifier
                .heightIn(min = 200.dp, max = 400.dp)
                .fillMaxSize(),
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
            SolidColor(Color(0xFFFFD700)),
            SolidColor(Color(0xFF00FF00)),
            SolidColor(Color(0xFF008B8B)),
            SolidColor(Color(0xFFFF4500)),
            SolidColor(Color(0xFF1E90FF)),
            SolidColor(Color(0xFFDA70D6)),
            SolidColor(Color(0xFFBDB76B)),
            SolidColor(Color(0xFF32CD32)),
            SolidColor(Color(0xAA317F43)),
            SolidColor(Color(0xFFFF1493)),
            SolidColor(Color(0xFFFF8C00)),
        )

        val windowSize by rememberWindowSize()

        val lines by remember(userPullRequests, windowSize) {
            val lines = userPullRequests.mapIndexed { index, value ->
                Line(
                    label = value.user,
                    values = value.pullRequests.map { it.count.toDouble() },
                    color = colors[index % colors.size],
                )
            }

            mutableStateOf(lines)
        }

        val years = userPullRequests.firstOrNull()
            ?.pullRequests
            ?.map { it.year.toString() } ?: emptyList()
        if (years.isEmpty()) {
            Box(
                modifier = Modifier
                    .heightIn(min = 200.dp, max = 400.dp)
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            }

            return@Column
        }

        LineChart(
            modifier = Modifier
                .heightIn(min = 200.dp, max = 400.dp)
                .fillMaxSize(),
            data = lines,
            curvedEdges = false,
            dotsProperties = DotProperties(
                enabled = true,
                radius = 2.dp,
                color = SolidColor(Color.White),
                strokeWidth = 2.dp,
                strokeColor = SolidColor(Color.Black)
            ),
            labelProperties = LabelProperties(
                enabled = true,
                labels = years,
            )
        )
    }
}