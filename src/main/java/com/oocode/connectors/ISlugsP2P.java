package com.oocode.connectors;

import java.math.BigDecimal;

public interface ISlugsP2P {
    String requestQuote(String raceId, int slugId, BigDecimal odds);
    void agree(String quoteId);
}
