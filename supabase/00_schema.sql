-- =============================================================================
-- Melodify – Supabase PostgreSQL Schema
-- File: 00_schema.sql
-- Description: Core database schema for the Melodify music streaming app.
--              Includes all tables, indexes, RLS policies foundation,
--              triggers, and helper functions.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- EXTENSIONS
-- ---------------------------------------------------------------------------

-- Enable uuid-ossp so we can call uuid_generate_v4() in default values
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


-- =============================================================================
-- TABLE: profiles
-- Extends auth.users – one row per authenticated user.
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.profiles (
    id              uuid        PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username        text        NOT NULL UNIQUE,
    display_name    text,
    avatar_url      text,
    bio             text,
    is_premium      boolean     NOT NULL DEFAULT false,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.profiles IS 'Public user profile data, extending Supabase auth.users.';
COMMENT ON COLUMN public.profiles.id         IS 'Matches auth.users.id exactly.';
COMMENT ON COLUMN public.profiles.username   IS 'Unique, URL-safe handle chosen by the user.';
COMMENT ON COLUMN public.profiles.is_premium IS 'Whether the user has an active premium subscription.';


-- =============================================================================
-- TABLE: songs
-- Central catalogue of all tracks available in Melodify.
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.songs (
    id               uuid        PRIMARY KEY DEFAULT uuid_generate_v4(),
    title            text        NOT NULL,
    artist_name      text        NOT NULL,
    album_name       text,
    cover_image_url  text,
    audio_url        text        NOT NULL,
    duration_seconds integer,
    genre            text,
    -- is_local = TRUE means this is an Iranian / local-market track
    is_local         boolean     NOT NULL DEFAULT false,
    play_count       bigint      NOT NULL DEFAULT 0,
    created_at       timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.songs IS 'Master catalogue of all songs available in Melodify.';
COMMENT ON COLUMN public.songs.is_local    IS 'TRUE for Iranian/Persian local-market songs, FALSE for international tracks.';
COMMENT ON COLUMN public.songs.play_count  IS 'Cumulative number of times this song has been played across all users.';
COMMENT ON COLUMN public.songs.audio_url   IS 'Publicly accessible URL to the audio file (CDN or Supabase Storage).';


-- =============================================================================
-- TABLE: playlists
-- User-created collections of songs.
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.playlists (
    id              uuid        PRIMARY KEY DEFAULT uuid_generate_v4(),
    -- user_id is nullable so seed data can create "global" system playlists
    user_id         uuid        REFERENCES public.profiles(id) ON DELETE SET NULL,
    name            text        NOT NULL,
    description     text,
    cover_image_url text,
    is_public       boolean     NOT NULL DEFAULT false,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.playlists IS 'User-created (or system) playlists grouping one or more songs.';
COMMENT ON COLUMN public.playlists.user_id   IS 'Owner of the playlist; NULL for system/global playlists.';
COMMENT ON COLUMN public.playlists.is_public IS 'When TRUE, any authenticated user can view this playlist.';


-- =============================================================================
-- TABLE: playlist_songs
-- Junction table linking songs to playlists with ordering support.
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.playlist_songs (
    id          uuid        PRIMARY KEY DEFAULT uuid_generate_v4(),
    playlist_id uuid        NOT NULL REFERENCES public.playlists(id) ON DELETE CASCADE,
    song_id     uuid        NOT NULL REFERENCES public.songs(id)     ON DELETE CASCADE,
    -- 0-based or 1-based ordering index within the playlist
    position    integer     NOT NULL,
    added_at    timestamptz NOT NULL DEFAULT now(),

    -- A song can only appear once per playlist
    CONSTRAINT uq_playlist_song UNIQUE (playlist_id, song_id)
);

COMMENT ON TABLE  public.playlist_songs IS 'Ordered mapping between playlists and songs.';
COMMENT ON COLUMN public.playlist_songs.position IS 'Sort order of the song within its playlist (ascending).';


-- =============================================================================
-- TABLE: liked_songs
-- Tracks which songs a user has "liked" (favourited).
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.liked_songs (
    user_id  uuid        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    song_id  uuid        NOT NULL REFERENCES public.songs(id)    ON DELETE CASCADE,
    liked_at timestamptz NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, song_id)
);

COMMENT ON TABLE public.liked_songs IS 'Per-user list of liked/favourited songs.';


-- =============================================================================
-- TABLE: recently_played
-- Rolling history of tracks a user has played.
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.recently_played (
    id        uuid        PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id   uuid        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    song_id   uuid        NOT NULL REFERENCES public.songs(id)    ON DELETE CASCADE,
    played_at timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE public.recently_played IS 'Time-ordered playback history per user. Multiple rows per song are intentional.';


-- =============================================================================
-- TABLE: follows
-- Directed social graph – follower → following.
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.follows (
    follower_id  uuid        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    following_id uuid        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at   timestamptz NOT NULL DEFAULT now(),

    PRIMARY KEY (follower_id, following_id),
    -- A user cannot follow themselves
    CONSTRAINT chk_no_self_follow CHECK (follower_id <> following_id)
);

COMMENT ON TABLE  public.follows IS 'Social follow graph. Each row means follower_id follows following_id.';
COMMENT ON COLUMN public.follows.follower_id  IS 'The user who initiated the follow.';
COMMENT ON COLUMN public.follows.following_id IS 'The user being followed.';


-- =============================================================================
-- TABLE: messages
-- Direct messages between users, supporting text and song-share message types.
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.messages (
    id           uuid        PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id    uuid        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    receiver_id  uuid        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    content      text,
    -- 'text' = plain message, 'song' = song-share message
    message_type text        NOT NULL DEFAULT 'text'
                             CHECK (message_type IN ('text', 'song')),
    -- Only populated when message_type = 'song'
    song_id      uuid        REFERENCES public.songs(id) ON DELETE SET NULL,
    -- Delivery / read-receipt status
    status       text        NOT NULL DEFAULT 'sent'
                             CHECK (status IN ('sending', 'sent', 'read')),
    created_at   timestamptz NOT NULL DEFAULT now(),

    -- Prevent messaging yourself
    CONSTRAINT chk_no_self_message CHECK (sender_id <> receiver_id)
);

COMMENT ON TABLE  public.messages IS 'Direct messages between users; supports plain text and song-share cards.';
COMMENT ON COLUMN public.messages.message_type IS '''text'' for plain DMs, ''song'' for shared song cards.';
COMMENT ON COLUMN public.messages.song_id      IS 'Referenced song when message_type = ''song''; NULL for text messages.';
COMMENT ON COLUMN public.messages.status       IS 'Delivery status: sending → sent → read.';


-- =============================================================================
-- TABLE: downloads
-- Tracks which songs a user has downloaded for offline playback.
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.downloads (
    id               uuid        PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id          uuid        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    song_id          uuid        NOT NULL REFERENCES public.songs(id)    ON DELETE CASCADE,
    -- Device-side local file path (stored by the Android client)
    local_path       text,
    file_size_bytes  bigint,
    downloaded_at    timestamptz NOT NULL DEFAULT now(),

    -- Each song can only be downloaded once per user account
    CONSTRAINT uq_user_song_download UNIQUE (user_id, song_id)
);

COMMENT ON TABLE  public.downloads IS 'Records of songs downloaded by users for offline listening.';
COMMENT ON COLUMN public.downloads.local_path IS 'Absolute path to the cached file on the user''s device.';


-- =============================================================================
-- TABLE: search_history
-- Stores recent search queries per user for autocomplete / history UI.
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.search_history (
    id          uuid        PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     uuid        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    query       text        NOT NULL,
    searched_at timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE public.search_history IS 'Per-user search query history for autocomplete and history display.';


-- =============================================================================
-- INDEXES
-- Covering the most common query patterns to keep reads fast.
-- =============================================================================

-- songs – filter by genre (genre browser)
CREATE INDEX IF NOT EXISTS idx_songs_genre
    ON public.songs (genre);

-- songs – filter local (Iranian) vs international music
CREATE INDEX IF NOT EXISTS idx_songs_is_local
    ON public.songs (is_local);

-- songs – search / filter by artist
CREATE INDEX IF NOT EXISTS idx_songs_artist_name
    ON public.songs (artist_name);

-- songs – order by popularity
CREATE INDEX IF NOT EXISTS idx_songs_play_count
    ON public.songs (play_count DESC);

-- messages – load conversation between two users efficiently
CREATE INDEX IF NOT EXISTS idx_messages_sender_receiver
    ON public.messages (sender_id, receiver_id);

-- messages – show most-recent messages first
CREATE INDEX IF NOT EXISTS idx_messages_created_at
    ON public.messages (created_at DESC);

-- recently_played – fetch a user's history ordered by recency
CREATE INDEX IF NOT EXISTS idx_recently_played_user_played
    ON public.recently_played (user_id, played_at DESC);

-- liked_songs – fetch all liked songs for a user quickly
CREATE INDEX IF NOT EXISTS idx_liked_songs_user
    ON public.liked_songs (user_id);

-- playlist_songs – ordered song list for a given playlist
CREATE INDEX IF NOT EXISTS idx_playlist_songs_playlist_position
    ON public.playlist_songs (playlist_id, position);

-- playlists – all playlists owned by a user
CREATE INDEX IF NOT EXISTS idx_playlists_user_id
    ON public.playlists (user_id);

-- search_history – most recent searches for a user
CREATE INDEX IF NOT EXISTS idx_search_history_user_searched
    ON public.search_history (user_id, searched_at DESC);


-- =============================================================================
-- TRIGGER: handle_new_user
-- Automatically creates a profiles row whenever a new user signs up via
-- Supabase Auth (insert into auth.users).
-- =============================================================================

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
-- The function runs with the privileges of its owner (postgres) so it can
-- write to public.profiles regardless of RLS policies.
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.profiles (id, username, display_name, avatar_url)
    VALUES (
        NEW.id,
        -- Derive a default username from the email address (before the @)
        -- The user can update this later from their profile settings.
        COALESCE(
            NEW.raw_user_meta_data->>'username',
            split_part(NEW.email, '@', 1)
        ),
        -- Optional display name from OAuth metadata (e.g., Google full name)
        NEW.raw_user_meta_data->>'full_name',
        -- Avatar from OAuth provider (e.g., Google profile picture)
        NEW.raw_user_meta_data->>'avatar_url'
    )
    ON CONFLICT (id) DO NOTHING; -- Idempotent in case of duplicate signals

    RETURN NEW;
END;
$$;

-- Attach the function to auth.users as an AFTER INSERT trigger
CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

COMMENT ON FUNCTION public.handle_new_user() IS
    'Trigger function: auto-creates a public.profiles row for every new Supabase Auth user.';


-- =============================================================================
-- TRIGGER: handle_updated_at
-- Keeps the updated_at column current on profiles and playlists.
-- =============================================================================

CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER trg_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.set_updated_at();

CREATE OR REPLACE TRIGGER trg_playlists_updated_at
    BEFORE UPDATE ON public.playlists
    FOR EACH ROW
    EXECUTE FUNCTION public.set_updated_at();

COMMENT ON FUNCTION public.set_updated_at() IS
    'Generic trigger function that sets updated_at = now() before any UPDATE.';


-- =============================================================================
-- FUNCTION: increment_play_count
-- Safely increments the play_count for a given song using an atomic UPDATE.
-- Call this from the Android client via a Supabase RPC (rpc('increment_play_count', ...)).
-- =============================================================================

CREATE OR REPLACE FUNCTION public.increment_play_count(p_song_id uuid)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    UPDATE public.songs
    SET    play_count = play_count + 1
    WHERE  id = p_song_id;

    -- Silently do nothing if the song id is not found (avoids client-side errors)
    IF NOT FOUND THEN
        RAISE NOTICE 'increment_play_count: song % not found, skipping.', p_song_id;
    END IF;
END;
$$;

COMMENT ON FUNCTION public.increment_play_count(uuid) IS
    'Atomically increments songs.play_count by 1 for the given song ID. '
    'Call via Supabase RPC: supabase.rpc("increment_play_count", {"p_song_id": "<uuid>"})';


-- =============================================================================
-- ROW LEVEL SECURITY (RLS) – enable on all user-facing tables.
-- Actual policies should be added per-feature; enabling RLS here is a safe
-- default so no data is accidentally exposed before policies are written.
-- =============================================================================

ALTER TABLE public.profiles        ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.songs           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.playlists       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.playlist_songs  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.liked_songs     ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.recently_played ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follows         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages        ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.downloads       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.search_history  ENABLE ROW LEVEL SECURITY;

-- ---------------------------------------------------------------------------
-- Basic RLS policies
-- ---------------------------------------------------------------------------

-- profiles: anyone authenticated can read all profiles (for social features)
CREATE POLICY "profiles_select_authenticated"
    ON public.profiles FOR SELECT
    USING (auth.role() = 'authenticated');

-- profiles: users can only update their own profile
CREATE POLICY "profiles_update_own"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

-- songs: all authenticated users can browse the catalogue
CREATE POLICY "songs_select_authenticated"
    ON public.songs FOR SELECT
    USING (auth.role() = 'authenticated');

-- playlists: public playlists visible to all; private only to owner
CREATE POLICY "playlists_select_public_or_own"
    ON public.playlists FOR SELECT
    USING (is_public = true OR auth.uid() = user_id);

CREATE POLICY "playlists_insert_own"
    ON public.playlists FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "playlists_update_own"
    ON public.playlists FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "playlists_delete_own"
    ON public.playlists FOR DELETE
    USING (auth.uid() = user_id);

-- playlist_songs: visible if the parent playlist is visible
CREATE POLICY "playlist_songs_select"
    ON public.playlist_songs FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.playlists p
            WHERE p.id = playlist_id
              AND (p.is_public = true OR p.user_id = auth.uid())
        )
    );

-- liked_songs: users manage only their own liked songs
CREATE POLICY "liked_songs_own"
    ON public.liked_songs FOR ALL
    USING (auth.uid() = user_id);

-- recently_played: users see only their own history
CREATE POLICY "recently_played_own"
    ON public.recently_played FOR ALL
    USING (auth.uid() = user_id);

-- follows: anyone can read follows (social graph); write own rows only
CREATE POLICY "follows_select_authenticated"
    ON public.follows FOR SELECT
    USING (auth.role() = 'authenticated');

CREATE POLICY "follows_insert_own"
    ON public.follows FOR INSERT
    WITH CHECK (auth.uid() = follower_id);

CREATE POLICY "follows_delete_own"
    ON public.follows FOR DELETE
    USING (auth.uid() = follower_id);

-- messages: only sender or receiver can read a message
CREATE POLICY "messages_select_own"
    ON public.messages FOR SELECT
    USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

CREATE POLICY "messages_insert_own"
    ON public.messages FOR INSERT
    WITH CHECK (auth.uid() = sender_id);

-- downloads: users manage only their own downloads
CREATE POLICY "downloads_own"
    ON public.downloads FOR ALL
    USING (auth.uid() = user_id);

-- search_history: users see only their own history
CREATE POLICY "search_history_own"
    ON public.search_history FOR ALL
    USING (auth.uid() = user_id);

-- =============================================================================
-- END OF SCHEMA
-- =============================================================================
