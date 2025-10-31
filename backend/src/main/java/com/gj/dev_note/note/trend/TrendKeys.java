package com.gj.dev_note.note.trend;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public abstract class TrendKeys {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public static final String VIEW_DELTA_PREFIX = "note:view";
    public static final String RANK_H_PREFIX = "rank:note:views:h:";
    public static final String RANK_D_PREFIX = "rank:note:views:d:";

    private static final DateTimeFormatter H_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HH");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String viewKey(long id) {
        return VIEW_DELTA_PREFIX + id;
    }

    public static String hourKey(Instant now) {
        return RANK_H_PREFIX + H_FMT.format(now.atZone(ZONE));
    }

    public static String dayKey(Instant now) {
        return RANK_D_PREFIX + D_FMT.format(now.atZone(ZONE));
    }

    public static List<String> lastNHourKeys(Instant now, int n) {
        var z = now.atZone(ZONE).withMinute(0).withSecond(0).withNano(0);
        var keys = new ArrayList<String>(n);
        for (int i = 0; i < n; i++) {
            keys.add(RANK_H_PREFIX + H_FMT.format(z.minusHours(i)));
        }
        return keys;
    }

    public static List<String> lastNDaysKeys(Instant now, int n) {
        var z = now.atZone(ZONE).withHour(0).withMinute(10).withSecond(0).withNano(0);
        var keys = new ArrayList<String>(n);
        for (int i = 0; i < n; i++) {
            keys.add((RANK_D_PREFIX + D_FMT.format(z.minusDays(i))));
        }
        return keys;
    }

}
