# 🎵 Melodify

A music streaming and social Android app, built with Kotlin and Jetpack Compose for the Mobile Programming course (Spring 2026) at Amirkabir University of Technology. Users can stream and download songs, build playlists, follow other listeners and chat with them in real time. The backend is [Supabase](https://supabase.com).

## ✨ Features

- **Streaming:** background playback with Media3 (ExoPlayer and a media session service), a mini player and a full-screen *Now Playing* screen whose colors come from the cover art (Palette).
- **Home and search:** song feeds and paginated search with Paging 3.
- **Offline downloads:** songs are downloaded in the background with WorkManager and stored in a Room database for offline listening.
- **Library:** playlists, liked songs, recently played and search history.
- **Social:** user search, public profiles, follows and notifications.
- **Real-time chat:** direct messages delivered live through Supabase Realtime (Postgres change streams).
- **Accounts:** sign-up and login with Supabase Auth, with profile avatars in Supabase Storage.
- **Settings:** English and Persian (RTL) localization, plus light and dark themes, saved with DataStore.

## 🛠️ Tech Stack

| Area | Technology |
| --- | --- |
| Language and UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| Architecture | Clean Architecture (data / domain / UI layers), MVI with unidirectional data flow |
| Dependency injection | Koin |
| Async | Kotlin Coroutines and Flow |
| Networking | Ktor, kotlinx.serialization, Supabase Kotlin client |
| Local storage | Room, DataStore |
| Media | Media3 (ExoPlayer, MediaSession), Coil, Palette |
| Background work | WorkManager |
| Backend | Supabase: PostgreSQL with row-level security, Auth, Storage, Realtime |

## 🏗️ Architecture

```
com.example.final_project/
├── core/     # MVI base classes, Resource<T>, dispatchers, constants, locale
├── data/     # Room, DataStore, Supabase/Ktor services, DTOs, repository implementations
├── domain/   # Pure Kotlin models, repository interfaces and use cases
├── ui/       # Compose screens, ViewModels, theme, navigation, shared components
├── player/   # Playback service, controller, player state and cache
└── di/       # Koin modules
```

Every screen follows the same MVI contract: the UI sends a `UiEvent` to its ViewModel, which exposes a `UiState` as a `StateFlow` and one-time `UiEffect`s (navigation, snackbars) as a `Flow`.

More detail:
- [`ARCHITECTURE.md`](ARCHITECTURE.md): package rules, MVI template and the data layer
- [`COMPONENTS.md`](COMPONENTS.md): shared Compose components

## 🗄️ Backend (Supabase)

The database schema, row-level security policies and seed data are in [`supabase/`](supabase) as SQL scripts. Tables: `profiles`, `songs`, `playlists`, `playlist_songs`, `liked_songs`, `recently_played`, `follows`, `messages`, `downloads` and `search_history`.

The app uses Supabase's public (publishable) key, so access control is enforced on the database side by the row-level security policies in `02_policies.sql`.

## ⚙️ How to Run

**Requirements:** Android Studio (recent version), JDK 17, and an Android device or emulator running Android 7.0 (API 24) or later.

1. Clone the repository and open it in Android Studio:
   ```bash
   git clone https://github.com/lKavehamdl/Android-Final-Proj.git
   ```
2. Let Gradle sync, then run the `app` configuration.

The app connects to the team's Supabase project out of the box. To use your own:

1. Create a project at [supabase.com](https://supabase.com).
2. Run the scripts in `supabase/` in order (`00_schema.sql` to `06_presence_and_message_delete.sql`) in the SQL editor.
3. Replace `SUPABASE_URL` and `SUPABASE_ANON_KEY` in `app/build.gradle.kts` with your project's URL and publishable key.

To run the UI without a backend, set `USE_FAKE_SONGS` to `true` in `app/build.gradle.kts`.

## 👥 Team

- **Kaveh Ahmadi** ([@lKavehamdl](https://github.com/lKavehamdl)): 
- **Hossein Goodarzi** ([@amoogoodarz](https://github.com/amoogoodarz))
- Aref Abdi ([@Arefabdi77](https://github.com/arefabdi77))
