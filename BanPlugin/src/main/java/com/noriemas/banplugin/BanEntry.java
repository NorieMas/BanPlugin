package com.noriemas.banplugin;

import java.util.Date;

public class BanEntry {
    private final String playerName;
    private final String issuer;
    private final String reason;
    private final Date issueDate;
    private final Long duration;

    public BanEntry(String playerName, String issuer, String reason, Date issueDate, Long duration) {
        this.playerName = playerName;
        this.issuer = issuer;
        this.reason = reason;
        this.issueDate = issueDate;
        this.duration = duration;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getReason() {
        return reason;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public Long getDuration() {
        return duration;
    }

    public Date getExpirationDate() {
        return duration == null ? null : new Date(issueDate.getTime() + duration);
    }

    public boolean isExpired() {
        return duration != null && new Date().getTime() > issueDate.getTime() + duration;
    }
}
