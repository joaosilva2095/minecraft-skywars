package net.RevTut.Skywars;

import net.RevTut.Skywars.arena.Arena;
import net.RevTut.Skywars.arena.ArenaDat;
import net.RevTut.Skywars.arena.ArenaRunnable;
import net.RevTut.Skywars.libraries.appearance.AppearanceAPI;
import net.RevTut.Skywars.libraries.bypasses.BypassesAPI;
import net.RevTut.Skywars.libraries.converters.ConvertersAPI;
import net.RevTut.Skywars.libraries.nametag.NameTagAPI;
import net.RevTut.Skywars.listeners.block.BlockBreak;
import net.RevTut.Skywars.listeners.block.BlockPlace;
import net.RevTut.Skywars.listeners.environment.ProjectileHit;
import net.RevTut.Skywars.listeners.environment.Weather;
import net.RevTut.Skywars.listeners.player.*;
import net.RevTut.Skywars.managers.ArenaManager;
import net.RevTut.Skywars.managers.PlayerManager;
import net.RevTut.Skywars.managers.ScoreBoardManager;
import net.RevTut.Skywars.player.PlayerDat;
import net.RevTut.Skywars.utils.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Random;

/**
 * Main class.
 *
 * <P>Skywars is a plugin where you have several players in islands. They all have the same amount of chests and resources available.</P>
 * <P>However inside the chests the furniture is completely random. The goal of a player is either to kill all the players or throw them
 * to the void since there is no ground on the map. If after a predefined amount of time the game is not over yet we will have a tie so
 * there will be no winner.</P>
 * <P>There is a wide variety of kits, each one gives a differente advantage to the player that uses it.</P>
 *
 * @author Joao Silva
 * @version 1.0
 */
public class Main extends JavaPlugin {
    /**
     * Title message on join
     */
    public String titleMessage = "§3RevTut";

    /**
     * Subtitle message on join
     */
    public String subTitleMessage = "§7Network";

    /**
     * Fade in message time
     */
    public int fadeIn = 20;

    /**
     * Fade out message time
     */
    public int fadeOut = 20;

    /**
     * Time on screen of messages
     */
    public int timeOnScreen = 20;

    /**
     * Tab list title
     */
    public String tabTitle = "§3RevTut";

    /**
     * Tab list footer
     */
    public String tabFooter = "§6www.revtut.net";

    /**
     * MySQL object
     */
    public MySQL mysql;

    /**
     * Arena Manager
     */
    public final ArenaManager arenaManager = new ArenaManager(this);

    /**
     * Player Manager
     */
    public final PlayerManager playerManager = new PlayerManager(this);

    /**
     * ScoreBoard Manager
     */
    public final ScoreBoardManager scoreBoardManager = new ScoreBoardManager(this);

    /**
     * Player Chest
     */
    public PlayerChest playerChest;

    /**
     * Name of the server
     */
    public final String servidor = Bukkit.getServerName();

    /**
     * Random Class
     */
    public final Random rand = new Random();

    /**
     * Points earned per win. This is not the final amount as it depends on a luck factor
     * and depends either on the amount of players killed.
     * PE = points earned
     * PW = points per win
     * K = kills
     * N = number of players
     * F = random percentage (between 0 - 50%)
     * Formula: PE = PW + PW * ( K / N ) + PW * F
     */
    public final int pointsPerWin = 800;

    /**
     * Points earned per game played. This is not the final amount as it depends on a luck factor
     * and depends either on the amount of players killed.
     * PE = points earned
     * PG = points per game
     * K = kills
     * N = number of players
     * F = random percentage (0 - 25%)
     * Formula: PE = PG + PG * ( K / N ) + PG * F
     */
    public final int pointsPerGame = 200;

    /**
     * Points earned per kill. This is not the final amount as it depends on a luck factor
     * and depends either on the amount of players already killed.
     * PE = points earned
     * PK = points per kill
     * K = kills
     * N = number of players
     * F = random percentage (0 - 10%)
     * Formula: PE = PK + PK * (K / N) + PK * F
     */
    public final int pointsPerKill = 50;

    /**
     * Enable the plugin
     */
    @Override
    public void onEnable() {
        /* Create Files */
        if (!createFiles())
            System.out.println("Error while trying to create the initial files.");

        /* Read Files */
        if (!readFiles())
            System.out.println("Error while trying to read the files.");

        /* Create Initial Arenas */
        String lastGameNumber = mysql.lastGameNumber();
        if (lastGameNumber == null) {
            System.out.println("Error while creating the initial arenas as last game number is null.");
            return;
        }
        lastGameNumber = arenaManager.nextGameNumber(lastGameNumber);
        // Arena 1
        if (!arenaManager.createNewArena()) {
            System.out.println("Error while creating the initial arenas.");
            return;
        }
        Arena arena = arenaManager.getArenas().get(0);
        ArenaDat arenaDat = arena.getArenaDat();
        if (arenaDat == null) {
            System.out.println("Error while creating the initial arenas as arena dat is null.");
            return;
        }
        arenaDat.setGameNumber(lastGameNumber);
        if (!arenaManager.createNewArena()) {
            System.out.println("Error while creating the initial arenas.");
            return;
        }


        long minDuration = Integer.MAX_VALUE;
        long maxDuration = Integer.MIN_VALUE;
        long totalDuration = 0;
        int numberIterations = 1;
        for(int k = 0; k < numberIterations; k++) {
            long startTime = System.nanoTime();

            long endTime = System.nanoTime();
            long duration = (endTime - startTime)/1000;
            if(duration < minDuration)
                minDuration = duration;
            if(duration > maxDuration)
                maxDuration = duration;
            totalDuration += duration;
            System.out.println("[" + k + "] " + duration + "ms");
        }
        System.out.println("MAXIMUM DURATION: " + maxDuration + "ms");
        System.out.println("MINIMUM DURATION: " + minDuration + "ms");
        System.out.println("AVERAGE: " + (totalDuration / numberIterations) + "ms");

        /* Arena Runnable */
        ArenaRunnable task = new ArenaRunnable(this);
        task.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, task, 20, 20));

        /* Player Chest */
        playerChest = new PlayerChest(this);

        /* Set Main Classes */
        AppearanceAPI.plugin = this;
        BypassesAPI.plugin = this;
        NameTagAPI.plugin = this;
        PlayerDat.plugin = this;

        /* Register Events */
        PluginManager pm = Bukkit.getServer().getPluginManager();
        /* Libraries */
        pm.registerEvents(new NameTagAPI(), this);
        /* Listeners  */
        // Block
        pm.registerEvents(new BlockBreak(this), this);
        pm.registerEvents(new BlockPlace(this), this);
        // Environment
        pm.registerEvents(new ProjectileHit(this), this);
        pm.registerEvents(new Weather(this), this);
        // Player
        pm.registerEvents(new PlayerBucketEmpty(this), this);
        pm.registerEvents(new PlayerBucketFill(this), this);
        pm.registerEvents(new PlayerChat(this), this);
        pm.registerEvents(new PlayerDamage(this), this);
        pm.registerEvents(new PlayerDeath(this), this);
        pm.registerEvents(new PlayerDrop(this), this);
        pm.registerEvents(new PlayerFood(this), this);
        pm.registerEvents(new PlayerInteract(this), this);
        pm.registerEvents(new PlayerInventoryClick(this), this);
        pm.registerEvents(new PlayerJoin(this), this);
        pm.registerEvents(new PlayerMove(this), this);
        pm.registerEvents(new PlayerPickup(this), this);
        pm.registerEvents(new PlayerQuit(this), this);
        pm.registerEvents(new PlayerRespawn(this), this);
    }

    /**
     * Disable the plugin
     */
    @Override
    public void onDisable() {
        /* Close MySQL */
        if (!mysql.closeConnection())
            System.out.println("Error while trying to close connection.");
    }

    /**
     * Create the configuration files
     *
     * @return true if successfull
     */
    private boolean createFiles() {
        /* Config File */
        final File config = new File(getDataFolder() + File.separator + "config.yml");
        if (!config.exists()) {
            try {
                if (!config.getParentFile().mkdirs())
                    return false;
                if (!config.createNewFile())
                    return false;
            } catch (IOException e) {
                System.out.println("Error while creating config.yml. Reason: " + e.getMessage());
            }
            if (!copy(getResource("config.yml"), config))
                return false;
        }
        /* MySQL File */
        final File mysqlConf = new File(getDataFolder() + File.separator + "mysql.yml");
        if (!mysqlConf.exists()) {
            try {
                if (!mysqlConf.getParentFile().mkdirs())
                    return false;
                if (!mysqlConf.createNewFile())
                    return false;
            } catch (IOException e) {
                System.out.println("Error while creating mysql.yml. Reason: " + e.getMessage());
            }
            if (!copy(getResource("mysql.yml"), mysqlConf))
                return false;
        }
        return true;
    }

    /**
     * Copy from a file to another one
     *
     * @param in   file to be copied
     * @param file file to copy to
     * @return true if successfull
     */
    private boolean copy(final InputStream in, final File file) {
        try {
            final OutputStream out = new FileOutputStream(file);
            final byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Read the configuration files and assign the variables
     *
     * @return true if successfull
     */
    private boolean readFiles() {
        /* Config File */
        final File config = new File(getDataFolder() + File.separator + "config.yml");
        final FileConfiguration configConf = YamlConfiguration.loadConfiguration(config);
        // Title
        titleMessage = ConvertersAPI.convertToJSON(configConf.getString("Title").replaceAll("&", "§"));
        subTitleMessage = ConvertersAPI.convertToJSON(configConf.getString("Subtitle").replaceAll("&", "§"));
        fadeIn = configConf.getInt("FadeIn");
        fadeOut = configConf.getInt("FadeOut");
        timeOnScreen = configConf.getInt("TimeOnScreen");
        // Tab
        tabTitle = ConvertersAPI.convertSpecialCharacters(ConvertersAPI.convertToJSON(configConf.getString("TabTitle").replaceAll("&", "§")));
        tabFooter = ConvertersAPI.convertSpecialCharacters(ConvertersAPI.convertToJSON(configConf.getString("TabFooter").replaceAll("&", "§")));
        /* MySQL File */
        final File mysqlFile = new File(getDataFolder() + File.separator + "mysql.yml");
        final FileConfiguration mysqlConf = YamlConfiguration.loadConfiguration(mysqlFile);
        mysql = new MySQL(this, mysqlConf.getString("Hostname"), mysqlConf.getString("Port"), mysqlConf.getString("Database"), mysqlConf.getString("Username"), mysqlConf.getString("Password"));
        if (!mysql.openConnection())
            return false;
        mysql.createMySQL();

        return true;
    }
}
