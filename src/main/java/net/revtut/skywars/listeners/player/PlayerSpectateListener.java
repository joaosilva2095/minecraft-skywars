package net.revtut.skywars.listeners.player;

import net.revtut.libraries.minecraft.bukkit.games.GameController;
import net.revtut.libraries.minecraft.bukkit.games.arena.Arena;
import net.revtut.libraries.minecraft.bukkit.games.events.player.PlayerSpectateArenaEvent;
import net.revtut.skywars.SkyWars;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Player Spectate Listener
 */
public class PlayerSpectateListener implements Listener {

    /**
     * Controls the player spectate arena event
     * @param event player spectate arena event
     */
    @EventHandler
    public void onSpectate(final PlayerSpectateArenaEvent event) {
        // Check if the arena belongs to this game
        final Arena arena = event.getArena();
        final GameController gameController = SkyWars.getInstance().getGameController();
        if(gameController == null || !gameController.hasArena(arena))
            return;

        // Change join message
        event.setJoinMessage(null);
    }
}