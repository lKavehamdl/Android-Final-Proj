# Melodify — Shared UI Components (LOCKED)

> **Phase 3 · Step 1 foundation**  
> Do **not** change these signatures without team agreement.  
> Import from `com.example.final_project.ui.components`.

## Navigation shell

`MelodifyApp` provides:

| Piece | Notes |
|-------|--------|
| `MelodifyTopBar` | Logo/name + notifications + settings + profile avatar |
| 5-tab `NavigationBar` | `home`, `search`, `downloads`, `playlists`, `profile` |
| `miniPlayer` slot | Optional composable above bottom nav — **Step 2 fills this** |

```kotlin
@Composable
fun MelodifyApp(
    themeMode: ThemeMode,
    language: AppLanguage,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    miniPlayer: @Composable (() -> Unit)? = null, // Step 2
)
```

Routes: see `ui/navigation/Screen.kt`.

---

## Locked composables

### `MelodifyTopBar`

```kotlin
@Composable
fun MelodifyTopBar(
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
)
```

### `ShimmerEffect` / `ShimmerBox`

```kotlin
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
)

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp = 120.dp,
    height: Dp = 16.dp,
    shape: Shape = MaterialTheme.shapes.medium,
)
```

### `CoverArt` (Coil)

```kotlin
@Composable
fun CoverArt(
    imageUrl: String?,
    contentDescription: String?,
    size: Dp,
    modifier: Modifier = Modifier,
)
```

### `SongCard` — horizontal LazyRow tiles

```kotlin
@Composable
fun SongCard(
    song: Song,
    onClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
)
```

### `SongRow` — vertical lists

```kotlin
@Composable
fun SongRow(
    song: Song,
    onClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
    onMoreClick: ((Song) -> Unit)? = null,
)
```

### `SectionHeader`

```kotlin
@Composable
fun SectionHeader(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    onSeeAllClick: (() -> Unit)? = null,
)
```

### `EmptyState`

```kotlin
@Composable
fun EmptyState(
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    @StringRes actionLabelRes: Int? = null,
    onActionClick: (() -> Unit)? = null,
)
```

---

## Design tokens (required)

| Need | Use |
|------|-----|
| Colors | `MaterialTheme.colorScheme.*` |
| Type | `MaterialTheme.typography.*` |
| Shapes | `MaterialTheme.shapes.*` |
| Spacing | `MelodifySpacing.*` (`ui/theme/Spacing.kt`) |
| Text | `stringResource(R.string.*)` only |

**No hardcoded colors, dp paddings, or string literals in feature screens.**

---

## Example — Home section

```kotlin
SectionHeader(titleRes = R.string.home_section_trending, onSeeAllClick = { })
LazyRow(...) {
    items(songs) { song ->
        SongCard(song = song, onClick = viewModel::onSongClick)
    }
}
```

## Example — Search / Downloads list

```kotlin
SongRow(
    song = song,
    onClick = { /* play */ },
    onMoreClick = { /* menu */ },
)
```

## Example — loading

```kotlin
if (state.isLoading) {
    ShimmerBox(
        width = MelodifySpacing.songCardCover,
        height = MelodifySpacing.songCardCover,
    )
}
```

---

## Step 2 owners

| Owner | Builds on |
|-------|-----------|
| Home (carousel, quick actions, LazyRows) | `SectionHeader`, `SongCard`, `ShimmerBox`, `MelodifyTopBar` (already in shell) |
| Mini Player | `MelodifyApp(miniPlayer = { … })` |
| Now Playing + shared element | new screen route; wire after Mini Player |
