package com.oocode.connectors;

import com.teamoptimization.Quote;

public interface ISlugsBookmaker {
    public Quote requestQuote(String raceId, int slugId);
    public void agree(String quoteId);
}
