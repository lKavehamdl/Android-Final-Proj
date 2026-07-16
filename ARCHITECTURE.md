# Melodify — Architecture Guide (Phase 0)

This document defines the conventions every team member must follow.

## Package Structure

```
com.example.final_project/
├── core/           # Shared utilities (no feature logic)
│   ├── mvi/        # UiState, UiEvent, UiEffect, BaseViewModel
│   ├── result/     # Resource<T> sealed class
│   ├── dispatchers/# DispatchersProvider (inject IO dispatcher)
│   ├── constants/  # App-wide constants
│   └── locale/     # AppLanguage enum
├── data/           # Data layer (Android/framework deps allowed)
│   ├── local/      # Room, DataStore
│   ├── remote/     # API clients, WebSocket, DTOs
│   ├── repository/ # Repository implementations
│   └── mapper/     # DTO ↔ Domain mappers
├── domain/         # Pure Kotlin — NO Android imports
│   ├── model/      # Domain models
│   ├── repository/ # Repository interfaces
│   └── usecase/    # Business logic UseCases
├── ui/             # Presentation layer
│   ├── theme/      # MaterialTheme (Person B)
│   ├── components/ # Shared composables
│   ├── navigation/ # NavHost, routes
│   └── screens/    # Feature screens (home, search, …)
└── di/             # Koin modules
```

## UI Pattern: MVI + UDF

We use **MVI (Model-View-Intent)** with **Unidirectional Data Flow**:

```
User Action → UiEvent → ViewModel.onEvent() → UiState (StateFlow) → Composable
                                              ↘ UiEffect (Flow) → Snackbar / Navigation
```

### Template for every screen

1. Define `XxxUiState`, `XxxUiEvent`, `XxxUiEffect`
2. Extend `BaseViewModel<XxxUiState, XxxUiEvent, XxxUiEffect>`
3. Collect state with `collectAsStateWithLifecycle()`
4. Collect effects in `LaunchedEffect` for one-time events

```kotlin
// 1. Contracts
data class HomeUiState(val isLoading: Boolean = false) : UiState
sealed interface HomeUiEvent : UiEvent { data object Refresh : HomeUiEvent }
sealed interface HomeUiEffect : UiEffect { data class ShowError(val msg: String) : HomeUiEffect }

// 2. ViewModel
class HomeViewModel(
    private val getSongsUseCase: GetSongsUseCase,
    private val dispatchers: DispatchersProvider,
) : BaseViewModel<HomeUiState, HomeUiEvent, HomeUiEffect>(HomeUiState()) {

    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Refresh -> loadSongs()
        }
    }

    private fun loadSongs() {
        viewModelScope.launch(dispatchers.io) {
            getSongsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> setState { copy(isLoading = true) }
                    is Resource.Success -> setState { copy(isLoading = false) }
                    is Resource.Error -> sendEffect(HomeUiEffect.ShowError(resource.message.orEmpty()))
                }
            }
        }
    }
}

// 3. Screen
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // render UI from state only — never mutate state in composables
}
```

## Dependency Injection (Koin)

Modules live in `di/`:

| Module | Responsibility |
|--------|----------------|
| `appModule` | Dispatchers, app-level ViewModels |
| `networkModule` | HTTP client, API services (Phase 1) |
| `databaseModule` | Room, DataStore (Phase 2) |
| `repositoryModule` | Repository bindings, UseCases |

Register new dependencies in the matching module. ViewModels use `viewModel { }` in Koin.

## Data Flow Rules

- **StateFlow** for UI state (survives config changes)
- **Channel → Flow** for one-time effects (snackbar, navigate)
- **Resource&lt;T&gt;** for async data: `Loading | Success | Error`
- **Dispatchers.IO** for all network/DB — inject via `DispatchersProvider`
- **Paging 3** for long lists (Phase 3+)

## Navigation

Bottom-nav routes are defined in `ui/navigation/Screen.kt`:

| Route | Screen |
|-------|--------|
| `home` | Home |
| `search` | Search |
| `downloads` | Downloads |
| `playlists` | Playlists |
| `profile` | Profile |

Add new routes to `Screen` sealed class and register in `MelodifyApp` NavHost.

## Phase 0 Verification Checklist

- [ ] App icon shows (custom adaptive icon)
- [ ] Bottom nav switches between 5 tabs
- [ ] Profile tab → theme chips switch dark/light/system
- [ ] Profile tab → language chips flip RTL (فارسی) / LTR (English)
- [ ] All text comes from `strings.xml` (no hardcoded strings)

## Phase 1 — Data Layer (Task C)

### Live backend (wired)

Credentials are hardcoded in `app/build.gradle.kts` → `BuildConfig` so any clone builds and runs:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://tumloddgamefuouqurww.supabase.co/\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"sb_publishable_...\"")
buildConfigField("boolean", "USE_FAKE_SONGS", "false")
```

`AppConstants` reads them via `BuildConfig`. Koin binds `SongRepositoryImpl` when `USE_FAKE_SONGS=false`.

### Files

| Layer | File | Role |
|-------|------|------|
| Domain | `domain/model/Song.kt` | Pure Kotlin model |
| Domain | `domain/repository/SongRepository.kt` | Interface |
| Domain | `domain/usecase/GetSongsUseCase.kt` | UseCase + `flowOn(IO)` |
| Data | `data/remote/dto/SongDto.kt` | `@Serializable` Supabase DTO |
| Data | `data/remote/SongApiService.kt` | Ktor GET `/rest/v1/songs` + RPCs |
| Data | `data/mapper/SongMapper.kt` | DTO → Domain |
| Data | `data/repository/FakeSongRepository.kt` | 5 hardcoded songs (dev fallback) |
| Data | `data/repository/SongRepositoryImpl.kt` | Live API repo |

### Expected Supabase schema (Task A)

```sql
songs (
  id uuid primary key,
  title text,
  artist_name text,
  album_name text,
  cover_image_url text,
  audio_url text,
  duration_seconds int,
  genre text,
  is_local boolean,
  play_count bigint,
  created_at timestamptz
)
```

### Phase 1 Verification

- [x] Task A: REST `/rest/v1/songs` returns data with publishable key
- [x] Task B: 50+ songs in DB
- [x] Task C: `USE_FAKE_SONGS=false` → Home loads live songs via Koin
