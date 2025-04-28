package org.devstatisticsgatherer.statistics

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class StatisticsViewModel: ViewModel() {

}

@Composable
fun StatisticsView(
    viewModel: StatisticsViewModel = viewModel { StatisticsViewModel() }
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "Statistics",
        style = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
    )
}