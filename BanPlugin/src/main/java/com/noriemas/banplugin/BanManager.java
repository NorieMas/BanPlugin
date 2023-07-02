package com.noriemas.banplugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BanManager {

    private final Connection databaseConnection;

    public BanManager(Connection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void banPlayer(String playerName, String issuer, String reason, Long duration) throws SQLException {
        String query = "INSERT INTO ban_history (player_name, issuer, reason, issue_date, duration) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = databaseConnection.prepareStatement(query)) {
            statement.setString(1, playerName);
            statement.setString(2, issuer);
            statement.setString(3, reason);
            statement.setTimestamp(4, new Timestamp(new Date().getTime()));
            statement.setLong(5, duration != null ? duration : 0);
            statement.executeUpdate();
        }
    }

    public void unbanPlayer(String playerName) throws SQLException {
        String query = "UPDATE ban_history SET duration = TIMESTAMPDIFF(SECOND, issue_date, NOW()) WHERE player_name = ? AND issue_date > NOW() - INTERVAL duration SECOND";
        try (PreparedStatement statement = databaseConnection.prepareStatement(query)) {
            statement.setString(1, playerName);
            statement.executeUpdate();
        }
    }

    public List<BanEntry> getBanHistory(String playerName) throws SQLException {
        List<BanEntry> banHistory = new ArrayList<>();
        String query = "SELECT * FROM ban_history WHERE player_name = ? ORDER BY issue_date DESC";
        try (PreparedStatement statement = databaseConnection.prepareStatement(query)) {
            statement.setString(1, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String issuer = resultSet.getString("issuer");
                    String reason = resultSet.getString("reason");
                    Date issueDate = resultSet.getTimestamp("issue_date");
                    Long duration = resultSet.getLong("duration");
                    banHistory.add(new BanEntry(playerName, issuer, reason, issueDate, duration));
                }
            }
        }
        return banHistory;
    }

    public boolean isPlayerBanned(String playerName) throws SQLException {
        String query = "SELECT * FROM ban_history WHERE player_name = ? AND (issue_date + INTERVAL duration SECOND > NOW() OR duration = 0) ORDER BY issue_date DESC LIMIT 1";
        try (PreparedStatement statement = databaseConnection.prepareStatement(query)) {
            statement.setString(1, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public BanEntry getActiveBan(String playerName) throws SQLException {
        String query = "SELECT * FROM ban_history WHERE player_name = ? AND (issue_date + INTERVAL duration SECOND > NOW() OR duration = 0) ORDER BY issue_date DESC LIMIT 1";
        try (PreparedStatement statement = databaseConnection.prepareStatement(query)) {
            statement.setString(1, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String issuer = resultSet.getString("issuer");
                    String reason = resultSet.getString("reason");
                    Date issueDate = resultSet.getTimestamp("issue_date");
                    Long duration = resultSet.getLong("duration");
                    return new BanEntry(playerName, issuer, reason, issueDate, duration);
                }
            }
        }
        return null;
    }
}

