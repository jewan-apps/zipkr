package com.jewan.zipkr.ui.search

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.ui.components.AddressResultCard
import com.jewan.zipkr.ui.components.EmptyState
import com.jewan.zipkr.ui.components.ErrorView
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.util.copyToClipboard
import com.jewan.zipkr.util.lightHaptic

// 리스트 끝에서 이 숫자만큼 앞서 다음 page를 미리 fetch해 UX를 부드럽게 한다.
private const val PREFETCH_THRESHOLD = 5

/**
 * 검색 메인 화면이다.
 * - 진입 시 입력창 자동 포커스 + 키보드 자동 표시 (즉시성).
 * - 결과 카드 우측 복사 버튼은 우편번호만 클립보드 복사 + 토스트 + 햅틱.
 * - 리스트 끝 도달 시 무한 스크롤로 다음 page를 자동 fetch한다.
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(ZipkrSpacing.md),
        ) {
            SearchBar(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = {
                    viewModel.searchNow()
                    keyboard?.hide()
                },
                focus = focus,
            )
            SearchBody(
                phase = state.phase,
                onCopyZip = { zipCode ->
                    context.copyToClipboard(zipLabel, zipCode)
                    view.lightHaptic()
                    // Android 13+ (API 33+)는 시스템이 자동으로 클립보드 토스트를 띄우므로 중복 알림을 막는다.
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        Toast.makeText(context, copyToastTemplate.format(zipCode), Toast.LENGTH_SHORT).show()
                    }
                },
                onCardClick = onCardClick,
                onRetry = { viewModel.searchNow() },
                onLoadMore = viewModel::loadMore,
            )
        }
    }
}

/**
 * AppError type을 strings.xml 리소스 키로 매핑한다.
 * ViewModel은 type만 보내고, 본 함수에서 사용자 가시 문자열을 결정해 i18n 시 strings-en.xml 추가만으로 EN 대응이 끝난다.
 */
private fun errorMessageRes(error: AppError): Int =
    when (error) {
        is AppError.Network -> R.string.error_network
        is AppError.Unknown -> R.string.error_unknown
        is AppError.ApiBadResponse -> apiBadResponseMessageRes(error)
    }

/**
 * ApiBadResponse errorCode를 세부 strings.xml 키로 매핑한다.
 * cyclomatic complexity 분산을 위해 별도 함수로 추출하며,
 * boolean predicate 순서 의존을 피해 code 자체를 switch한다 (방어적).
 */
private fun apiBadResponseMessageRes(error: AppError.ApiBadResponse): Int =
    when (error.code) {
        in AppError.ApiBadResponse.AUTH_ERROR_CODES -> R.string.error_auth
        AppError.ApiBadResponse.QUERY_TOO_BROAD_CODE -> R.string.error_query_too_broad
        AppError.ApiBadResponse.QUERY_TOO_SHORT_CODE -> R.string.error_query_too_short
        AppError.ApiBadResponse.NUMERIC_ONLY_CODE -> R.string.error_numeric_only
        in AppError.ApiBadResponse.INVALID_QUERY_CODES -> R.string.error_invalid_query
        AppError.ApiBadResponse.EMPTY_QUERY_CODE -> R.string.error_empty_query
        AppError.ApiBadResponse.PATH_ERROR_CODE -> R.string.error_path
        else -> R.string.error_api // fallback: 명세 외 코드이다.
    }

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    focus: FocusRequester,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(focus),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // 키보드 검색 버튼은 debounce를 기다리지 않고 즉시 검색 + 키보드 닫기 (즉시성).
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        // 비어있을 때는 X 버튼을 숨겨 시각 노이즈를 줄인다.
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.clear_query_action),
                    )
                }
            }
        },
    )
}

@Composable
private fun SearchBody(
    phase: SearchUiState.Phase,
    onCopyZip: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
) {
    when (phase) {
        SearchUiState.Phase.Idle ->
            EmptyState(
                title = stringResource(R.string.empty_title),
                description = stringResource(R.string.empty_description),
            )
        SearchUiState.Phase.Loading ->
            // 매 입력마다 큰 스켈레톤 카드가 깜빡이면 노이즈가 된다. 작은 스피너로 대체한다.
            Column(
                modifier = Modifier.fillMaxWidth().padding(ZipkrSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        SearchUiState.Phase.Empty ->
            EmptyState(
                title = stringResource(R.string.empty_results_title),
                description = stringResource(R.string.empty_results_description),
            )
        SearchUiState.Phase.PostalCodeUnsupported ->
            EmptyState(
                title = stringResource(R.string.postal_code_unsupported_title),
                description = stringResource(R.string.postal_code_unsupported_description),
            )
        is SearchUiState.Phase.Error ->
            ErrorView(
                message = stringResource(errorMessageRes(phase.error)),
                onRetry = onRetry,
            )
        is SearchUiState.Phase.Success ->
            SearchResultsList(
                phase = phase,
                onCopyZip = onCopyZip,
                onCardClick = onCardClick,
                onLoadMore = onLoadMore,
            )
    }
}

/**
 * 검색 결과 리스트이다.
 * 무한 스크롤: 마지막 아이템에서 PREFETCH_THRESHOLD번째 전 시점에 onLoadMore를 호출한다.
 * isLoadingMore 상태에서는 하단에 작은 스피너를 추가한다.
 */
@Composable
private fun SearchResultsList(
    phase: SearchUiState.Phase.Success,
    onCopyZip: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        items(phase.results) { address ->
            AddressResultCard(
                address = address,
                onCardClick = { onCardClick(address.zipCode) },
                onCopyZip = { onCopyZip(address.zipCode) },
            )
        }
        if (phase.isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(ZipkrSpacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }

    // 마지막 행에서 PREFETCH_THRESHOLD번째 전부터 다음 page를 미리 fetch한다 (UX 부드러움).
    val shouldLoadMore by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            phase.hasNext && !phase.isLoadingMore && lastVisible >= phase.results.size - PREFETCH_THRESHOLD
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }
}
