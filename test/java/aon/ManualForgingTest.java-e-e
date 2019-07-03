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

import aon.crypto.Crypto;
import aon.util.Time;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class ManualForgingTest extends AbstractForgingTest {

    @Test
    public void manualForgingTest() {
        Properties properties = ManualForgingTest.newTestProperties();
        properties.setProperty("aon.enableFakeForging", "true");
        properties.setProperty("aon.timeMultiplier", "1");
        AbstractForgingTest.init(properties);
        Assert.assertTrue("aon.fakeForgingAccount must be defined in aon.properties", AON.getStringProperty("aon.fakeForgingAccount") != null);
        final byte[] testPublicKey = Crypto.getPublicKey(testForgingSecretPhrase);
        AON.setTime(new Time.CounterTime(AON.getEpochTime()));
        try {
            for (int i = 0; i < 10; i++) {
                blockchainProcessor.generateBlock(testForgingSecretPhrase, AON.getEpochTime());
                Assert.assertArrayEquals(testPublicKey, blockchain.getLastBlock().getGeneratorPublicKey());
            }
        } catch (BlockchainProcessor.BlockNotAcceptedException e) {
            throw new RuntimeException(e.toString(), e);
        }
        Assert.assertEquals(startHeight + 10, blockchain.getHeight());
        AbstractForgingTest.shutdown();
    }

}
