package com.bekvon.bukkit.residence.shopStuff;

public class Vote {
    double vote;
    int amount;

    public Vote(double vote, int amount) {
	this.vote = vote;
	this.amount = amount;

    }

    public int getAmount() {
	return this.amount;
    }

    public double getVote() {
	return this.vote;
    }

}
