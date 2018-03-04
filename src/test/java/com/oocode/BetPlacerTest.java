package com.oocode;

import com.oocode.connectors.ISlugsBookmaker;
import com.oocode.connectors.ISlugsP2P;
import com.oocode.connectors.SlugsBookmaker;
import com.oocode.connectors.SlugsP2P;
import com.teamoptimization.Quote;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class BetPlacerTest {

    // API Interfaces
    private ISlugsP2P apiP2P;
    private ISlugsBookmaker apiBookmaker;
    // Common input parameters (example)
    private int slugId = 1;
    private String raceId = "The Monday race";
    private BigDecimal odds = new BigDecimal("0.50");
    private String p2pGuid = "{43a07213-1937-449a-bf86-ff43e24747f2}";
    private String bookerGuid = "{43a07213-1937-449a-bf86-ff43e24747f2}";

    @Before
    public void prepareCamera() {
        apiP2P = mock(SlugsP2P.class);
        apiBookmaker = mock(SlugsBookmaker.class);
    }

    @Test
    public void usesCheaperProviderIfOddsTheSame() {
        // mock both APIs, both returning/accepting same odd
        when(apiP2P.requestQuote(raceId, slugId, odds)).thenReturn(p2pGuid);
        when(apiBookmaker.requestQuote(raceId, slugId)).thenReturn(new Quote(odds, bookerGuid));

        // When placing a bet, if the odds are the same, choose cheapest one => 1st API (SlugSwapsAPI = apiP2P)
        BetPlacer betPlacer = new BetPlacer(apiP2P, apiBookmaker);
        betPlacer.placeBet(slugId, raceId, odds);

        // Assert that both APIs are called with the same inputs
        verify(apiP2P).requestQuote(eq(raceId), eq(slugId), eq(odds));
        verify(apiBookmaker).requestQuote(eq(raceId), eq(slugId));
        // Assert that the 1st API (SlugSwapsAPI = apiP2P) quote is accepted
        verify(apiP2P).agree(eq(p2pGuid));
        // Assert that the 2nd API (SlugRacingOdds) is not called anymore
        verifyNoMoreInteractions(apiBookmaker);
    }

    @Test
    public void usesCheaperProviderIfOddsBetter() throws Exception {
        // mock both APIs, 1st API (SlugSwapsApi) returns better value (higher) than 2nd API (SlugRacingOddsApi)
        // choose cheapest one => 1st one (SlugSwapsApi)
    }

    @Test
    public void usesExpensiveProviderIfOddsBetter() throws Exception {
        // mock both APIs, 1st API (SlugRacingOddsApi) returns worst value (lower) than 2nd API (SlugRacingOddsApi) returns better value (higher)
        // choose best one => 2nd one (SlugRacingOddsApi)
    }

    @Test
    public void placesExpensiveBetIfTargetOddsNotMetOnCheapBet() throws Exception {
        // mock both APIs, return null on 1st API (SlugRacingOddsApi) and return quote on 2nd API (SlugRacingOddsApi) lower or equal to desired quote
        // choose 2nd one (SlugRacingOddsApi)
        // Probably need to split into two tests (equal or higher quote)
    }

    @Test
    public void placesNoBetIfTargetOddsNotMetOnEither() throws Exception {
        // mock both APIs, return null on 1st API (SlugRacingOddsApi) and lower quote on 2nd API (SlugRacingOddsApi)
        // Do nothing. Don't place bet
    }
}
