package com.momenta.util;

/**
 * Automatic Backup: called after tasks/reminders/goals/finance entries are
 * reloaded (i.e. right after any change), this writes a fresh momenta.json
 * snapshot on a background thread. Failures are swallowed on purpose —
 * losing a backup write should never interrupt what the user is doing.
 */
public class BackupManager {

    public static void autoBackup(int userId) {
        AsyncUtil.run(
                () -> { JsonExporter.exportAll(userId); return null; },
                v -> { },
                error -> { /* best-effort — ignore */ }
        );
    }
}
