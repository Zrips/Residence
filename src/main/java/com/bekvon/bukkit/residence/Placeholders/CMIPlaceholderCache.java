package com.bekvon.bukkit.residence.Placeholders;

public class CMIPlaceholderCache {

    private Long time = 0L;
    private String result = null;

    public Long getValidUntil() {
	return time;
    }

    public void setValidUntil(Long time) {
	this.time = time;
    }

    public String getResult() {
	return result;
    }

    public void setResult(String result) {
	this.result = result;
    }

}
