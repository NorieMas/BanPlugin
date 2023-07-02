package com.noriemas.banplugin;

import com.noriemas.banplugin.commands.BanCommand;
import com.noriemas.banplugin.commands.HistoryCommand;
import com.noriemas.banplugin.commands.UnbanCommand;
import com.noriemas.banplugin.listeners.PlayerJoinListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BanPlugin extends JavaPlugin {

    private BanManager banManager;
    private Connection databaseConnection;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        FileConfiguration config = getConfig();

        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.database");
        String username = config.getString("database.username");
        String password = config.getString("database.password");
        String databaseUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            databaseConnection = DriverManager.getConnection(databaseUrl, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            getPluginLoader().disablePlugin(this);
            return;
        }

        try (Statement statement = databaseConnection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS ban_history (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "player_name VARCHAR(16) NOT NULL," +
                    "issuer VARCHAR(16) NOT NULL," +
                    "reason VARCHAR(255) NOT NULL," +
                    "issue_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "duration BIGINT NOT NULL DEFAULT 0" +
                    ")";
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        banManager = new BanManager(databaseConnection);

        PluginCommand banCommand = getCommand("ban");
        if (banCommand != null) {
            banCommand.setExecutor(new BanCommand(banManager, config));
        } else {
            getLogger().severe("Failed to initialize 'ban' command");
        }

        PluginCommand unbanCommand = getCommand("unban");
        if (unbanCommand != null) {
            unbanCommand.setExecutor(new UnbanCommand(banManager, config));
        } else {
            getLogger().severe("Failed to initialize 'unban' command");
        }

        PluginCommand historyCommand = getCommand("history");
        if (historyCommand != null) {
            historyCommand.setExecutor(new HistoryCommand(banManager, this));
        } else {
            getLogger().severe("Failed to initialize 'history' command");
        }

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(banManager), this);
    }

    @Override
    public void onDisable() {
        try {
            if (databaseConnection != null) {
                databaseConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
