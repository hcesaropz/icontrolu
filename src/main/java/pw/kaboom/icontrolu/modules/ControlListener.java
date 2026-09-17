package pw.kaboom.icontrolu.modules;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

final class ControlListener implements Listener {
    private final Control control;

    ControlListener(final Control control) {
        this.control = control;
    }

    @EventHandler
    void onPlayerQuit(final PlayerQuitEvent event) {
        // if player was a controller...
        control.stopControlling(event.getPlayer());

        // if they were a target...
        control.stopBeingControlled(event.getPlayer())
                .ifPresent(player -> player.sendMessage(Component.text("The player you were " +
                        "controlling has disconnected. You are invisible for 10 seconds.")));
    }

    @EventHandler
    void onAsyncChat(final AsyncChatEvent event) {
        if (isControlled(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        if (isControlled(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerGameModeChange(final PlayerGameModeChangeEvent event) {
        if (isControlled(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private boolean isControlled(final Player target) {
        return control.getController(target).isPresent();
    }
}
