package com.noriemas.banplugin.commands;

import com.noriemas.banplugin.BanEntry;
import com.noriemas.banplugin.BanManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HistoryCommand implements CommandExecutor, Listener {

    private final BanManager banManager;
    private final int itemsPerPage = 45;
    private final JavaPlugin plugin;

    public HistoryCommand(BanManager banManager, JavaPlugin plugin) {
        this.banManager = banManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage("Usage: /history <player name>");
            return true;
        }
        Player player = (Player) sender;
        String targetPlayer = args[0];
        List<BanEntry> banHistory;
        try {
            banHistory = banManager.getBanHistory(targetPlayer);
        } catch (SQLException e) {
            sender.sendMessage("Error retrieving ban history.");
            e.printStackTrace();
            return true;
        }
        openInventory(player, banHistory, 1);
        return true;
    }

    private void openInventory(Player player, List<BanEntry> banHistory, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "Ban History: Page " + page);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int start = (page - 1) * itemsPerPage;
        for (int i = start; i < start + itemsPerPage && i < banHistory.size(); i++) {
            BanEntry entry = banHistory.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Ban #" + (i + 1));
            List<String> lore = new ArrayList<>();
            lore.add("Date: " + sdf.format(entry.getIssueDate()));
            lore.add("Issuer: " + entry.getIssuer());
            lore.add("Reason: " + entry.getReason());
            if (entry.getDuration() != null) {
                lore.add("Duration: " + entry.getDuration());
                lore.add("Expiration Date: " + sdf.format(entry.getExpirationDate()));
                lore.add("Expired: " + (entry.isExpired() ? "Yes" : "No"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }
        if (start + itemsPerPage < banHistory.size()) {
            ItemStack item = new ItemStack(Material.ARROW);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Next Page");
            item.setItemMeta(meta);
            inv.setItem(53, item);
        }
        if (page > 1) {
            ItemStack item = new ItemStack(Material.ARROW);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Previous Page");
            item.setItemMeta(meta);
            inv.setItem(45, item);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("Ban History:")) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        String targetPlayer = player.getName();
        List<BanEntry> banHistory;
        try {
            banHistory = banManager.getBanHistory(targetPlayer);
        } catch (SQLException e) {
            player.sendMessage("Error retrieving ban history.");
            e.printStackTrace();
            return;
        }
        int currentPage = Integer.parseInt(event.getView().getTitle().split(" ")[2]);

        if (clickedItem.getType() == Material.ARROW && clickedItem.getItemMeta().getDisplayName().equals("Next Page")) {
            openInventory(player, banHistory, currentPage + 1);
        }
        if (clickedItem.getType() == Material.ARROW && clickedItem.getItemMeta().getDisplayName().equals("Previous Page")) {
            openInventory(player, banHistory, currentPage - 1);
        }
    }
}