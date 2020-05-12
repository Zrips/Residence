package com.bekvon.bukkit.residence.utils;

import com.bekvon.bukkit.residence.Residence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GetTime {
    public static String getTime(Long time) {
        return getTime(time, false);
    }

    public static String getTime(Long time, boolean shortFormat) {
        Date dNow = new Date(time);
        SimpleDateFormat ft = new SimpleDateFormat(!shortFormat ? Residence.getInstance().getConfigManager().getDateFormat() : Residence.getInstance().getConfigManager().getDateFormatShort());
        ft.setTimeZone(TimeZone.getTimeZone(Residence.getInstance().getConfigManager().getTimeZone()));
        return ft.format(dNow);
    }
}