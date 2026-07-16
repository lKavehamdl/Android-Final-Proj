-- =============================================================================
-- Melodify – Database Functions, Realtime & Helper View (FIXED)
-- File: 03_functions_and_realtime.sql
-- =============================================================================


-- SECTION 1: get_conversation
CREATE OR REPLACE FUNCTION public.get_conversation(
    user1_id uuid,
    user2_id uuid
)
RETURNS TABLE (
    id           uuid,
    sender_id    uuid,
    receiver_id  uuid,
    content      text,
    status       text,
    created_at   timestamptz
)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT
        m.id,
        m.sender_id,
        m.receiver_id,
        m.content,
        m.status,
        m.created_at
    FROM public.messages m
    WHERE
        (m.sender_id = user1_id   AND m.receiver_id = user2_id)
        OR
        (m.sender_id = user2_id   AND m.receiver_id = user1_id)
    ORDER BY m.created_at ASC;
$$;


-- SECTION 2: get_trending_songs
CREATE OR REPLACE FUNCTION public.get_trending_songs(
    limit_count integer DEFAULT 20
)
RETURNS TABLE (
    id               uuid,
    title            text,
    artist_name      text,
    album_name       text,
    duration_seconds integer,
    cover_image_url  text,
    audio_url        text,
    play_count       bigint,
    created_at       timestamptz
)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT
        s.id,
        s.title,
        s.artist_name,
        s.album_name,
        s.duration_seconds,
        s.cover_image_url,
        s.audio_url,
        s.play_count,
        s.created_at
    FROM public.songs s
    ORDER BY s.play_count DESC
    LIMIT GREATEST(1, limit_count);
$$;


-- SECTION 3: get_new_releases
CREATE OR REPLACE FUNCTION public.get_new_releases(
    limit_count integer DEFAULT 20
)
RETURNS TABLE (
    id               uuid,
    title            text,
    artist_name      text,
    album_name       text,
    duration_seconds integer,
    cover_image_url  text,
    audio_url        text,
    play_count       bigint,
    created_at       timestamptz
)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT
        s.id,
        s.title,
        s.artist_name,
        s.album_name,
        s.duration_seconds,
        s.cover_image_url,
        s.audio_url,
        s.play_count,
        s.created_at
    FROM public.songs s
    ORDER BY s.created_at DESC
    LIMIT GREATEST(1, limit_count);
$$;


-- SECTION 4: search_songs
CREATE OR REPLACE FUNCTION public.search_songs(
    search_query text
)
RETURNS TABLE (
    id               uuid,
    title            text,
    artist_name      text,
    album_name       text,
    duration_seconds integer,
    cover_image_url  text,
    audio_url        text,
    play_count       bigint,
    created_at       timestamptz
)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT
        s.id,
        s.title,
        s.artist_name,
        s.album_name,
        s.duration_seconds,
        s.cover_image_url,
        s.audio_url,
        s.play_count,
        s.created_at
    FROM public.songs s
    WHERE
        s.title       ILIKE '%' || search_query || '%'
        OR s.artist_name ILIKE '%' || search_query || '%'
    ORDER BY
        (s.title ILIKE search_query)        DESC,
        (s.artist_name ILIKE search_query)  DESC,
        s.play_count                        DESC;
$$;


-- SECTION 5: toggle_like
CREATE OR REPLACE FUNCTION public.toggle_like(
    p_user_id uuid,
    p_song_id uuid
)
RETURNS boolean
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    already_liked boolean;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM public.liked_songs
        WHERE user_id = p_user_id AND song_id = p_song_id
    ) INTO already_liked;

    IF already_liked THEN
        DELETE FROM public.liked_songs
        WHERE user_id = p_user_id AND song_id = p_song_id;
        RETURN false;
    ELSE
        INSERT INTO public.liked_songs (user_id, song_id)
        VALUES (p_user_id, p_song_id)
        ON CONFLICT (user_id, song_id) DO NOTHING;
        RETURN true;
    END IF;
END;
$$;


-- SECTION 6: follow_user
CREATE OR REPLACE FUNCTION public.follow_user(
    p_follower_id  uuid,
    p_following_id uuid
)
RETURNS boolean
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    already_following boolean;
BEGIN
    IF p_follower_id = p_following_id THEN
        RAISE EXCEPTION 'A user cannot follow themselves.';
    END IF;

    SELECT EXISTS (
        SELECT 1 FROM public.follows
        WHERE follower_id = p_follower_id AND following_id = p_following_id
    ) INTO already_following;

    IF already_following THEN
        DELETE FROM public.follows
        WHERE follower_id = p_follower_id AND following_id = p_following_id;
        RETURN false;
    ELSE
        INSERT INTO public.follows (follower_id, following_id)
        VALUES (p_follower_id, p_following_id)
        ON CONFLICT (follower_id, following_id) DO NOTHING;
        RETURN true;
    END IF;
END;
$$;


-- SECTION 7: Helper view – v_songs_with_stats
CREATE OR REPLACE VIEW public.v_songs_with_stats AS
SELECT
    s.id,
    s.title,
    s.artist_name,
    s.album_name,
    s.duration_seconds,
    s.cover_image_url,
    s.audio_url,
    s.play_count,
    s.created_at,
    COALESCE(lk.like_count, 0)  AS like_count
FROM public.songs s
LEFT JOIN (
    SELECT
        song_id,
        COUNT(*) AS like_count
    FROM public.liked_songs
    GROUP BY song_id
) lk ON lk.song_id = s.id;

GRANT SELECT ON public.v_songs_with_stats TO anon, authenticated;


-- SECTION 8: Enable Realtime for messages
ALTER PUBLICATION supabase_realtime ADD TABLE public.messages;