package com.fresh.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fresh.launcher.AppEntry
import com.fresh.launcher.FavoritesStore
import com.fresh.launcher.R
import com.fresh.launcher.normalizedForSearch

/** Filtra por nombre ignorando tildes; primero las que empiezan con la búsqueda. */
fun filterApps(apps: List<AppEntry>, query: String): List<AppEntry> {
    val q = query.trim().normalizedForSearch()
    if (q.isEmpty()) return apps
    val (starts, contains) = apps.filter { q in it.searchKey }.partition {
        it.searchKey.startsWith(q) || it.searchKey.split(' ').any { word -> word.startsWith(q) }
    }
    return starts + contains
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    apps: List<AppEntry>,
    query: String,
    onQueryChange: (String) -> Unit,
    favoriteKeys: List<String>,
    searchFocus: FocusRequester,
    dragConnection: NestedScrollConnection,
    onLaunch: (AppEntry) -> Unit,
    onToggleFavorite: (AppEntry) -> Unit,
    onInfo: (AppEntry) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLauncherColors.current
    val haptics = LocalHapticFeedback.current
    val results = remember(apps, query) { filterApps(apps, query) }
    var actionsKey by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val locale = LocalConfiguration.current.locales[0]

    Column(
        modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(colors.drawer)
            .statusBarsPadding()
            .imePadding()
            .nestedScroll(dragConnection),
    ) {
        // Asa: indica que el cajón se cierra arrastrando hacia abajo
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(PillShape)
                    .background(colors.onDrawer.copy(alpha = 0.18f)),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            BasicTextField(
                value = query,
                onValueChange = {
                    actionsKey = null
                    onQueryChange(it)
                },
                singleLine = true,
                textStyle = LauncherType.app.copy(color = colors.onDrawer),
                cursorBrush = SolidColor(if (colors.isDark) FreshLime else Ink),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(onGo = { results.firstOrNull()?.let(onLaunch) }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(searchFocus),
                decorationBox = { field ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .heightIn(min = 56.dp)
                            .clip(PillShape)
                            .background(colors.field)
                            .padding(horizontal = 22.dp),
                    ) {
                        if (query.isEmpty()) {
                            BasicText(
                                stringResource(R.string.search_apps),
                                style = LauncherType.app.copy(color = colors.onDrawerMuted),
                            )
                        }
                        field()
                    }
                },
            )

            // Configuración: mismo alto y fondo que el buscador, para que lean como un solo bloque
            val settingsLabel = stringResource(R.string.settings)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(PillShape)
                    .background(colors.field)
                    .clickable(onClickLabel = settingsLabel, role = Role.Button, onClick = onOpenSettings)
                    .semantics { contentDescription = settingsLabel },
            ) {
                SettingsIcon(color = colors.onDrawer, background = colors.field)
            }
        }

        if (results.isEmpty()) {
            BasicText(
                text = stringResource(R.string.no_results, query.trim()),
                style = LauncherType.body.copy(color = colors.onDrawerMuted),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            )
        }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            modifier = Modifier
                .weight(1f)
                .navigationBarsPadding(),
        ) {
            itemsIndexed(results, key = { _, app -> app.key }) { index, app ->
                val letter = app.label.firstOrNull()?.uppercase(locale) ?: ""
                val previous = results.getOrNull(index - 1)?.label?.firstOrNull()?.uppercase(locale)
                val showLetter = query.isBlank() && letter != previous
                val isFavorite = app.key in favoriteKeys

                Column(Modifier.animateItem()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .combinedClickable(
                                onClick = {
                                    actionsKey = null
                                    onLaunch(app)
                                },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    actionsKey = if (actionsKey == app.key) null else app.key
                                },
                                onLongClickLabel = stringResource(
                                    if (isFavorite) R.string.remove_from_home else R.string.add_to_home,
                                ),
                            )
                            .padding(horizontal = 24.dp),
                    ) {
                        // Índice alfabético: dato, por eso en mono
                        BasicText(
                            text = if (showLetter) letter else "",
                            style = LauncherType.data.copy(color = colors.onDrawerMuted),
                            modifier = Modifier.width(28.dp),
                        )
                        BasicText(
                            text = app.label,
                            style = LauncherType.app.copy(color = colors.onDrawer),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (isFavorite) {
                            Box(
                                Modifier
                                    .padding(start = 12.dp)
                                    .size(8.dp)
                                    .clip(PillShape)
                                    .background(if (colors.isDark) FreshLime else FreshGreen),
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = actionsKey == app.key,
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                    ) {
                        val full = !isFavorite && favoriteKeys.size >= FavoritesStore.MAX
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 52.dp, end = 24.dp, bottom = 12.dp),
                        ) {
                            Pill(
                                text = when {
                                    isFavorite -> stringResource(R.string.remove_from_home)
                                    full -> stringResource(R.string.home_full, favoriteKeys.size, FavoritesStore.MAX)
                                    else -> stringResource(R.string.add_to_home)
                                },
                                enabled = !full,
                                onClick = {
                                    actionsKey = null
                                    onToggleFavorite(app)
                                },
                            )
                            Pill(
                                text = stringResource(R.string.app_info),
                                filled = false,
                                onClick = {
                                    actionsKey = null
                                    onInfo(app)
                                },
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
