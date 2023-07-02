package com.noriemas.banplugin.listeners;

import com.noriemas.banplugin.BanEntry;
import com.noriemas.banplugin.BanManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerJoinListener implements Listener {

    private final BanManager banManager;
    private static final Logger LOGGER = Logger.getLogger(PlayerJoinListener.class.getName());

    public PlayerJoinListener(BanManager banManager) {
        this.banManager = banManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            BanEntry banEntry = banManager.getActiveBan(player.getName());

            if (banEntry != null) {
                String kickMessage = "You are banned from this server!\n";
                kickMessage += "Reason: " + banEntry.getReason() + "\n";
                if (banEntry.getDuration() != null) {
                    kickMessage += "Expires: " + banEntry.getExpirationDate() + "\n";
                }
                player.kickPlayer(kickMessage);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving ban information for player: " + player.getName(), e);
        }
    }
}
