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

import aon.db.BasicDb;
import aon.db.TransactionalDb;

public final class Db {

    public static final String PREFIX = Constants.isTestnet ? "aon.testDb" : "aon.db";
    public static final TransactionalDb db = new TransactionalDb(new BasicDb.DbProperties()
            .maxCacheSize(AON.getIntProperty("aon.dbCacheKB"))
            .dbUrl(AON.getStringProperty(PREFIX + "Url"))
            .dbType(AON.getStringProperty(PREFIX + "Type"))
            .dbDir(AON.getStringProperty(PREFIX + "Dir"))
            .dbParams(AON.getStringProperty(PREFIX + "Params"))
            .dbUsername(AON.getStringProperty(PREFIX + "Username"))
            .dbPassword(AON.getStringProperty(PREFIX + "Password", null, true))
            .maxConnections(AON.getIntProperty("aon.maxDbConnections"))
            .loginTimeout(AON.getIntProperty("aon.dbLoginTimeout"))
            .defaultLockTimeout(AON.getIntProperty("aon.dbDefaultLockTimeout") * 1000)
            .maxMemoryRows(AON.getIntProperty("aon.dbMaxMemoryRows"))
    );

    public static void init() {
        db.init(new NxtDbVersion());
    }

    static void shutdown() {
        db.shutdown();
    }

    private Db() {} // never

}
