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

import nxt.db.DbIterator;
import nxt.db.DerivedDbTable;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.Listener;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public final class FxtDistribution implements Listener<Block> {

//    public static final int DISTRIBUTION_END = Constants.FXT_BLOCK;
//    public static final int DISTRIBUTION_START = DISTRIBUTION_END - 90 * 1440; // run for 90 days
    public static final int DISTRIBUTION_FREQUENCY = 720; // run processing every 720 blocks
    public static final int DISTRIBUTION_STEP = 60; // take snapshots every 60 blocks
    public static final long FXT_ASSET_ID = Long.parseUnsignedLong(Constants.isTestnet ? "861080501219231688" : "12422608354438203866");
    public static final long FXT_ISSUER_ID = Convert.parseAccountId(Constants.isTestnet ? "NXT-F8FG-RDWZ-GRW7-4GSK9" : "NXT-FQ28-G9SQ-BG8M-6V6QH");
//    private static final BigInteger BALANCE_DIVIDER = BigInteger.valueOf(10000L * (DISTRIBUTION_END - DISTRIBUTION_START) / DISTRIBUTION_STEP);
    private static final String logAccount = Nxt.getStringProperty("nxt.logFxtBalance");
    private static final long logAccountId = Convert.parseAccountId(logAccount);
    private static final String fxtJsonFile = Constants.isTestnet ? "fxt-testnet.json" : "fxt.json";
    private static final boolean hasSnapshot = ClassLoader.getSystemResource(fxtJsonFile) != null;

    public static final long BITSWIFT_ASSET_ID = Long.parseUnsignedLong("12034575542068240440");
    public static final long BITSWIFT_SHAREDROP_ACCOUNT = Convert.parseAccountId("NXT-2HKA-GTP2-ZBFL-34B9L");
    public static final long JANUS_ASSET_ID = Long.parseUnsignedLong("4348103880042995903");
    public static final long JANUSXT_ASSET_ID = Long.parseUnsignedLong("14572747084550678873");
    public static final long COMJNSXT_ASSET_ID = Long.parseUnsignedLong("13363533560620557665");
    public static final Set<Long> ardorSnapshotAssets = Collections.unmodifiableSet(
            Convert.toSet(new long[] {FXT_ASSET_ID, BITSWIFT_ASSET_ID, JANUS_ASSET_ID, JANUSXT_ASSET_ID, COMJNSXT_ASSET_ID}));

    private static final DerivedDbTable accountFXTTable = new DerivedDbTable("account_fxt") {
        @Override
        public void trim(int height) {

        }
    };

    static void init() {}

    static {
        Nxt.getBlockchainProcessor().addListener(new FxtDistribution(), BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT);
    }

    @Override
    public void notify(Block block) {
    }
}
