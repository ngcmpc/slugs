package com.oocode;

import com.oocode.connectors.ISlugsBookmaker;
import com.oocode.connectors.ISlugsP2P;
import com.oocode.connectors.SlugsBookmaker;
import com.oocode.connectors.SlugsP2P;
import com.teamoptimization.Quote;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.*;

public class BetPlacerTest {

    // API Interfaces
    private ISlugsP2P apiP2P;
    private ISlugsBookmaker apiBookmaker;
    // Common input parameters (example)
    private final int slugId = 1;
    private final String raceId = "The Monday race";
    private final BigDecimal odds = new BigDecimal("0.50");
    private final BigDecimal oddsBetter = new BigDecimal("0.51");
    private final BigDecimal oddsWorst = new BigDecimal("0.49");
    private final String p2pGuid = "{43a07213-1937-449a-bf86-ff43e24747f2}";
    private final String bookerGuid = "{43a07213-1937-449a-bf86-ff43e24747f2}";

    @Before
    public void prepareCamera() {
        apiP2P = mock(SlugsP2P.class);
        apiBookmaker = mock(SlugsBookmaker.class);
    }

    @Test
    public void usesExpensiveProviderIfOddsTheSame() {
        // mock both APIs, both returning/accepting same odd
        prepareMockReturnValues(odds, odds);

        BetPlacer betPlacer = new BetPlacer(apiP2P, apiBookmaker);
        betPlacer.placeBet(slugId, raceId, odds);

        // Assert that both APIs are called with the expected inputs
        verify(apiP2P).requestQuote(eq(raceId), eq(slugId), eq(odds));
        verify(apiBookmaker).requestQuote(eq(raceId), eq(slugId));
        // Assert that the 2nd API (SlugRacingOdds) quote is accepted
        verify(apiBookmaker).agree(eq(bookerGuid));
        // Assert that the 1st API (SlugSwapsAPI = apiP2P) is not called anymore
        verifyNoMoreInteractions(apiP2P);
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
        // Assert that odd(P2P) if better than odd(Bookmaker)
        assertThat(oddsBetter,greaterThan(odds));
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
        // Assert that odd(Bookmaker) if better than odd(P2P)
        assertThat(odds,lessThan(oddsBetter));
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

        // Assert that odd(Bookmaker) if worst than solicited odd
        assertThat(odds,greaterThan(oddsWorst));
    }

    @Test
    public void avoidTimeoutOnCheaperProvider() {
        // mock both APIs, delaying the 1st API 1 second
        prepareMockReturnValuesWithDelay(odds, odds, 1000);

        BetPlacer betPlacer = new BetPlacer(apiP2P, apiBookmaker);
        betPlacer.placeBet(slugId, raceId, odds);

        // Assert that both APIs are called with the expected inputs
        verify(apiP2P).requestQuote(eq(raceId), eq(slugId), eq(odds));
        verify(apiBookmaker).requestQuote(eq(raceId), eq(slugId));
        // Assert that the 2nd API (SlugRacingOdds) quote is accepted (if equal or higher than initial odd)
        verify(apiBookmaker).agree(eq(bookerGuid));
        // Assert that the 1st API (SlugSwapsAPI) is avoided because of the delay introduced
        verifyNoMoreInteractions(apiP2P);
    }

    // Auxiliary methods
    private void prepareMockReturnValues(BigDecimal oddsP2P, BigDecimal oddsBookmaker) {
        when(apiP2P.requestQuote(raceId, slugId, oddsP2P)).thenReturn(p2pGuid);
        when(apiBookmaker.requestQuote(raceId, slugId)).thenReturn(new Quote(oddsBookmaker, bookerGuid));
    }
    private void prepareMockReturnValuesWithDelay(BigDecimal oddsP2P, BigDecimal oddsBookmaker, int delay) {
        when(apiP2P.requestQuote(raceId, slugId, oddsP2P)).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws InterruptedException {
                Thread.sleep(delay);
                return p2pGuid;
            }
        });
        when(apiBookmaker.requestQuote(raceId, slugId)).thenReturn(new Quote(oddsBookmaker, bookerGuid));
    }
}
