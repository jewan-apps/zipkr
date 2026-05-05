package com.jewan.zipkr.ui.search

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jewan.zipkr.R
import com.jewan.zipkr.ads.AdBanner
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.ui.components.EmptyState
import com.jewan.zipkr.ui.components.ErrorView
import com.jewan.zipkr.ui.components.HistoryFavoritesPanel
import com.jewan.zipkr.ui.components.SidoAnchor
import com.jewan.zipkr.ui.components.SidoSelectorSheet
import com.jewan.zipkr.ui.detail.DetailSheet
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.util.copyToClipboard
import com.jewan.zipkr.util.lightHaptic

// SidoAnchor.ANCHOR_RADIUS와 동일한 값. 두 컴포넌트가 한 줄에서 같은 코너 곡률을 공유한다.
private val SEARCH_BAR_RADIUS = 14.dp

// 5자리 숫자(우편번호) 검색 분기용 — 빈 결과 메시지를 우편번호 톤으로 바꾸기 위해 사용한다.
// ViewModel의 POSTAL_CODE_PATTERN과 의미가 같으나 화면 레이어 책임 분리를 위해 별도로 둔다.
private val ZIP_QUERY_PATTERN = Regex("""\d{5}""")

/**
 * 진입 즉시 입력창에 포커스 + 키보드 노출을 자동 트리거하는 hook.
 * focus는 OutlinedTextField에 connect용으로, keyboard는 카드 탭/시트 열기 등 hide 용으로 호출자가 함께 사용한다.
 */
@Composable
private fun rememberAutoFocusKeyboard(): Pair<FocusRequester, SoftwareKeyboardController?> {
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }
    return focus to keyboard
}

/**
 * 검색 메인 화면이다.
 * - 진입 시 입력창 자동 포커스 + 키보드 자동 표시 (즉시성).
 * - 결과 카드 우측 복사 버튼은 우편번호만 클립보드 복사 + 토스트 + 햅틱.
 * - 리스트 끝 도달 시 무한 스크롤로 다음 page를 자동 fetch한다.
 * - 카드 탭 시 DetailSheet 모달을 띄운다 (네비 없이 화면 내부 sheet 패턴).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val (focus, keyboard) = rememberAutoFocusKeyboard()
    val copyToastTemplate = stringResource(R.string.copy_toast)

    // 시트 열림 상태는 순수 UI라 ViewModel이 아닌 화면 내 saveable로 관리한다 (Address는 @Parcelize).
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var detailSheetAddress by rememberSaveable { mutableStateOf<Address?>(null) }

    val callbacks =
        rememberSearchCallbacks(
            context = context,
            view = view,
            copyToastTemplate = copyToastTemplate,
            onCardClick = { addr ->
                keyboard?.hide()
                detailSheetAddress = addr
            },
            viewModel = viewModel,
            onOpenSheet = {
                keyboard?.hide()
                sheetOpen = true
            },
            onSubmitExtra = { keyboard?.hide() },
        )

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { padding ->
        SearchScreenContent(
            state = state,
            favorites = favorites,
            recent = recent,
            focus = focus,
            callbacks = callbacks,
            contentPadding = padding,
        )
    }

    SearchOverlays(
        state = state,
        sheetOpen = sheetOpen,
        detailAddress = detailSheetAddress,
        onSidoSelect = viewModel::onSidoChange,
        onSidoDismiss = { sheetOpen = false },
        onDetailDismiss = { detailSheetAddress = null },
    )
}

/**
 * 시·도 선택 시트와 상세 시트 두 모달을 묶은 overlay layer이다.
 * SearchScreen 본체 길이를 50 lines 이내로 유지하기 위해 분리했다.
 */
@Composable
private fun SearchOverlays(
    state: SearchUiState,
    sheetOpen: Boolean,
    detailAddress: Address?,
    onSidoSelect: (com.jewan.zipkr.data.Sido?) -> Unit,
    onSidoDismiss: () -> Unit,
    onDetailDismiss: () -> Unit,
) {
    if (sheetOpen) {
        SidoSelectorSheet(
            selected = state.selectedSido,
            onSelect = onSidoSelect,
            onDismiss = onSidoDismiss,
        )
    }
    detailAddress?.let { addr ->
        DetailSheet(
            address = addr,
            onDismiss = onDetailDismiss,
            query = state.query,
        )
    }
}

@Composable
private fun SearchScreenContent(
    state: SearchUiState,
    favorites: List<Address>,
    recent: List<Address>,
    focus: FocusRequester,
    callbacks: SearchCallbacks,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(ZipkrSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        // 시·도 앵커는 검색바와 같은 줄 좌측 prefix로 — 한 줄에 [지역 ▾] [입력창]을 묶어 흐름이 한 호흡으로 읽힌다.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
        ) {
            SidoAnchor(selected = state.selectedSido, onOpen = callbacks.onOpenSheet)
            SearchBar(
                query = state.query,
                onQueryChange = callbacks.onQueryChange,
                onSearch = callbacks.onSubmit,
                focus = focus,
                modifier = Modifier.weight(1f),
            )
        }
        // SearchBody가 fillMaxSize인 EmptyState 등을 가질 수 있어 weight(1f)로 영역 보장한다.
        // 그래야 마지막 자식 AdBanner가 바닥에 고정되어 그려진다.
        Box(modifier = Modifier.weight(1f)) {
            SearchBody(
                phase = state.phase,
                query = state.query,
                favorites = favorites,
                recent = recent,
                callbacks = callbacks,
            )
        }
        AdBanner()
    }
}

@Composable
private fun rememberSearchCallbacks(
    context: android.content.Context,
    view: android.view.View,
    copyToastTemplate: String,
    onCardClick: (Address) -> Unit,
    viewModel: SearchViewModel,
    onOpenSheet: () -> Unit,
    onSubmitExtra: () -> Unit,
): SearchCallbacks =
    remember(context, view, copyToastTemplate, onCardClick, viewModel, onOpenSheet, onSubmitExtra) {
        SearchCallbacks(
            onQueryChange = viewModel::onQueryChange,
            onSubmit = {
                viewModel.searchNow()
                onSubmitExtra()
            },
            onCopyAddress = { label, text ->
                context.copyToClipboard(label, text)
                view.lightHaptic()
                // Android 13+ (API 33+)는 시스템이 자동 클립보드 토스트를 띄우므로 중복을 막는다.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    Toast.makeText(context, copyToastTemplate.format(text), Toast.LENGTH_SHORT).show()
                }
            },
            onCardClick = onCardClick,
            onRetry = viewModel::searchNow,
            onLoadMore = viewModel::loadMore,
            onOpenSheet = onOpenSheet,
        )
    }

/**
 * 빈 결과 패널이다. 우편번호(5자리 숫자) 검색이면 "해당 우편번호의 주소가 없어요"로 분기해
 * 사용자 의도에 맞는 메시지를 노출한다.
 */
@Composable
private fun EmptyResultPanel(query: String) {
    val isZipQuery = query.matches(ZIP_QUERY_PATTERN)
    val titleRes = if (isZipQuery) R.string.empty_zip_results_title else R.string.empty_results_title
    val descRes = if (isZipQuery) R.string.empty_zip_results_description else R.string.empty_results_description
    EmptyState(
        title = stringResource(titleRes),
        description = stringResource(descRes),
    )
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
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        singleLine = true,
        // 시·도 앵커와 동일한 14dp radius로 통일 — 한 줄에서 한 컴포넌트로 읽히게 한다.
        shape = RoundedCornerShape(SEARCH_BAR_RADIUS),
        modifier = modifier.focusRequester(focus),
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

/**
 * 화면 액션 콜백 묶음이다.
 * 헌법 §1.7 인자 4개 룰을 만족하기 위해 data class로 묶고, 각 자식 컴포저블이 필요한 것만 사용한다.
 */
private data class SearchCallbacks(
    val onQueryChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onCopyAddress: (label: String, text: String) -> Unit,
    val onCardClick: (Address) -> Unit,
    val onRetry: () -> Unit,
    val onLoadMore: () -> Unit,
    val onOpenSheet: () -> Unit,
)

@Composable
private fun SearchBody(
    phase: SearchUiState.Phase,
    query: String,
    favorites: List<Address>,
    recent: List<Address>,
    callbacks: SearchCallbacks,
) {
    when (phase) {
        SearchUiState.Phase.Idle ->
            // 즐겨찾기/최근이 하나라도 있으면 칩 행 패널, 둘 다 비면 기본 EmptyState (첫 사용자는 변화 못 느낌).
            if (favorites.isNotEmpty() || recent.isNotEmpty()) {
                HistoryFavoritesPanel(
                    favorites = favorites,
                    recent = recent,
                    onAddressClick = callbacks.onCardClick,
                )
            } else {
                EmptyState(
                    title = stringResource(R.string.empty_title),
                    description = stringResource(R.string.empty_description),
                )
            }
        SearchUiState.Phase.Loading ->
            // 매 입력마다 큰 스켈레톤 카드가 깜빡이면 노이즈가 된다. 작은 스피너로 대체한다.
            Column(
                modifier = Modifier.fillMaxWidth().padding(ZipkrSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        SearchUiState.Phase.Empty -> EmptyResultPanel(query = query)
        is SearchUiState.Phase.Error ->
            ErrorView(
                message = stringResource(errorMessageRes(phase.error)),
                onRetry = callbacks.onRetry,
            )
        is SearchUiState.Phase.Success ->
            SearchResultsList(
                phase = phase,
                query = query,
                onCopyAddress = callbacks.onCopyAddress,
                onCardClick = callbacks.onCardClick,
                onLoadMore = callbacks.onLoadMore,
            )
    }
}
