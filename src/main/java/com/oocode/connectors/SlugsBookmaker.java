package com.oocode.connectors;

import com.teamoptimization.*;

public class SlugsBookmaker implements ISlugsBookmaker {

    public Quote requestQuote(String raceId, int slugId) {
        return SlugRacingOddsApi.on(slugId, raceId);
    }
    public void agree(String quoteId) {
        SlugRacingOddsApi.agree(quoteId);
    }
}
