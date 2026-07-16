-- =============================================================================
-- Melodify – Row Level Security (RLS) Policies
-- File: 02_policies.sql
-- Run after 01_schema.sql
-- =============================================================================

-- =============================================================================
-- SECTION 1: Enable RLS on every table
-- =============================================================================

ALTER TABLE public.songs             ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles          ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.playlists         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.playlist_songs    ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.liked_songs       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.recently_played   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follows           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages          ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.downloads         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.search_history    ENABLE ROW LEVEL SECURITY;


-- =============================================================================
-- SECTION 2: Service-role bypass policies (all tables)
-- The service_role key bypasses RLS by default in Supabase, but these
-- explicit policies make intent clear and support custom service accounts.
-- =============================================================================

-- songs
CREATE POLICY "service_role_bypass_songs"
    ON public.songs FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- profiles
CREATE POLICY "service_role_bypass_profiles"
    ON public.profiles FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- playlists
CREATE POLICY "service_role_bypass_playlists"
    ON public.playlists FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- playlist_songs
CREATE POLICY "service_role_bypass_playlist_songs"
    ON public.playlist_songs FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- liked_songs
CREATE POLICY "service_role_bypass_liked_songs"
    ON public.liked_songs FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- recently_played
CREATE POLICY "service_role_bypass_recently_played"
    ON public.recently_played FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- follows
CREATE POLICY "service_role_bypass_follows"
    ON public.follows FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- messages
CREATE POLICY "service_role_bypass_messages"
    ON public.messages FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- downloads
CREATE POLICY "service_role_bypass_downloads"
    ON public.downloads FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- search_history
CREATE POLICY "service_role_bypass_search_history"
    ON public.search_history FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);


-- =============================================================================
-- SECTION 3: songs
-- • PUBLIC READ  – anyone (anon + authenticated) can read songs
-- • NO USER WRITE – only service_role may insert / update / delete
-- =============================================================================

CREATE POLICY "songs_public_read"
    ON public.songs FOR SELECT
    TO anon, authenticated
    USING (true);

-- No INSERT / UPDATE / DELETE policies for regular roles → denied by default.


-- =============================================================================
-- SECTION 4: profiles
-- • All authenticated users can read every profile
-- • A user can only UPDATE their own profile row
-- =============================================================================

CREATE POLICY "profiles_read_all"
    ON public.profiles FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "profiles_update_own"
    ON public.profiles FOR UPDATE
    TO authenticated
    USING      (auth.uid() = id)
    WITH CHECK (auth.uid() = id);


-- =============================================================================
-- SECTION 5: playlists
-- • Public playlists: readable by everyone (anon + authenticated)
-- • Private playlists: readable only by the owner
-- • Owner can INSERT, UPDATE, DELETE their own playlists
-- =============================================================================

CREATE POLICY "playlists_read_public"
    ON public.playlists FOR SELECT
    TO anon, authenticated
    USING (is_public = true);

CREATE POLICY "playlists_read_own_private"
    ON public.playlists FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "playlists_insert_own"
    ON public.playlists FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "playlists_update_own"
    ON public.playlists FOR UPDATE
    TO authenticated
    USING      (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "playlists_delete_own"
    ON public.playlists FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);


-- =============================================================================
-- SECTION 6: playlist_songs
-- • Readable if the parent playlist is readable by the requesting user
-- • Only the playlist owner can INSERT or DELETE entries
-- =============================================================================

CREATE POLICY "playlist_songs_read"
    ON public.playlist_songs FOR SELECT
    TO anon, authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.playlists p
            WHERE p.id = playlist_id
              AND (
                    p.is_public = true
                    OR p.user_id = auth.uid()
                  )
        )
    );

CREATE POLICY "playlist_songs_insert_by_owner"
    ON public.playlist_songs FOR INSERT
    TO authenticated
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.playlists p
            WHERE p.id = playlist_id
              AND p.user_id = auth.uid()
        )
    );

CREATE POLICY "playlist_songs_delete_by_owner"
    ON public.playlist_songs FOR DELETE
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.playlists p
            WHERE p.id = playlist_id
              AND p.user_id = auth.uid()
        )
    );


-- =============================================================================
-- SECTION 7: liked_songs
-- • Users can only see and manage their OWN liked songs
-- =============================================================================

CREATE POLICY "liked_songs_select_own"
    ON public.liked_songs FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "liked_songs_insert_own"
    ON public.liked_songs FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "liked_songs_delete_own"
    ON public.liked_songs FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);


-- =============================================================================
-- SECTION 8: recently_played
-- • Users can only see and manage their OWN listening history
-- =============================================================================

CREATE POLICY "recently_played_select_own"
    ON public.recently_played FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "recently_played_insert_own"
    ON public.recently_played FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "recently_played_update_own"
    ON public.recently_played FOR UPDATE
    TO authenticated
    USING      (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "recently_played_delete_own"
    ON public.recently_played FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);


-- =============================================================================
-- SECTION 9: follows
-- • Anyone can read follows (needed for public follower / following counts)
-- • Authenticated users can insert a follow where they are the follower
-- • Authenticated users can delete only their own follow rows
-- =============================================================================

CREATE POLICY "follows_read_all"
    ON public.follows FOR SELECT
    TO anon, authenticated
    USING (true);

CREATE POLICY "follows_insert_own"
    ON public.follows FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = follower_id);

CREATE POLICY "follows_delete_own"
    ON public.follows FOR DELETE
    TO authenticated
    USING (auth.uid() = follower_id);


-- =============================================================================
-- SECTION 10: messages
-- • Users can see messages where they are the sender OR receiver
-- • Users can INSERT only as the sender
-- • Users can UPDATE (e.g. mark as read) only messages where they are receiver
-- =============================================================================

CREATE POLICY "messages_select_participant"
    ON public.messages FOR SELECT
    TO authenticated
    USING (
        auth.uid() = sender_id
        OR auth.uid() = receiver_id
    );

CREATE POLICY "messages_insert_as_sender"
    ON public.messages FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = sender_id);

CREATE POLICY "messages_update_as_receiver"
    ON public.messages FOR UPDATE
    TO authenticated
    USING      (auth.uid() = receiver_id)
    WITH CHECK (auth.uid() = receiver_id);


-- =============================================================================
-- SECTION 11: downloads
-- • Users can only see and manage their OWN downloads
-- =============================================================================

CREATE POLICY "downloads_select_own"
    ON public.downloads FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "downloads_insert_own"
    ON public.downloads FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "downloads_update_own"
    ON public.downloads FOR UPDATE
    TO authenticated
    USING      (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "downloads_delete_own"
    ON public.downloads FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);


-- =============================================================================
-- SECTION 12: search_history
-- • Users can only see and manage their OWN search history
-- =============================================================================

CREATE POLICY "search_history_select_own"
    ON public.search_history FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "search_history_insert_own"
    ON public.search_history FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "search_history_delete_own"
    ON public.search_history FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);
