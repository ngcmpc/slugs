package com.oocode.connectors;

import com.teamoptimization.*;

import java.math.BigDecimal;

public class SlugsP2P implements ISlugsP2P {

    public String requestQuote(String raceId, int slugId, BigDecimal odds) {
        Race race = SlugSwapsApi.forRace(raceId);
        return (race == null? null : race.quote(slugId, odds));
    }
    public void agree(String quoteId) {
        try {
            SlugSwapsApi.accept(quoteId);
        } catch (SlugSwaps.Timeout timeout) { /* Do Nothing? */ }
    }
}
