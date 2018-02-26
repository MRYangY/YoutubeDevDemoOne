package com.example.youtubedevdemoone;

/**
 * Created by yangyu on 2018/2/26.
 */

public class Utils {
    public static String formatTimeMilliseconds(long time) {
        String str;
        if (time == 0L) str = "00:00";
        else {
            int itime = (int) (time / 1000L);
            int hour = itime / 3600;
            int min = itime % 3600 / 60;
            int sec = itime % 60;
            if (hour != 0) {
                str = String.format("%02d:%02d:%02d", hour, min, sec);
            } else {
                str = String.format("%02d:%02d", min, sec);
            }
        }
        return str;
    }
}
