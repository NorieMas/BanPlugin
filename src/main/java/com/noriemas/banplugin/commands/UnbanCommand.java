package com.noriemas.banplugin.commands;

import com.noriemas.banplugin.BanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;

public class UnbanCommand implements CommandExecutor {

    private final BanManager banManager;
    private final FileConfiguration config;

    public UnbanCommand(BanManager banManager, FileConfiguration config) {
        this.banManager = banManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(config.getString("messages.unbanUsage", "Usage: /unban <player>"));
            return true;
        }
        String targetPlayer = args[0];
        try {
            if (!banManager.isPlayerBanned(targetPlayer)) {
                sender.sendMessage(config.getString("messages.playerNotBanned", "Player is not banned."));
                return true;
            }
            banManager.unbanPlayer(targetPlayer);
            String successMessage = config.getString("messages.playerUnbanned", "Player %s has been unbanned.");
            sender.sendMessage(String.format(successMessage, targetPlayer));
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(config.getString("messages.unbanError", "Error unbanning player."));
        }
        return true;
    }
}
