package com.oocode.connectors;

import com.teamoptimization.Race;

import java.math.BigDecimal;

public interface ISlugsSwaps {
    public Race forRace(String raceName);
    public String quote(int slugId, BigDecimal odds);
}
    public void placeBet(int slugId, String raceName, BigDecimal targetOdds) {
        String result;
        Race race = SlugSwapsApi.forRace(raceName);
        if (race == null) {
            result = null;
        } else {
            result = race.quote(slugId, targetOdds);
        }
        String p2p = result;

        Quote b = SlugRacingOddsApi.on(slugId, raceName);
        if (p2p != null && targetOdds.compareTo(b.odds) >= 0) {
            try {
                SlugSwapsApi.accept(p2p);
            } catch (SlugSwaps.Timeout timeout) {
                // Do Nothing?
            }
        } else {
            if (b.odds.compareTo(targetOdds) >= 0) {
                SlugRacingOddsApi.agree(b.uid);
            }
        }
    }
