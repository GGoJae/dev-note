package com.gj.dev_note.note.trend;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrendKeys {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public static final String VIEW_DELTA_PREFIX = "note:view:";

    public static final String RANK_H_PREFIX = "rank:note:views:h:";
    public static final String RANK_D_PREFIX = "rank:note:views:d:";

    public static final String RANK_H_INDEX = "rank:note:index:h:";
    public static final String RANK_D_INDEX = "rank:note:index:d:";

    public static final String TREND_24H = "trending:note:24h";
    public static final String TREND_7D = "trending:note:7d";

    private static final DateTimeFormatter H_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HH");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final int LAST_N_HOUR_LIMIT = 24;
    private static final int LAST_N_DAY_LIMIT = 7;

    public static String viewKey(long id) {
        return VIEW_DELTA_PREFIX + id;
    }

    public static String hourKey(Instant now) {
        var z = now.atZone(ZONE).withMinute(0).withSecond(0).withNano(0);
        return RANK_H_PREFIX + H_FMT.format(z);
    }

    public static String dayKey(Instant now) {
        var z = now.atZone(ZONE).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return RANK_D_PREFIX + D_FMT.format(z);
    }

    public static List<String> lastNHourKeys(Instant now, int n) {
        var z = now.atZone(ZONE).withMinute(0).withSecond(0).withNano(0);
        int limit = Math.min(n, LAST_N_HOUR_LIMIT);
        var keys = new ArrayList<String>(limit);
        for (int i = 0; i < limit; i++) {
            keys.add(RANK_H_PREFIX + H_FMT.format(z.minusHours(i)));
        }
        return keys;
    }

    public static List<String> lastNDaysKeys(Instant now, int n) {
        var z = now.atZone(ZONE).withHour(0).withMinute(10).withSecond(0).withNano(0);
        int limit = Math.min(n, LAST_N_DAY_LIMIT);
        var keys = new ArrayList<String>(limit);
        for (int i = 0; i < limit; i++) {
            keys.add((RANK_D_PREFIX + D_FMT.format(z.minusDays(i))));
        }
        return keys;
    }

    public static Instant parseHourKey(String hourKey) {
        var raw = hourKey.substring(RANK_H_PREFIX.length());
        var zdt = LocalDateTime.parse(raw, H_FMT).atZone(ZONE);
        return zdt.toInstant();
    }

    public static Instant parseDayKey(String dayKey) {
        var raw = dayKey.substring(RANK_D_PREFIX.length());
        var zdt = LocalDateTime.parse(raw, D_FMT).atZone(ZONE);
        return zdt.toInstant();
    }

}
