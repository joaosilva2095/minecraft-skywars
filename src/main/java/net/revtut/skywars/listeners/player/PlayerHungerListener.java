package net.revtut.skywars.listeners.player;

import net.revtut.libraries.minecraft.bukkit.games.GameController;
import net.revtut.libraries.minecraft.bukkit.games.arena.Arena;
import net.revtut.libraries.minecraft.bukkit.games.events.player.PlayerHungerEvent;
import net.revtut.libraries.minecraft.bukkit.games.player.GamePlayer;
import net.revtut.libraries.minecraft.bukkit.games.player.PlayerState;
import net.revtut.skywars.SkyWars;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Player Hunger Listener
 */
public class PlayerHungerListener implements Listener {

    /**
     * Controls the player hunger event
     * @param event player hunger event
     */
    @EventHandler
    public void onHunger(final PlayerHungerEvent event) {
        // Check if the arena belongs to this game
        final Arena arena = event.getArena();
        final GameController gameController = SkyWars.getInstance().getGameController();
        if(gameController == null || !gameController.hasArena(arena))
            return;

        final GamePlayer player = event.getPlayer();

        // Block non live players
        if(player.getState() != PlayerState.ALIVE)
            event.setCancelled(true);
    }
}