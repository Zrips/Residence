package com.bekvon.bukkit.residence.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TimeModifier {

    s(1), m(60), h(60 * 60), d(60 * 60 * 24), w(60 * 60 * 24 * 7), M(60 * 60 * 24 * 30), Y(60 * 60 * 24 * 365);
    private int modifier = 0;

    static Pattern patern = Pattern.compile("(\\d+[a-z])");

    TimeModifier(int modifier) {
	this.modifier = modifier;
    }

    public int getModifier() {
	return modifier;
    }

    public void setModifier(int modifier) {
	this.modifier = modifier;
    }

    public static Long getTimeRangeFromString(String time) {
	try {
	    return Long.parseLong(time);
	} catch (Exception e) {
	}
	Matcher match = patern.matcher(time);
	Long total = null;
	while (match.find()) {
	    String t = match.group(1);
	    for (TimeModifier one : TimeModifier.values()) {
		if (t.endsWith(one.name())) {
		    try {
			Long amount = Long.parseLong(t.substring(0, t.length() - one.name().length()));
			if (total == null)
			    total = 0L;
			total += amount * one.getModifier();
		    } catch (Exception e) {
			break;
		    }
		}
	    }
	}
	return total;
    }

}
