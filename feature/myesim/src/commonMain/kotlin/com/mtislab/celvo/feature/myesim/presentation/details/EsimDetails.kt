package com.mtislab.celvo.feature.myesim.presentation.details

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EsimDetailsRoot(
    onBackClick: () -> Unit,
    onTopUpClick: (String) -> Unit,
    viewModel: EsimDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-shot events
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is EsimDetailsEvent.NavigateToTopUp -> onTopUpClick(event.esimId)
                is EsimDetailsEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is EsimDetailsEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    EsimDetailsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when (action) {
                EsimDetailsAction.BackClick -> onBackClick()
                else -> viewModel.onAction(action)
            }
        }
    )
}

@Composable
fun EsimDetailsScreen(
    state: EsimDetailsState,
    snackbarHostState: SnackbarHostState,
    onAction: (EsimDetailsAction) -> Unit,
) {
    // TODO: UI იმპლემენტაცია ფიგმის დიზაინის მიხედვით
    // ეს იქნება შემდეგი ეტაპი
}