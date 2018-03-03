package com.oocode.connectors;

import com.teamoptimization.Quote;
import com.teamoptimization.Race;
import java.math.BigDecimal;

public interface ISlugsP2P {
    public String requestQuote(String raceId, int slugId, BigDecimal odds);
    public void agree(String quoteId);
}
