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

package aon.addons;

import aon.AON;
import aon.util.Logger;

public final class BeforeShutdown implements AddOn {

    private final String beforeShutdownScript = AON.getStringProperty("aon.beforeShutdownScript");

    @Override
    public void shutdown() {
        if (beforeShutdownScript != null) {
            try {
                Runtime.getRuntime().exec(beforeShutdownScript);
            } catch (Exception e) {
                Logger.logShutdownMessage("Failed to run after start script: " + beforeShutdownScript, e);
            }
        }
    }

}
