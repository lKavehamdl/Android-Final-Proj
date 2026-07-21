-- =============================================================================
-- STORAGE: avatars bucket
-- Profile pictures, uploaded by the owning user to "<user_id>/avatar.jpg".
-- Public read (avatars are shown to other users in chat/top bar); writes are
-- restricted to the owner via the first path segment matching their uid.
-- =============================================================================

INSERT INTO storage.buckets (id, name, public)
VALUES ('avatars', 'avatars', true)
ON CONFLICT (id) DO NOTHING;

CREATE POLICY "public_read_avatars" ON storage.objects
    FOR SELECT USING (bucket_id = 'avatars');

CREATE POLICY "owner_write_avatars" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'avatars'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

CREATE POLICY "owner_update_avatars" ON storage.objects
    FOR UPDATE USING (
        bucket_id = 'avatars'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );

CREATE POLICY "owner_delete_avatars" ON storage.objects
    FOR DELETE USING (
        bucket_id = 'avatars'
        AND (storage.foldername(name))[1] = auth.uid()::text
    );
