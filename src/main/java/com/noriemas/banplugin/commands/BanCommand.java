package com.noriemas.banplugin.commands;

import com.noriemas.banplugin.BanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BanCommand implements CommandExecutor {

    private final BanManager banManager;
    private final FileConfiguration config;

    public BanCommand(BanManager banManager, FileConfiguration config) {
        this.banManager = banManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(config.getString("messages.usage", "Usage: /ban <player> [duration] [reason]"));
            return true;
        }

        String targetPlayer = args[0];

        String duration = null;
        Long durationInSeconds = null;
        if (args.length > 1) {
            duration = args[1];
            if (!isValidDuration(duration)) {
                sender.sendMessage(config.getString("messages.invalidDuration", "Invalid duration format."));
                return true;
            }
            durationInSeconds = convertDurationToSeconds(duration);
        }

        String reason = null;
        if (args.length > 2) {
            reason = String.join(" ", args).substring(targetPlayer.length() + duration.length() + 2);
        }

        String issuer = sender.getName();

        try {
            banManager.banPlayer(targetPlayer, issuer, reason, durationInSeconds);
        } catch (Exception e) {
            sender.sendMessage("Failed to ban player.");
            return true;
        }

        String successMessage = config.getString("messages.playerBanned", "Player %s has been banned.");
        sender.sendMessage(String.format(successMessage, targetPlayer));

        Player target = sender.getServer().getPlayer(targetPlayer);
        if (target != null) {
            String kickMessage = "You are banned from this server!\n";
            kickMessage += reason != null ? "Reason: " + reason + "\n" : "";
            kickMessage += duration != null ? "Duration: " + duration + "\n" : "";
            target.kickPlayer(kickMessage);
        }

        return true;
    }

    private boolean isValidDuration(String duration) {
        Pattern durationPattern = Pattern.compile("^((\\d+mo)?(\\d+w)?(\\d+d)?(\\d+h)?(\\d+m)?(\\d+s)?)$");
        Matcher matcher = durationPattern.matcher(duration);
        return matcher.matches();
    }

    private Long convertDurationToSeconds(String duration) {
        Matcher matcher = Pattern.compile("(\\d+)(mo|w|d|h|m|s)").matcher(duration);
        long totalSeconds = 0;
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "mo":
                    totalSeconds += value * 2592000; // Assume 30 days in a month
                    break;
                case "w":
                    totalSeconds += value * 604800; // 7 days
                    break;
                case "d":
                    totalSeconds += value * 86400; // 24 hours
                    break;
                case "h":
                    totalSeconds += value * 3600; // 60 minutes
                    break;
                case "m":
                    totalSeconds += value * 60; // 60 seconds
                    break;
                case "s":
                    totalSeconds += value;
                    break;
            }
        }
        return totalSeconds;
    }
}
