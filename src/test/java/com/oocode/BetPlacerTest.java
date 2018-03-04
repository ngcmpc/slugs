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
    private BigDecimal oddsBetter = new BigDecimal("0.51");
    private BigDecimal oddsWorst = new BigDecimal("0.49");
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
        prepareMockReturnValues(odds, odds);

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
        // mock both APIs, both returning/accepting different odds
        prepareMockReturnValues(oddsBetter, odds);

        BetPlacer betPlacer = new BetPlacer(apiP2P, apiBookmaker);
        betPlacer.placeBet(slugId, raceId, oddsBetter);

        // Assert that both APIs are called
        verify(apiP2P).requestQuote(eq(raceId), eq(slugId), eq(oddsBetter));
        verify(apiBookmaker).requestQuote(eq(raceId), eq(slugId));
        // Assert that the 1st API (SlugSwapsAPI = apiP2P) quote is accepted
        verify(apiP2P).agree(eq(p2pGuid));
        // Assert that the 2nd API (SlugRacingOdds) is not called anymore
        verifyNoMoreInteractions(apiBookmaker);
    }

    @Test
    public void usesExpensiveProviderIfOddsBetter() throws Exception {
        // mock both APIs, both returning/accepting different odds
        prepareMockReturnValues(odds, oddsBetter);

        BetPlacer betPlacer = new BetPlacer(apiP2P, apiBookmaker);
        betPlacer.placeBet(slugId, raceId, odds);

        // Assert that both APIs are called
        verify(apiP2P).requestQuote(eq(raceId), eq(slugId), eq(odds));
        verify(apiBookmaker).requestQuote(eq(raceId), eq(slugId));
        // Assert that the 2nd API (SlugRacingOdds = apiBookmaker) quote is accepted
        verify(apiBookmaker).agree(eq(bookerGuid));
        // Assert that the 1st API (SlugSwapsAPI) is not called anymore
        verifyNoMoreInteractions(apiP2P);
    }

    @Test
    public void placesExpensiveBetIfTargetOddsNotMetOnCheapBet() throws Exception {
        // mock both APIs, 1st API not returning quote, but 2nd API returns accepted quote (equal or better)
        when(apiP2P.requestQuote(raceId, slugId, odds)).thenReturn(null);
        when(apiBookmaker.requestQuote(raceId, slugId)).thenReturn(new Quote(odds, bookerGuid));

        BetPlacer betPlacer = new BetPlacer(apiP2P, apiBookmaker);
        betPlacer.placeBet(slugId, raceId, odds);

        // Assert that both APIs are called
        verify(apiP2P).requestQuote(eq(raceId), eq(slugId), eq(odds));
        verify(apiBookmaker).requestQuote(eq(raceId), eq(slugId));
        // Assert that the 2nd API (SlugRacingOdds = apiBookmaker) quote is accepted
        verify(apiBookmaker).agree(eq(bookerGuid));
        // Assert that the 1st API (SlugSwapsAPI) is not called anymore
        verifyNoMoreInteractions(apiP2P);
    }

    @Test
    public void placesNoBetIfTargetOddsNotMetOnEither() throws Exception {
        // mock both APIs, 1st API not returning quote, but 2nd API returns accepted quote (equal or better)
        when(apiP2P.requestQuote(raceId, slugId, odds)).thenReturn(null);
        when(apiBookmaker.requestQuote(raceId, slugId)).thenReturn(new Quote(oddsWorst, bookerGuid));

        BetPlacer betPlacer = new BetPlacer(apiP2P, apiBookmaker);
        betPlacer.placeBet(slugId, raceId, odds);

        // Assert that both APIs are called
        verify(apiP2P).requestQuote(eq(raceId), eq(slugId), eq(odds));
        verify(apiBookmaker).requestQuote(eq(raceId), eq(slugId));
        // Assert that no bet is accepted on either API
        verifyNoMoreInteractions(apiP2P);
        verifyNoMoreInteractions(apiBookmaker);
    }

    @Test
    public void avoidTimeoutOnCheaperProvider() {
        // mock both APIs, 1st API (SlugRacingOddsApi) takes over 1second to response
        // choose 2nd one (SlugRacingOddsApi) to avoid timeout
    }

    private void prepareMockReturnValues(BigDecimal oddsP2P, BigDecimal oddsBookmaker) {
        when(apiP2P.requestQuote(raceId, slugId, oddsP2P)).thenReturn(p2pGuid);
        when(apiBookmaker.requestQuote(raceId, slugId)).thenReturn(new Quote(oddsBookmaker, bookerGuid));
    }
}
