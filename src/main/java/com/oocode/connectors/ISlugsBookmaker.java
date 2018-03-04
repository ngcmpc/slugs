package com.oocode.connectors;

import com.teamoptimization.Quote;

public interface ISlugsBookmaker {
    Quote requestQuote(String raceId, int slugId);
    void agree(String quoteId);
}
