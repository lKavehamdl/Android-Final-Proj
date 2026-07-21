-- =============================================================================
-- Melodify – Presence + message deletion
-- File: 06_presence_and_message_delete.sql
-- Adds a last_seen column for online / last-seen status, and a DELETE policy so
-- users can delete their own messages.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- profiles.last_seen — updated by the client as a presence heartbeat.
-- ---------------------------------------------------------------------------
ALTER TABLE public.profiles
    ADD COLUMN IF NOT EXISTS last_seen timestamptz;

COMMENT ON COLUMN public.profiles.last_seen IS
    'Last time the user was active in the app; drives online / last-seen status.';

-- ---------------------------------------------------------------------------
-- messages: allow a user to delete their OWN messages.
-- (There was previously no DELETE policy, so deletes were silently rejected.)
-- ---------------------------------------------------------------------------
CREATE POLICY "messages_delete_own"
    ON public.messages FOR DELETE
    USING (auth.uid() = sender_id);
