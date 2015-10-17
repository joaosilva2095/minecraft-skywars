package net.revtut.skywars.listeners.arena;

import net.revtut.libraries.minecraft.games.GameController;
import net.revtut.libraries.minecraft.games.arena.Arena;
import net.revtut.libraries.minecraft.games.events.arena.ArenaBlockPlaceEvent;
import net.revtut.libraries.minecraft.games.events.arena.ArenaBucketEmptyEvent;
import net.revtut.libraries.minecraft.games.player.PlayerData;
import net.revtut.libraries.minecraft.games.player.PlayerState;
import net.revtut.skywars.SkyWars;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Arena Bucket Empty Listener
 */
public class ArenaBucketEmptyListener implements Listener {

    /**
     * Controls the arena bucket empty event
     * @param event arena bucket empty event
     */
    @EventHandler
    public void onBucketEmpty(final ArenaBucketEmptyEvent event) {
        // Check if the arena belongs to this game
        final Arena arena = event.getArena();
        final GameController gameController = SkyWars.getInstance().getGameController();
        if(gameController == null || !gameController.hasArena(arena))
            return;

        final PlayerData player = event.getPlayer();

        // Block non live players
        if(player.getState() != PlayerState.ALIVE)
            event.setCancelled(true);
    }
}