package pw.kaboom.icontrolu.modules;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import pw.kaboom.icontrolu.Main;

import java.util.Map;
import java.util.Optional;

public final class Control {
    private final Main plugin;
    private final BukkitScheduler scheduler;
    // K: controller, V: target
    private final BiMap<Player, Player> controllers = HashBiMap.create();
    private int taskNum;

    public Control(final Main plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    public void enable() {
        taskNum = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            for (final Map.Entry<Player, Player> entry : controllers.entrySet()) {
                tick(entry.getKey(), entry.getValue());
            }
        }, 0, 1);
    }

    public void disable() {
        scheduler.cancelTask(taskNum);
    }

    public Optional<Player> getController(final Player target) {
        return Optional.ofNullable(controllers.inverse().get(target));
    }

    public Optional<Player> getTarget(final Player controller) {
        return Optional.ofNullable(controllers.get(controller));
    }

    public void control(final Player controller, final Player target) {
        controller.teleportAsync(target.getLocation());
        controller.getInventory().setContents(
                target.getInventory().getContents()
        );
        controllers.put(controller, target);
    }

    public Optional<Player> stopControl(final Player controller) {
        return Optional.ofNullable(controllers.remove(controller));
    }

    private static void tick(final Player controller, final Player target) {
        target.setGameMode(GameMode.SPECTATOR);
        target.setSpectatorTarget(controller);
    }
}
