package net.RevTut.Skywars.utils;

import net.RevTut.Skywars.arena.ArenaDat;
import org.bukkit.Bukkit;
import org.fusesource.jansi.Ansi;

import java.sql.*;
import java.util.Date;
import java.util.UUID;

/**
 * Created by João on 24/10/2014.
 */
public class MySQL {

    /* Initializers */
    private final String hostname;
    private final String port;
    private final String database;
    private final String username;
    private final String password;
    /* Databases */
    private final String DBCore = "Core";
    private final String DBGameCore = "SkyWarsCore";
    private final String DBGameInfo = "SkyWarsInfo";
    /* Connection */
    public Connection connection;

    /* Constructor */
    public MySQL(String hostname, String port, String database, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    /* Open Connection */
    public boolean openConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, this.username, this.password);
            System.out.println("Successfully established the connection with MySQL!");
            return true;
        } catch (final SQLException e) {
            System.out.println("Error while trying to connect with MySQL! Reason: " + e.getMessage());
        } catch (final ClassNotFoundException e) {
            System.out.println("JDBC Driver not found!");
        }
        System.out.println("Error while trying to connect with MySQL!");
        Bukkit.shutdown();
        return false;
    }

    /* Check Connection */
    public boolean isClosed() {
        try {
            return this.connection.isClosed();
        } catch (final SQLException e) {
            System.out.println("Error while trying to check the connection status. Reason: " + e.getMessage());
        }
        return true;
    }

    /* Close Connection */
    public boolean closeConnection() {
        try {
            this.connection.close();
            return true;
        } catch (final SQLException e) {
            System.out.println("Error while trying to close the connection. Reason: " + e.getMessage());
        }
        return false;
    }

    /* Get's */
    public Connection getConnection() {
        return this.connection;
    }

    /* Create MySQL */
    public void createMySQL() {
        try {
            Statement statementCreation = connection.createStatement();

            /* Core - Player, PlayTime, PlayersVisible, Chat, Points, NumberBans, NumberJoins, NumberKicks, NumberReports */
            final ResultSet resultadoCore = connection.getMetaData().getTables(null, null, DBCore, null);
            if (resultadoCore.next()) {
                System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).bold() + DBCore + ":" + Ansi.ansi().fg(Ansi.Color.WHITE).boldOff() + " Encontrada!");
            } else {
                statementCreation.executeUpdate("CREATE TABLE IF NOT EXISTS " + DBCore + " (Player VARCHAR(100), PlayTime int(20), PlayersVisible TINYINT(1), Chat TINYINT(1), Points int(20), NumberBans int(3), NumberJoins int(20), NumberKicks int(3), NumberReports int(10));");
                System.out.println(Ansi.ansi().fg(Ansi.Color.YELLOW).bold() + DBCore + ":" + Ansi.ansi().fg(Ansi.Color.WHITE).boldOff() + " Criada!");
            }

            /* GameCore - Player, PlayTime, Wins, Losses, Kills, Deaths */
            final ResultSet resultadoGameCore = connection.getMetaData().getTables(null, null, DBGameCore, null);
            if (resultadoGameCore.next()) {
                System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).bold() + DBGameCore + ":" + Ansi.ansi().fg(Ansi.Color.WHITE).boldOff() + " Encontrada!");
            } else {
                statementCreation.executeUpdate("CREATE TABLE IF NOT EXISTS " + DBGameCore + " (Player VARCHAR(100), PlayTime int(20), Wins int(20), Losses int(3), Kills int(20), Deaths int(3));");
                System.out.println(Ansi.ansi().fg(Ansi.Color.YELLOW).bold() + DBGameCore + ":" + Ansi.ansi().fg(Ansi.Color.WHITE).boldOff() + " Criada!");
            }

            /* GameCore - GameNumber, Winner, StartDate, StopDate, InitialPlayers, GameChat, GameEvents */
            final ResultSet resultadoGameInfo = connection.getMetaData().getTables(null, null, DBGameInfo, null);
            if (resultadoGameInfo.next()) {
                System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).bold() + DBGameInfo + ":" + Ansi.ansi().fg(Ansi.Color.WHITE).boldOff() + " Encontrada!");
            } else {
                statementCreation.executeUpdate("CREATE TABLE IF NOT EXISTS " + DBGameInfo + " (GameNumber VARCHAR(100), Winner VARCHAR(100), StartDate VARCHAR(100), StopDate VARCHAR(100), InitialPlayers VARCHAR(250), GameChat MEDIUMTEXT, GameEvents MEDIUMTEXT);");
                System.out.println(Ansi.ansi().fg(Ansi.Color.YELLOW).bold() + DBGameInfo + ":" + Ansi.ansi().fg(Ansi.Color.WHITE).boldOff() + " Criada!");
            }
        } catch (final SQLException e) {
            System.out.println("Error while trying to create the MySQL. Reason: " + e.getMessage());
        }
    }

    /* Create PlayerDat */
    public boolean createPlayerDat(UUID uuid) {
        try {
            final String coreStatement = "SELECT * FROM " + DBCore + " WHERE Player = '" + uuid + "';";
            final String gameStatement = "SELECT * FROM " + DBGameCore + " WHERE Player = '" + uuid + "';";
            final ResultSet resultCore = this.connection.createStatement().executeQuery(coreStatement);
            final ResultSet resultGame = this.connection.createStatement().executeQuery(gameStatement);
            if (resultGame.next()) {
                if (resultCore.next())
                    PlayerDat.addPlayerDat(new PlayerDat(uuid, new Date(), resultGame.getLong("PlayTime"), resultCore.getInt("Points"), resultGame.getInt("Wins"), resultGame.getInt("Losses"), resultGame.getInt("Kills"), resultGame.getInt("Deaths")));
                else
                    PlayerDat.addPlayerDat(new PlayerDat(uuid, new Date(), resultGame.getLong("PlayTime"), 0, resultGame.getInt("Wins"), resultGame.getInt("Losses"), resultGame.getInt("Kills"), resultGame.getInt("Deaths")));
            } else {
                PlayerDat.addPlayerDat(new PlayerDat(uuid, new Date(), 0, 0, 0, 0, 0, 0));
            }
        } catch (final SQLException e) {
            System.out.println("Error while trying to create PlayerDat! Reason: " + e.getMessage());
            return false;
        }
        return true;
    }

    /* Update MySQL PlayerDat */
    public boolean updateMySQLPlayerDat(PlayerDat playerDat) {
        try {
            // Update Core
            final String coreStatement = "SELECT * FROM " + DBCore + " WHERE Player = '" + playerDat.getUUID() + "';";
            final ResultSet resultCore = this.connection.createStatement().executeQuery(coreStatement);
            if(resultCore.next()){
                final String coreUpdate = "UPDATE " + DBCore + " SET Points = " + playerDat.getPoints() + " WHERE Player = '" + playerDat.getUUID() + "';";
                this.connection.createStatement().executeUpdate(coreUpdate);
            }else{
                final String coreUpdate = "INSERT INTO " + DBCore + " (Player, PlayTime, PlayersVisible, Chat, Points, NumberBans, NumberJoins, NumberKicks, NumberReports) VALUES ('" + playerDat.getUUID() + "', " + playerDat.getPlayTime() + ", 0, 0, " + playerDat.getPoints() + ", 0, 1, 0, 0);";
                this.connection.createStatement().executeUpdate(coreUpdate);
            }
            // Update Game
            final String gameStatement = "SELECT * FROM " + DBGameCore + " WHERE Player = '" + playerDat.getUUID() + "'";
            final ResultSet resultGame = this.connection.createStatement().executeQuery(gameStatement);
            if (resultGame.next()) {
                final String gameUpdate = "UPDATE " + DBGameCore + " SET PlayTime = " + playerDat.getPlayTime() + ", Wins = " + playerDat.getWins() + ", Losses = " + playerDat.getLosses() + ", Kills = " + playerDat.getKills() + ", Deaths = " + playerDat.getDeaths() + " WHERE Player = '" + playerDat.getUUID() + "';";
                this.connection.createStatement().executeUpdate(gameUpdate);
            } else {
                final String gameUpdate = "INSERT INTO " + DBGameCore + " (Player, PlayTime, Wins, Losses, Kills, Deaths) VALUES ('" + playerDat.getUUID() + "', " + playerDat.getPlayTime() + ", " + playerDat.getWins() + ", " + playerDat.getLosses() + ", " + playerDat.getKills() + ", " + playerDat.getDeaths() + ");";
                this.connection.createStatement().executeUpdate(gameUpdate);
            }
        } catch (final SQLException e) {
            System.out.println("Error while trying to update the MySQL! Reason: " + e.getMessage());
            return false;
        }
        return true;
    }

    /* Update MySQL ArenaDat */
    public boolean updateMySQLArenaDat(ArenaDat arenaDat) {
        try {
            final String infoCreate = "INSERT INTO " + DBGameInfo + " (GameNumber, Winner, StartDate, StopDate, InitialPlayers, GameChat, GameEvents) VALUES ('" + arenaDat.getGameNumber() + "', '" + arenaDat.getWinner() + "', '" + arenaDat.getStartDate() + "', '" + arenaDat.getStopDate() + "'', '" + arenaDat.getInitialPlayers() + "'', '" + arenaDat.getGameChat() + "', '" + arenaDat.getGameEvents() + "');";
            this.connection.createStatement().executeUpdate(infoCreate);
        } catch (final SQLException e) {
            System.out.println("Error while trying to update the MySQL! Reason: " + e.getMessage());
            return false;
        }
        return true;
    }
}