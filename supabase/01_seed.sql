-- =============================================================================
-- Melodify – Supabase Seed Data
-- File: 01_seed.sql
-- Description: Populates the songs catalogue with 55 tracks (35 international
--              + 20 Iranian/Persian), then creates 4 global playlists and
--              assigns 10 songs to each.
-- Run AFTER 00_schema.sql.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Temporarily allow writes to songs/playlists/playlist_songs from seed script.
-- In a fresh migration context this is fine; in production use service-role key.
-- ---------------------------------------------------------------------------


-- =============================================================================
-- SECTION 1 – SONGS (55 total)
--
-- audio_url cycles through SoundHelix-Song-1 … SoundHelix-Song-17
-- cover_image_url uses picsum.photos with a unique seed word per song
-- duration_seconds: realistic 180–280 s
-- play_count: simulated real-world numbers between 1 000 and 999 999
-- created_at: spread over the last ~2 years
-- =============================================================================

INSERT INTO public.songs (
    id,
    title,
    artist_name,
    album_name,
    cover_image_url,
    audio_url,
    duration_seconds,
    genre,
    is_local,
    play_count,
    created_at
) VALUES

-- ===========================================================================
-- INTERNATIONAL SONGS (35) – is_local = false
-- ===========================================================================

-- ---- Coldplay (5) ----------------------------------------------------------
(
    uuid_generate_v4(),
    'Yellow',
    'Coldplay',
    'Parachutes',
    'https://picsum.photos/seed/yellow/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
    268, 'Rock', false, 874523,
    NOW() - INTERVAL '22 months'
),
(
    uuid_generate_v4(),
    'Fix You',
    'Coldplay',
    'X&Y',
    'https://picsum.photos/seed/fixyou/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
    295, 'Rock', false, 941200,
    NOW() - INTERVAL '20 months'
),
(
    uuid_generate_v4(),
    'Clocks',
    'Coldplay',
    'A Rush of Blood to the Head',
    'https://picsum.photos/seed/clocks/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
    307, 'Rock', false, 763445,
    NOW() - INTERVAL '18 months'
),
(
    uuid_generate_v4(),
    'The Scientist',
    'Coldplay',
    'A Rush of Blood to the Head',
    'https://picsum.photos/seed/scientist/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
    312, 'Rock', false, 812034,
    NOW() - INTERVAL '17 months'
),
(
    uuid_generate_v4(),
    'Viva la Vida',
    'Coldplay',
    'Viva la Vida or Death and All His Friends',
    'https://picsum.photos/seed/vivlavida/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3',
    242, 'Rock', false, 999123,
    NOW() - INTERVAL '16 months'
),

-- ---- Ed Sheeran (5) --------------------------------------------------------
(
    uuid_generate_v4(),
    'Shape of You',
    'Ed Sheeran',
    'Divide',
    'https://picsum.photos/seed/shapeofyou/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3',
    234, 'Pop', false, 987654,
    NOW() - INTERVAL '15 months'
),
(
    uuid_generate_v4(),
    'Perfect',
    'Ed Sheeran',
    'Divide',
    'https://picsum.photos/seed/perfect/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3',
    263, 'Pop', false, 856321,
    NOW() - INTERVAL '14 months'
),
(
    uuid_generate_v4(),
    'Photograph',
    'Ed Sheeran',
    'X',
    'https://picsum.photos/seed/photograph/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3',
    258, 'Pop', false, 723410,
    NOW() - INTERVAL '13 months'
),
(
    uuid_generate_v4(),
    'Thinking Out Loud',
    'Ed Sheeran',
    'X',
    'https://picsum.photos/seed/thinkingout/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3',
    281, 'Pop', false, 645789,
    NOW() - INTERVAL '12 months'
),
(
    uuid_generate_v4(),
    'Castle on the Hill',
    'Ed Sheeran',
    'Divide',
    'https://picsum.photos/seed/castlehill/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3',
    261, 'Pop', false, 532100,
    NOW() - INTERVAL '11 months'
),

-- ---- Imagine Dragons (5) ---------------------------------------------------
(
    uuid_generate_v4(),
    'Believer',
    'Imagine Dragons',
    'Evolve',
    'https://picsum.photos/seed/believer/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3',
    204, 'Rock', false, 921034,
    NOW() - INTERVAL '10 months'
),
(
    uuid_generate_v4(),
    'Radioactive',
    'Imagine Dragons',
    'Night Visions',
    'https://picsum.photos/seed/radioactive/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3',
    187, 'Rock', false, 875000,
    NOW() - INTERVAL '9 months'
),
(
    uuid_generate_v4(),
    'Thunder',
    'Imagine Dragons',
    'Evolve',
    'https://picsum.photos/seed/thunder/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3',
    187, 'Rock', false, 741200,
    NOW() - INTERVAL '8 months'
),
(
    uuid_generate_v4(),
    'Enemy',
    'Imagine Dragons',
    'Mercury – Act 1',
    'https://picsum.photos/seed/enemy/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3',
    173, 'Rock', false, 812560,
    NOW() - INTERVAL '7 months'
),
(
    uuid_generate_v4(),
    'Natural',
    'Imagine Dragons',
    'Origins',
    'https://picsum.photos/seed/natural/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3',
    189, 'Rock', false, 698450,
    NOW() - INTERVAL '7 months'
),

-- ---- The Weeknd (4) --------------------------------------------------------
(
    uuid_generate_v4(),
    'Blinding Lights',
    'The Weeknd',
    'After Hours',
    'https://picsum.photos/seed/blindinglights/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3',
    200, 'R&B', false, 999999,
    NOW() - INTERVAL '6 months'
),
(
    uuid_generate_v4(),
    'Save Your Tears',
    'The Weeknd',
    'After Hours',
    'https://picsum.photos/seed/saveyourtears/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-17.mp3',
    215, 'R&B', false, 887654,
    NOW() - INTERVAL '6 months'
),
(
    uuid_generate_v4(),
    'Starboy',
    'The Weeknd',
    'Starboy',
    'https://picsum.photos/seed/starboy/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',  -- wraps back to 1
    230, 'R&B', false, 765432,
    NOW() - INTERVAL '5 months'
),
(
    uuid_generate_v4(),
    'Can''t Feel My Face',
    'The Weeknd',
    'Beauty Behind the Madness',
    'https://picsum.photos/seed/cantfeelmyface/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
    213, 'R&B', false, 654321,
    NOW() - INTERVAL '5 months'
),

-- ---- Dua Lipa (4) ----------------------------------------------------------
(
    uuid_generate_v4(),
    'Levitating',
    'Dua Lipa',
    'Future Nostalgia',
    'https://picsum.photos/seed/levitating/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
    203, 'Pop', false, 943100,
    NOW() - INTERVAL '4 months'
),
(
    uuid_generate_v4(),
    'Don''t Start Now',
    'Dua Lipa',
    'Future Nostalgia',
    'https://picsum.photos/seed/dontstartnow/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
    183, 'Pop', false, 871200,
    NOW() - INTERVAL '4 months'
),
(
    uuid_generate_v4(),
    'New Rules',
    'Dua Lipa',
    'Dua Lipa',
    'https://picsum.photos/seed/newrules/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3',
    209, 'Pop', false, 734560,
    NOW() - INTERVAL '3 months'
),
(
    uuid_generate_v4(),
    'Physical',
    'Dua Lipa',
    'Future Nostalgia',
    'https://picsum.photos/seed/physical/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3',
    194, 'Pop', false, 612890,
    NOW() - INTERVAL '3 months'
),

-- ---- Billie Eilish (3) -----------------------------------------------------
(
    uuid_generate_v4(),
    'Bad Guy',
    'Billie Eilish',
    'When We All Fall Asleep, Where Do We Go?',
    'https://picsum.photos/seed/badguy/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3',
    194, 'Pop', false, 912345,
    NOW() - INTERVAL '2 months'
),
(
    uuid_generate_v4(),
    'Happier Than Ever',
    'Billie Eilish',
    'Happier Than Ever',
    'https://picsum.photos/seed/happierthanever/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3',
    299, 'Pop', false, 723456,
    NOW() - INTERVAL '2 months'
),
(
    uuid_generate_v4(),
    'Ocean Eyes',
    'Billie Eilish',
    'Don''t Smile at Me',
    'https://picsum.photos/seed/oceaneyes/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3',
    200, 'Pop', false, 534567,
    NOW() - INTERVAL '1 month'
),

-- ---- Taylor Swift (3) ------------------------------------------------------
(
    uuid_generate_v4(),
    'Anti-Hero',
    'Taylor Swift',
    'Midnights',
    'https://picsum.photos/seed/antihero/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3',
    200, 'Pop', false, 997821,
    NOW() - INTERVAL '1 month'
),
(
    uuid_generate_v4(),
    'Shake It Off',
    'Taylor Swift',
    '1989',
    'https://picsum.photos/seed/shakeitoff/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3',
    219, 'Pop', false, 876543,
    NOW() - INTERVAL '3 weeks'
),
(
    uuid_generate_v4(),
    'Blank Space',
    'Taylor Swift',
    '1989',
    'https://picsum.photos/seed/blankspace/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3',
    231, 'Pop', false, 754321,
    NOW() - INTERVAL '2 weeks'
),

-- ---- Adele (3) -------------------------------------------------------------
(
    uuid_generate_v4(),
    'Rolling in the Deep',
    'Adele',
    '21',
    'https://picsum.photos/seed/rollingdeep/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3',
    228, 'Pop', false, 965432,
    NOW() - INTERVAL '2 weeks'
),
(
    uuid_generate_v4(),
    'Hello',
    'Adele',
    '25',
    'https://picsum.photos/seed/adelhello/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3',
    295, 'Pop', false, 934567,
    NOW() - INTERVAL '10 days'
),
(
    uuid_generate_v4(),
    'Someone Like You',
    'Adele',
    '21',
    'https://picsum.photos/seed/someonelikeyou/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3',
    285, 'Pop', false, 881234,
    NOW() - INTERVAL '9 days'
),

-- ---- Lewis Capaldi (2) -----------------------------------------------------
(
    uuid_generate_v4(),
    'Someone You Loved',
    'Lewis Capaldi',
    'Divinely Uninspired to a Hellish Extent',
    'https://picsum.photos/seed/someoneloved/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3',
    182, 'Pop', false, 812000,
    NOW() - INTERVAL '8 days'
),
(
    uuid_generate_v4(),
    'Before You Go',
    'Lewis Capaldi',
    'Divinely Uninspired to a Hellish Extent',
    'https://picsum.photos/seed/beforeyougo/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-17.mp3',
    215, 'Pop', false, 732100,
    NOW() - INTERVAL '7 days'
),

-- ---- The Chainsmokers (2) --------------------------------------------------
(
    uuid_generate_v4(),
    'Closer',
    'The Chainsmokers',
    'Memories...Do Not Open',
    'https://picsum.photos/seed/closer/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
    249, 'Electronic', false, 876543,
    NOW() - INTERVAL '6 days'
),
(
    uuid_generate_v4(),
    'Don''t Let Me Down',
    'The Chainsmokers',
    'Collage',
    'https://picsum.photos/seed/dontletmedown/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
    207, 'Electronic', false, 712345,
    NOW() - INTERVAL '5 days'
),

-- ===========================================================================
-- IRANIAN / PERSIAN SONGS (20) – is_local = true
-- ===========================================================================

-- ---- Ebi (3) ---------------------------------------------------------------
(
    uuid_generate_v4(),
    'Jomeh',
    'Ebi',
    'Jomeh',
    'https://picsum.photos/seed/jomeh/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
    245, 'Persian Pop', true, 654321,
    NOW() - INTERVAL '23 months'
),
(
    uuid_generate_v4(),
    'Ghesmat',
    'Ebi',
    'Ghesmat',
    'https://picsum.photos/seed/ghesmat/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
    218, 'Persian Pop', true, 543210,
    NOW() - INTERVAL '21 months'
),
(
    uuid_generate_v4(),
    'Royaye Shirin',
    'Ebi',
    'Royaye Shirin',
    'https://picsum.photos/seed/royayeshirin/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3',
    232, 'Persian Pop', true, 412000,
    NOW() - INTERVAL '19 months'
),

-- ---- Googoosh (3) ----------------------------------------------------------
(
    uuid_generate_v4(),
    'Talagh',
    'Googoosh',
    'Talagh',
    'https://picsum.photos/seed/talagh/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3',
    278, 'Persian Pop', true, 723456,
    NOW() - INTERVAL '18 months'
),
(
    uuid_generate_v4(),
    'Gharibeh-o-Meh',
    'Googoosh',
    'Gharibeh-o-Meh',
    'https://picsum.photos/seed/gharibeh/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3',
    256, 'Persian Pop', true, 612000,
    NOW() - INTERVAL '16 months'
),
(
    uuid_generate_v4(),
    'Dastan',
    'Googoosh',
    'Dastan',
    'https://picsum.photos/seed/dastan/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3',
    265, 'Persian Pop', true, 534000,
    NOW() - INTERVAL '15 months'
),

-- ---- Dariush (3) -----------------------------------------------------------
(
    uuid_generate_v4(),
    'Ey Iran',
    'Dariush',
    'Ey Iran',
    'https://picsum.photos/seed/eyiran/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3',
    198, 'Persian Classical', true, 891000,
    NOW() - INTERVAL '14 months'
),
(
    uuid_generate_v4(),
    'Nameh',
    'Dariush',
    'Nameh',
    'https://picsum.photos/seed/nameh/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3',
    223, 'Persian Pop', true, 456000,
    NOW() - INTERVAL '12 months'
),
(
    uuid_generate_v4(),
    'Tanhayi',
    'Dariush',
    'Tanhayi',
    'https://picsum.photos/seed/tanhayi/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3',
    241, 'Persian Pop', true, 378000,
    NOW() - INTERVAL '11 months'
),

-- ---- Shajarian (3) ---------------------------------------------------------
(
    uuid_generate_v4(),
    'Sepide',
    'Mohammad Reza Shajarian',
    'Sepide',
    'https://picsum.photos/seed/sepide/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3',
    312, 'Persian Classical', true, 567000,
    NOW() - INTERVAL '10 months'
),
(
    uuid_generate_v4(),
    'Mast-o-Moshtagh',
    'Mohammad Reza Shajarian',
    'Mast-o-Moshtagh',
    'https://picsum.photos/seed/mastomoshtagh/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3',
    428, 'Persian Classical', true, 489000,
    NOW() - INTERVAL '9 months'
),
(
    uuid_generate_v4(),
    'Morghe Sahar',
    'Mohammad Reza Shajarian',
    'Morghe Sahar',
    'https://picsum.photos/seed/morghesahar/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3',
    356, 'Persian Classical', true, 621000,
    NOW() - INTERVAL '8 months'
),

-- ---- Shahram Shabpareh (2) -------------------------------------------------
(
    uuid_generate_v4(),
    'Dokhtare Shoma',
    'Shahram Shabpareh',
    'Dokhtare Shoma',
    'https://picsum.photos/seed/dokhtareshoma/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3',
    214, 'Persian Pop', true, 312000,
    NOW() - INTERVAL '7 months'
),
(
    uuid_generate_v4(),
    'Dokhtar',
    'Shahram Shabpareh',
    'Dokhtar',
    'https://picsum.photos/seed/dokhtar/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3',
    228, 'Persian Pop', true, 275000,
    NOW() - INTERVAL '6 months'
),

-- ---- Moein (2) -------------------------------------------------------------
(
    uuid_generate_v4(),
    'Royaye Man',
    'Moein',
    'Royaye Man',
    'https://picsum.photos/seed/royayeman/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-17.mp3',
    247, 'Persian Pop', true, 298000,
    NOW() - INTERVAL '5 months'
),
(
    uuid_generate_v4(),
    'Tik Tik Tik',
    'Moein',
    'Tik Tik Tik',
    'https://picsum.photos/seed/tiktiktik/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
    195, 'Persian Pop', true, 214000,
    NOW() - INTERVAL '4 months'
),

-- ---- Hayedeh (2) -----------------------------------------------------------
(
    uuid_generate_v4(),
    'Ye Joor Dige',
    'Hayedeh',
    'Ye Joor Dige',
    'https://picsum.photos/seed/yejoordige/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
    263, 'Persian Pop', true, 334000,
    NOW() - INTERVAL '3 months'
),
(
    uuid_generate_v4(),
    'Mosafer',
    'Hayedeh',
    'Mosafer',
    'https://picsum.photos/seed/mosafer/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
    251, 'Persian Pop', true, 287000,
    NOW() - INTERVAL '2 months'
),

-- ---- Andy (2) --------------------------------------------------------------
(
    uuid_generate_v4(),
    'Bahar',
    'Andy',
    'Bahar',
    'https://picsum.photos/seed/baharsong/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
    224, 'Persian Pop', true, 198000,
    NOW() - INTERVAL '6 weeks'
),
(
    uuid_generate_v4(),
    'Delam Mikhad',
    'Andy',
    'Delam Mikhad',
    'https://picsum.photos/seed/delammikhad/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3',
    237, 'Persian Pop', true, 176000,
    NOW() - INTERVAL '5 weeks'
),

-- ---- Shadmehr Aghili (2) ---------------------------------------------------
(
    uuid_generate_v4(),
    'Eshgham',
    'Shadmehr Aghili',
    'Eshgham',
    'https://picsum.photos/seed/eshgham/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3',
    253, 'Persian Pop', true, 421000,
    NOW() - INTERVAL '4 weeks'
),
(
    uuid_generate_v4(),
    'Royat',
    'Shadmehr Aghili',
    'Royat',
    'https://picsum.photos/seed/royat/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3',
    268, 'Persian Pop', true, 387000,
    NOW() - INTERVAL '3 weeks'
),

-- ---- Viguen (1) ------------------------------------------------------------
(
    uuid_generate_v4(),
    'Jonoon',
    'Viguen',
    'Jonoon',
    'https://picsum.photos/seed/jonoon/300/300',
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3',
    219, 'Persian Pop', true, 143000,
    NOW() - INTERVAL '2 weeks'
);

-- Verify total song count (should be 55)
-- SELECT COUNT(*) FROM public.songs;


-- =============================================================================
-- SECTION 2 – GLOBAL SYSTEM PLAYLISTS (4)
--
-- user_id is NULL → these are Melodify curated playlists visible to all.
-- is_public = true so all authenticated users can browse them.
-- =============================================================================

INSERT INTO public.playlists (id, user_id, name, description, cover_image_url, is_public, created_at, updated_at)
VALUES
(
    'aaaaaaaa-0001-0001-0001-aaaaaaaaaaaa',
    NULL,
    'Top Global Hits',
    'The biggest international tracks everyone is playing right now.',
    'https://picsum.photos/seed/tophits/300/300',
    true,
    NOW() - INTERVAL '12 months',
    NOW() - INTERVAL '1 day'
),
(
    'aaaaaaaa-0002-0002-0002-aaaaaaaaaaaa',
    NULL,
    'Best of Iranian Music',
    'A curated journey through the golden era of Persian music.',
    'https://picsum.photos/seed/iranianmusic/300/300',
    true,
    NOW() - INTERVAL '11 months',
    NOW() - INTERVAL '2 days'
),
(
    'aaaaaaaa-0003-0003-0003-aaaaaaaaaaaa',
    NULL,
    'Chill Vibes',
    'Relax and unwind with these smooth, feel-good songs.',
    'https://picsum.photos/seed/chillvibes/300/300',
    true,
    NOW() - INTERVAL '10 months',
    NOW() - INTERVAL '3 days'
),
(
    'aaaaaaaa-0004-0004-0004-aaaaaaaaaaaa',
    NULL,
    'Rock Essentials',
    'Iconic rock anthems that defined a generation.',
    'https://picsum.photos/seed/rockessentials/300/300',
    true,
    NOW() - INTERVAL '9 months',
    NOW() - INTERVAL '4 days'
);


-- =============================================================================
-- SECTION 3 – PLAYLIST_SONGS
-- Assign 10 songs to each playlist by referencing songs via a subquery.
-- Using ORDER BY created_at to pick deterministic songs for each playlist.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Playlist 1: "Top Global Hits" – 10 most-played international songs
-- ---------------------------------------------------------------------------
INSERT INTO public.playlist_songs (id, playlist_id, song_id, position, added_at)
SELECT
    uuid_generate_v4(),
    'aaaaaaaa-0001-0001-0001-aaaaaaaaaaaa',
    s.id,
    ROW_NUMBER() OVER (ORDER BY s.play_count DESC) AS position,
    NOW() - INTERVAL '11 months'
FROM public.songs s
WHERE s.is_local = false
ORDER BY s.play_count DESC
LIMIT 10;


-- ---------------------------------------------------------------------------
-- Playlist 2: "Best of Iranian Music" – 10 most-played Iranian songs
-- ---------------------------------------------------------------------------
INSERT INTO public.playlist_songs (id, playlist_id, song_id, position, added_at)
SELECT
    uuid_generate_v4(),
    'aaaaaaaa-0002-0002-0002-aaaaaaaaaaaa',
    s.id,
    ROW_NUMBER() OVER (ORDER BY s.play_count DESC) AS position,
    NOW() - INTERVAL '10 months'
FROM public.songs s
WHERE s.is_local = true
ORDER BY s.play_count DESC
LIMIT 10;


-- ---------------------------------------------------------------------------
-- Playlist 3: "Chill Vibes" – 10 Pop/R&B songs with the longest duration
-- ---------------------------------------------------------------------------
INSERT INTO public.playlist_songs (id, playlist_id, song_id, position, added_at)
SELECT
    uuid_generate_v4(),
    'aaaaaaaa-0003-0003-0003-aaaaaaaaaaaa',
    s.id,
    ROW_NUMBER() OVER (ORDER BY s.duration_seconds DESC) AS position,
    NOW() - INTERVAL '9 months'
FROM public.songs s
WHERE s.genre IN ('Pop', 'R&B', 'Persian Pop')
ORDER BY s.duration_seconds DESC
LIMIT 10;


-- ---------------------------------------------------------------------------
-- Playlist 4: "Rock Essentials" – 10 Rock songs ordered by play count
-- ---------------------------------------------------------------------------
INSERT INTO public.playlist_songs (id, playlist_id, song_id, position, added_at)
SELECT
    uuid_generate_v4(),
    'aaaaaaaa-0004-0004-0004-aaaaaaaaaaaa',
    s.id,
    ROW_NUMBER() OVER (ORDER BY s.play_count DESC) AS position,
    NOW() - INTERVAL '8 months'
FROM public.songs s
WHERE s.genre = 'Rock'
ORDER BY s.play_count DESC
LIMIT 10;


-- =============================================================================
-- END OF SEED DATA
-- =============================================================================
