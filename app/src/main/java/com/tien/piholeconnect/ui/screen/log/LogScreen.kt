package com.tien.piholeconnect.ui.screen.log

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tien.piholeconnect.ui.component.LogItem
import com.tien.piholeconnect.ui.component.SwipeToRefreshLayout
import com.tien.piholeconnect.util.showGenericPiHoleConnectionError
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    viewModel: LogViewModel = viewModel(),
    scaffoldState: ScaffoldState
) {
    val context = LocalContext.current
    var isRefreshing by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.viewModelScope.launch {
            viewModel.apply {
                refresh()
                error?.let {
                    scaffoldState.snackbarHostState.showGenericPiHoleConnectionError(context)
                }
            }
        }
    }

    if (viewModel.logs.count() == 0) return

    SwipeToRefreshLayout(
        refreshingState = isRefreshing,
        onRefresh = {
            viewModel.viewModelScope.launch {
                isRefreshing = true
                viewModel.apply {
                    refresh()
                    isRefreshing = false
                    error?.let {
                        scaffoldState.snackbarHostState.showGenericPiHoleConnectionError(context)
                    }
                }
            }
        }) {
        LazyColumn(modifier) {
            viewModel.logs.forEachIndexed { index, log ->
                item(key = index) {
                    LogItem(log)
                }
            }
        }
    }
}