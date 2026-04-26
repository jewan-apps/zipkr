package com.jewan.zipkr.ui.search

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jewan.zipkr.R
import com.jewan.zipkr.ui.components.AddressResultCard
import com.jewan.zipkr.ui.components.EmptyState
import com.jewan.zipkr.ui.components.ErrorView
import com.jewan.zipkr.ui.components.LoadingSkeleton
import com.jewan.zipkr.util.copyToClipboard
import com.jewan.zipkr.util.lightHaptic

/**
 * 검색 메인 화면이다.
 * - 진입 시 입력창 자동 포커스 + 키보드 자동 표시 (즉시성).
 * - 결과 카드 우측 복사 버튼은 우편번호만 클립보드 복사 + 토스트 + 햅틱.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onCardClick: (zip: String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }
    val copyToastTemplate = stringResource(R.string.copy_toast)
    val zipLabel = stringResource(R.string.copy_zip_label)

    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
            SearchBody(
                phase = state.phase,
                onCopyZip = { zipCode ->
                    context.copyToClipboard(zipLabel, zipCode)
                    view.lightHaptic()
                    Toast.makeText(context, copyToastTemplate.format(zipCode), Toast.LENGTH_SHORT).show()
                },
                onCardClick = onCardClick,
                onRetry = { viewModel.searchNow() },
            )
        }
    }
}

@Composable
private fun SearchBody(
    phase: SearchUiState.Phase,
    onCopyZip: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    when (phase) {
        SearchUiState.Phase.Idle -> EmptyState(
            title = stringResource(R.string.empty_title),
            description = stringResource(R.string.empty_description),
        )
        SearchUiState.Phase.Loading -> LoadingSkeleton()
        SearchUiState.Phase.Empty -> EmptyState(
            title = stringResource(R.string.empty_results_title),
            description = stringResource(R.string.empty_results_description),
        )
        is SearchUiState.Phase.Error -> ErrorView(
            message = phase.message,
            onRetry = onRetry,
        )
        is SearchUiState.Phase.Success -> LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(phase.results) { address ->
                AddressResultCard(
                    address = address,
                    onCardClick = { onCardClick(address.zipCode) },
                    onCopyZip = { onCopyZip(address.zipCode) },
                )
            }
        }
    }
}
