package com.noriemas.banplugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(?:(\\d+)mo)?(?:(\\d+)w)?(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?");

    public static Long parseDuration(String input) {
        Matcher matcher = DURATION_PATTERN.matcher(input);
        if (matcher.matches()) {
            int months = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 0;
            int weeks = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
            int days = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
            int hours = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 0;
            int minutes = matcher.group(5) != null ? Integer.parseInt(matcher.group(5)) : 0;
            int seconds = matcher.group(6) != null ? Integer.parseInt(matcher.group(6)) : 0;
            long totalSeconds = seconds +
                    (minutes * 60) +
                    (hours * 3600) +
                    (days * 86400) +
                    (weeks * 604800) +
                    (months * 2592000);
            return totalSeconds * 1000;
        }
        return null;
    }
}
