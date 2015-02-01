package net.revtut.skywars.arena.tasks;

import net.revtut.skywars.SkyWars;
import net.revtut.skywars.arena.Arena;
import net.revtut.skywars.arena.ArenaDat;
import net.revtut.skywars.arena.ArenaLocation;
import net.revtut.skywars.arena.ArenaStatus;
import net.revtut.skywars.player.PlayerDat;
import net.revtut.skywars.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.logging.Level;

/**
 * Arena Lobby Runnable.
 *
 * <P>Takes care of all the arenas which are on "Lobby".</P>
 *
 * @author Joao Silva
 * @version 1.0
 */
public class ArenaLobby implements Runnable {

    /**
     * Main class
     */
    private final SkyWars plugin;

    /**
     * Runnable ID
     */
    private static int id;

    /**
     * Constructor of ArenaLobby
     *
     * @param plugin the Main class
     */
    public ArenaLobby(SkyWars plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the task ID
     *
     * @return ID of the task
     */
    public static int getId() {
        return id;
    }

    /**
     * Sets the ID of the runnable.
     *
     * @param id new ID for the task
     */
    public static void setId(int id) {
        ArenaLobby.id = id;
    }

    /**
     * Cancel this task from being run.
     */
    public static void cancel() {
        Bukkit.getScheduler().cancelTask(id);
    }

    /**
     * Runnable which controls the remaining time of all arenas
     */
    @Override
    public void run() {
        plugin.arenaManager.getArenas().forEach(arena -> {
            int remainingTime;

            // Check if there are players in arena
            if (arena.getPlayers().size() < 1)
                return;

            // Check status
            if(arena.getStatus() != ArenaStatus.LOBBY)
                return;

            // Remaining time of that arena
            remainingTime = arena.getRemainingTime();
            if (remainingTime >= 0) {
                onLobby(arena);

                // Enough players to decrease remaining time
                if (remainingTime > 30 && arena.getPlayers().size() >= plugin.arenaManager.minReduceTimePlayers) {
                    arena.sendMessage(Message.MINIMUM_PLAYERS_REDUCE_TIME_ACHIEVED, "" + plugin.arenaManager.minReduceTimePlayers);
                    remainingTime = 31;
                }

                arena.setRemainingTime(remainingTime - 1);
            } else {
                // Minimum Players in the arena
                if (arena.getPlayers().size() >= plugin.arenaManager.minPlayers)
                    fromLobbyToPreGame(arena);
                else {
                    arena.sendMessage(Message.MININUM_PLAYERS_NOT_ACHIEVED, "" + plugin.arenaManager.minPlayers);
                    arena.setRemainingTime(ArenaStatus.LOBBY.getTime());
                }
            }
        });
    }

    /**
     * Sets the player level to remaining time.
     *
     * @param arena arena which is on lobby
     */
    private void onLobby(Arena arena) {
        int remainingTime = arena.getRemainingTime();
        arena.getPlayers().forEach(alvoDat -> {
            Player alvo = Bukkit.getPlayer(alvoDat.getUUID());
            if (alvo == null)
                return;

            alvo.setLevel(remainingTime);

            switch (remainingTime) {
                case 5:
                    alvo.playSound(alvo.getLocation(), Sound.NOTE_PIANO, 1, 10);
                    break;
                case 4:
                    alvo.playSound(alvo.getLocation(), Sound.NOTE_PIANO, 1, 10);
                    break;
                case 3:
                    alvo.playSound(alvo.getLocation(), Sound.NOTE_PIANO, 1, 10);
                    break;
                case 2:
                    alvo.playSound(alvo.getLocation(), Sound.NOTE_PIANO, 1, 10);
                    break;
                case 1:
                    alvo.playSound(alvo.getLocation(), Sound.NOTE_PIANO, 1, 10);
                    break;
                case 0:
                    alvo.playSound(alvo.getLocation(), Sound.ORB_PICKUP, 1, 10);
                    break;
            }
        });
    }

    /**
     * Switch an arena from lobby to pre game.
     * Teleports all the players to the spawn locations with delay between teleports.
     *
     * @param arena arena to switch
     */
    private void fromLobbyToPreGame(final Arena arena) {
        // Arena Dat
        ArenaDat arenaDat = arena.getArenaDat();
        if (null == arenaDat) {
            plugin.getLogger().log(Level.SEVERE, "ArenaDat is null when changing from Lobby to PreGame!");
            return;
        }

        // Change Status
        arena.setStatus(ArenaStatus.PREGAME);

        arenaDat.setStartDate(new Date()); // Set start date
        arenaDat.addGameEvent("The game " + arenaDat.getGameNumber() + " has started!");

        // Message
        String mapName = arena.getMapName().replace("" + arena.getArenaNumber(), "").replaceAll("_", " ");
        arena.sendMessage(Message.GAME_MAP, mapName);

        // Send Players To Spawns
        ArenaLocation arenaLocation = arena.getArenaLocation();
        if (null == arenaLocation) {
            plugin.getLogger().log(Level.SEVERE, "Arena location is null when changing from Lobby to PreGame!");
            return;
        }

        int i = 0;
        for (final PlayerDat alvoDat : arena.getPlayers()) {
            final Player alvo = Bukkit.getPlayer(alvoDat.getUUID());
            if (alvo == null)
                continue;

            // Config Player
            if (!plugin.playerManager.configPlayer(alvoDat, alvoDat.getStatus(), GameMode.ADVENTURE, false, false, 0, 0, 20.0, 20, true, true, 0)) {
                plugin.getLogger().log(Level.WARNING, "Error while configuring the player.");
                continue;
            }

            // Teleport player
            final Location spawnLocation = arenaLocation.getSpawnLocations().get(i);
            if (spawnLocation == null) {
                plugin.getLogger().log(Level.WARNING, "Spawn location " + i + " is null when chaning from Lobby to PreGame");
                continue;
            }
            arenaDat.addInitialPlayer(alvoDat.getUUID().toString()); // Add to initial players list
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                alvo.teleport(spawnLocation);
                // Give kit menu to the player
                arena.getKitManager().giveKitMenuItem(alvoDat);
            }, i);
            i++;
        }
    }
}