package team.bits.vanilla.fabric.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A class to model a lock that can be used to persistently lock/unlock features. Defaults to a locked state.
 */
public class ToggleableFeatureLock {
    private static final String UNLOCK_ERR = "An error occurred unlocking the %s lock. Is it already unlocked?";
    private static final String LOCK_ERR = "An error occurred locking the %s lock. Is it already locked?";

    private final String name;
    private final File unlockFile;

    public ToggleableFeatureLock(String name) {
        this.name = name;
        this.unlockFile = new File(name + ".unlock");
    }

    public void toggleLock() {
        if (isLocked()) {
            unlock();
        } else {
            lock();
        }
    }

    private void lock() {
        try {
            Files.delete(unlockFile.toPath());
        } catch (IOException ex) {
            throw new RuntimeException(String.format(LOCK_ERR, name));
        }
    }

    private void unlock() {
        try {
            Files.createFile(unlockFile.toPath());
        } catch (IOException ex) {
            throw new RuntimeException(String.format(UNLOCK_ERR, name));
        }
    }

    public boolean isLocked() {
        return !unlockFile.exists();
    }
}
