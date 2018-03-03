package com.oocode;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class TodoTests {
    @Test
    public void usesCheaperProviderIfOddsTheSame() throws Exception {
        // mock both APIs, both return same value
        // choose cheapest one => 1st one (SlugSwapsApi)
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
