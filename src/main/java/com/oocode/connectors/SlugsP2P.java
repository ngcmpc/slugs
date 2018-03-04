package com.oocode.connectors;

import com.teamoptimization.*;

import java.math.BigDecimal;

public class SlugsP2P implements ISlugsP2P {

    private long startTime = 0;

    public String requestQuote(String raceId, int slugId, BigDecimal odds) {
        this.startTime = System.currentTimeMillis();
        Race race = SlugSwapsApi.forRace(raceId);
        return (race == null? null : race.quote(slugId, odds));
    }
    public void agree(String quoteId) {
        try {
            SlugSwapsApi.accept(quoteId);
        } catch (SlugSwaps.Timeout timeout) { /* Do Nothing? */ }
    }
}
