package com.agaloth.townywild.tasks;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.scheduler.BukkitTask;

public class RemoveProtectedPlayerTask implements Runnable {
    private final UUID uuid;
    private final Set<UUID> protectedPlayers;
    private final Map<UUID, Long> scheduledRemovalTimes;
    private BukkitTask progressUpdaterTask;

    public RemoveProtectedPlayerTask(UUID uuid, Set<UUID> protectedPlayers, Map<UUID, Long> scheduledRemovalTimes) {
        this.uuid = uuid;
        this.protectedPlayers = protectedPlayers;
        this.scheduledRemovalTimes = scheduledRemovalTimes;
    }

    public void cancelProgressUpdater() {
        if (progressUpdaterTask != null) {
            progressUpdaterTask.cancel();
        }
    }

    public void run() {
        cancelProgressUpdater();
        return;
    }
}



