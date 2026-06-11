package com.truebalance.creditiq.common;

public enum GameType {

    CREDIT_IQ(50),
    CRICKET(10);

    private final int coinsMultiplier;

    GameType(int coinsMultiplier) {
        this.coinsMultiplier = coinsMultiplier;
    }

    public int getCoinsMultiplier() {
        return coinsMultiplier;
    }

    public int toCoins(int score) {
        return score * coinsMultiplier;
    }
}
