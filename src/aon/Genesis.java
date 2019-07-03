/*
 * Copyright © 2013-2019 The AON Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the AON software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package aon;

import aon.util.Convert;

public final class Genesis {

    public static final long GENESIS_BLOCK_ID = 6560519640389173571L;
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