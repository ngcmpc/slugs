package com.oocode;

import com.oocode.connectors.ISlugsBookmaker;
import com.oocode.connectors.ISlugsP2P;
import com.teamoptimization.*;

import java.math.BigDecimal;

public class BetPlacer {

    private final ISlugsP2P apiP2P;
    private final ISlugsBookmaker apiBookmaker;

    public BetPlacer(ISlugsP2P _p2p, ISlugsBookmaker _bookmaker) {
        apiP2P = _p2p;
        apiBookmaker = _bookmaker;
    }

//    public static void main(String[] args) {
//        /* Results usually look like a bit like one of the following:
//           Time out on SlugSwaps
//           accepted quote = 14281567-1fde-4996-a61f-0ba60b2c95c0 with offered odds 0.87
//           accepted quote = dada5f35-c244-4da6-a370-648ea35f7a03 with required odds 0.50
//        */
//
//        // Note that the names of today’s races change every day!
//        new BetPlacer(null, null).placeBet(3, "The Monday race", new BigDecimal("0.50"));
//    }

    public void placeBet(int slugId, String raceName, BigDecimal targetOdds) {

        long startTime = System.currentTimeMillis();

        String p2p = apiP2P.requestQuote(raceName, slugId, targetOdds);
        Quote b = apiBookmaker.requestQuote(raceName, slugId);

        if (p2p != null && targetOdds.compareTo(b.odds) > 0 && (System.currentTimeMillis() - startTime <= 1000)) {
            apiP2P.agree(p2p);
        } else {
            if (b.odds.compareTo(targetOdds) >= 0) {
                apiBookmaker.agree(b.uid);
            }
        }
    }
}
