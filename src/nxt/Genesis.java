/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt;

import nxt.util.Convert;

//--------------------------------FOR PROD----------------------------------------------
public final class Genesis {

    public static final long GENESIS_BLOCK_ID = 1243276750460020887L;
    public static final long CREATOR_ID = 2195323509041580970L;

    public static final byte[] CREATOR_PUBLIC_KEY = {
            -42, -100, -29, -123, 119, -67, -22, -81, 40, -117, 33, 91, -74, -69, 83, -76,
            -88, -28, -91, -104, -1, -48, -78, -39, 126, 77, 70, 105, 76, -81, -4, 112
    };

    public static final long[] GENESIS_RECIPIENTS = {
            Long.parseUnsignedLong("5406475261927130517"),
    };

    public static final String[] GENESIS_PUBLIC_KEYS = {
            "6e2700ac41acd3fe0c969e58bd3ed9f23fd8d85f0eee091729bfee5509f83b6f"
    };

    public static final int[] GENESIS_AMOUNTS = {
            21000000,
    };

    public static final byte[][] GENESIS_SIGNATURES = {
            Convert.parseHexString("d7f3f4fb2400bf68d733d3661e72758f9a6429c3cd6f650f58b62893304f1f0c2880b44387dab5fb6d75ac46a73c2ca15cb1f38695dd9a63b2a4e849946a381f")
    };

    public static final byte[] GENESIS_BLOCK_SIGNATURE = new byte[]{
            -51, 58, 53, 101, -78, -45, -118, 4, 59, -61, 127, 32, 34, 107, -8, 89, -103, -75, -123, -109, -23, 87, -69, -76, -91, 2, -120, -5, -42, 51, -119, 0, -50, 82, -25, 63, 26, 61, -100, -3, -115, -77, -58, 63, 61, 60, 80, -52, -12, -1, -104, 21, -102, 110, -109, -81, 121, -69, 67, 8, -57, -101, 113, -51
    };

    private Genesis() {} // never
}
//public final class Genesis {
//
//    public static final long GENESIS_BLOCK_ID = 1243276750460020887L;
//    public static final long CREATOR_ID = 2195323509041580970L;
//
//    public static final byte[] CREATOR_PUBLIC_KEY = {
//            -42, -100, -29, -123, 119, -67, -22, -81, 40, -117, 33, 91, -74, -69, 83, -76,
//            -88, -28, -91, -104, -1, -48, -78, -39, 126, 77, 70, 105, 76, -81, -4, 112
//    };
//
//    public static final long[] GENESIS_RECIPIENTS = {
//            Long.parseUnsignedLong("5406475261927130517"),
//            Long.parseUnsignedLong("17168038959902573891"),
//            Long.parseUnsignedLong("13366385061852681783"),
//            Long.parseUnsignedLong("10435364560610960264"),
//    };
//
//    public static final String[] GENESIS_PUBLIC_KEYS = {
////            queen mention warm shoe bid hang nerd math claim purpose grant shatter
////            AON-P3EP-MEK3-ZK4T-6AX3Q
//            "6e2700ac41acd3fe0c969e58bd3ed9f23fd8d85f0eee091729bfee5509f83b6f",
////            giant sort family pass button horizon explore difference warn group child sorry
////            AON-YKC5-H79J-4VND-G2BJW
//            "da583d5486875e774a57ac325a5b7d1daf9ff2564740cd67702a69b74c49e249",
////            down roam block scream tumble flash acid jeans impossible already trade choice
////            AON-47KR-EP5F-2W68-DJRZL
//            "51002b6496a372ecc1fe546e6eb53aa7088aa0d36991766103d45de901ddc672",
////            opinion rough city wave receive clock crowd state dry position blink expression
////            AON-PAWA-SXNG-Z2W8-BWGN3
//            "9b8f6c57ddc034f2e5264026d9b9e61e5c4d3296ccb62968f119c91e8fa00a37"
//    };
//
//    public static final int[] GENESIS_AMOUNTS = {
//            21000000,
//            500000,
//            500000,
//            500000
//    };
//
//    public static final byte[][] GENESIS_SIGNATURES = {
////            d7f3f4fb2400bf68d733d3661e72758f9a6429c3cd6f650f58b62893304f1f0c2880b44387dab5fb6d75ac46a73c2ca15cb1f38695dd9a63b2a4e849946a381f
//            Convert.parseHexString("d7f3f4fb2400bf68d733d3661e72758f9a6429c3cd6f650f58b62893304f1f0c2880b44387dab5fb6d75ac46a73c2ca15cb1f38695dd9a63b2a4e849946a381f"),
//            Convert.parseHexString("d904056e7f70688db321eaa833a76d6763105735069e060d22fa4ca2f528f008ae63ce419aa022ee5469661990d1a15c9e855fa80bdacce31731f5b277d489bc"),
//            Convert.parseHexString("39d960bbc01c7dca14e02facdfe5a94fb4d3a37f9ae96daba86e658c44bc410c4623d42233e1bdb67b5c00fa098ecc77f03eb9d8fc0d7fef4bec54a9a28529ea"),
//            Convert.parseHexString("edde504a160b10365fe71380f01656bab461388a570cf89e8d715200a2fb2a0f5ec6cda25becf2c907c86deb1413821ff24cf263cf0fd722738262efc98f4c61"),
//    };
//
////    Arrays.toString(Crypto.sign(genesisBlock.bytes(), "respond crack consider sleep strife joke rise slight better volume sadness lick"))
//    public static final byte[] GENESIS_BLOCK_SIGNATURE = new byte[]{
//        -13, 19, -99, 117, -14, 54, 25, -63, 73, 9, -126, -21, 5, -108, 7, -50, 122, -127, 39, 48, 27, -29, -32, 8, -10, -55, 87, -114, 28, -118, 92, 5, -48, 51, -30, 0, 31, 72, 109, 85, -48, -27, 33, -27, -85, 124, 45, -94, -65, -55, -78, 44, 126, 82, -118, -90, -31, -70, -22, 81, -116, 95, -74, 33
//            //            -51, 58, 53, 101, -78, -45, -118, 4, 59, -61, 127, 32, 34, 107, -8, 89, -103, -75, -123, -109, -23, 87, -69, -76, -91, 2, -120, -5, -42, 51, -119, 0, -50, 82, -25, 63, 26, 61, -100, -3, -115, -77, -58, 63, 61, 60, 80, -52, -12, -1, -104, 21, -102, 110, -109, -81, 121, -69, 67, 8, -57, -101, 113, -51
//    };
//
//    private Genesis() {} // never
//}

