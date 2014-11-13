package net.RevTut.Skywars.listeners;

import net.RevTut.Skywars.Main;
import net.RevTut.Skywars.arena.Arena;
import net.RevTut.Skywars.libraries.nametag.NameTagAPI;
import net.RevTut.Skywars.libraries.tab.TabAPI;
import net.RevTut.Skywars.libraries.titles.TitleAPI;
import net.RevTut.Skywars.player.PlayerDat;
import net.RevTut.Skywars.utils.ScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;

/**
 * Player Join.
 *
 * <P>Controls the join event.</P>
 *
 * @author Joao Silva
 * @version 1.0
 */
public class PlayerJoin implements Listener {

    /** Main class */
    private final Main plugin;

    /**
     * Constructor of PlayerJoin
     *
     * @param plugin    main class
     */
    public PlayerJoin(final Main plugin) {
        this.plugin = plugin;
    }

    /**
     *  Takes care of a player when he joins. Create the PlayerDat of him and assign him to
     *  an existing arena. If needed it creates a new one.
     *
     * @param e     player join event
     * @see         PlayerJoinEvent
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        // MySQL Tasks
        final UUID uuid = p.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                // PlayerDat
                plugin.mysql.createPlayerDat(uuid);
                // Config Player
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        // Add to Arena
                        PlayerDat playerDat = PlayerDat.getPlayerDatByUUID(uuid);
                        if (playerDat == null) {
                            /** Send him to Hub. Error in playerDat */
                            return;
                        }
                        if (!Arena.addPlayer(playerDat)) {
                            /** Send him to Hub. No arena available */
                            return;
                        }
                        Arena arena = Arena.getArenaByPlayer(playerDat);
                        if (arena == null) {
                            /** Send him to Hub. Error in arena */
                            return;
                        }
                        // New Arena if Needed
                        if (Arena.getNumberAvailableArenas() <= 1) {
                            Arena.createNewArena();
                        }
                        // Tab List
                        TabAPI.setTab(p, plugin.tabTitle, plugin.tabFooter);
                        // ScoreBoard
                        ScoreBoard.showScoreBoard(p, arena);
                        // NameTag
                        Scoreboard board = ScoreBoard.getScoreBoardByPlayer(p.getUniqueId());
                        if (board != null)
                            NameTagAPI.setNameTag(board, p, true);
                    }
                });
            }
        });
    }
}
