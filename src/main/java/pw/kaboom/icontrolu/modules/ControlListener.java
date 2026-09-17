package pw.kaboom.icontrolu.modules;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

final class ControlListener implements Listener {
    private final Control control;

    ControlListener(final Control control) {
        this.control = control;
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
