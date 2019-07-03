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
import aon.util.Listener;
import aon.util.Logger;
import org.junit.Assert;

import java.util.Properties;

public abstract class AbstractBlockchainTest {

    protected static AONEngineBlockchainProcessor blockchainProcessor;
    protected static BlockchainImpl blockchain;
    private static final Object doneLock = new Object();
    private static boolean done = false;

    protected static Properties newTestProperties() {
        Properties testProperties = new Properties();
        testProperties.setProperty("nxt.shareMyAddress", "false");
        testProperties.setProperty("nxt.savePeers", "false");
        //testProperties.setProperty("aon.enableAPIServer", "false");
        //testProperties.setProperty("aon.enableUIServer", "false");
        testProperties.setProperty("aon.disableGenerateBlocksThread", "true");
        //testProperties.setProperty("aon.disableProcessTransactionsThread", "true");
        //testProperties.setProperty("aon.disableRemoveUnconfirmedTransactionsThread", "true");
        //testProperties.setProperty("aon.disableRebroadcastTransactionsThread", "true");
        //testProperties.setProperty("aon.disablePeerUnBlacklistingThread", "true");
        //testProperties.setProperty("aon.getMorePeers", "false");
        testProperties.setProperty("aon.testUnconfirmedTransactions", "true");
        testProperties.setProperty("nxt.debugTraceAccounts", "");
        testProperties.setProperty("nxt.debugLogUnconfirmed", "false");
        testProperties.setProperty("nxt.debugTraceQuote", "\"");
        //testProperties.setProperty("aon.numberOfForkConfirmations", "0");
        return testProperties;
    }

    protected static void init(Properties testProperties) {
        AON.init(testProperties);
        blockchain = BlockchainImpl.getInstance();
        blockchainProcessor = AONEngineBlockchainProcessor.getInstance();
        blockchainProcessor.setGetMoreBlocks(false);
        AONTransactionEngineProcessor.getInstance().clearUnconfirmedTransactions();
        Listener<Block> countingListener = block -> {
            if (block.getHeight() % 1000 == 0) {
                Logger.logMessage("downloaded block " + block.getHeight());
            }
        };
        blockchainProcessor.addListener(countingListener, BlockchainProcessor.Event.BLOCK_PUSHED);
    }

    protected static void shutdown() {
        AONTransactionEngineProcessor.getInstance().clearUnconfirmedTransactions();
    }

    protected static void downloadTo(final int endHeight) {
        if (blockchain.getHeight() == endHeight) {
            return;
        }
        Assert.assertTrue(blockchain.getHeight() < endHeight);
        Listener<Block> stopListener = block -> {
            if (blockchain.getHeight() == endHeight) {
                synchronized (doneLock) {
                    done = true;
                    blockchainProcessor.setGetMoreBlocks(false);
                    doneLock.notifyAll();
                    throw new NxtException.StopException("Reached height " + endHeight);
                }
            }
        };
        blockchainProcessor.addListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
        synchronized (doneLock) {
            done = false;
            Logger.logMessage("Starting download from height " + blockchain.getHeight());
            blockchainProcessor.setGetMoreBlocks(true);
            while (! done) {
                try {
                    doneLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Assert.assertEquals(endHeight, blockchain.getHeight());
        blockchainProcessor.removeListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
    }

    protected static void forgeTo(final int endHeight, final String secretPhrase) {
        if (blockchain.getHeight() == endHeight) {
            return;
        }
        Assert.assertTrue(blockchain.getHeight() < endHeight);
        Listener<Block> stopListener = block -> {
            if (blockchain.getHeight() == endHeight) {
                synchronized (doneLock) {
                    done = true;
                    Generator.stopForging(secretPhrase);
                    doneLock.notifyAll();
                }
            }
        };
        blockchainProcessor.addListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
        synchronized (doneLock) {
            done = false;
            Logger.logMessage("Starting forging from height " + blockchain.getHeight());
            Generator.startForging(secretPhrase);
            while (! done) {
                try {
                    doneLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Assert.assertTrue(blockchain.getHeight() >= endHeight);
        Assert.assertArrayEquals(Crypto.getPublicKey(secretPhrase), blockchain.getLastBlock().getGeneratorPublicKey());
        blockchainProcessor.removeListener(stopListener, BlockchainProcessor.Event.BLOCK_PUSHED);
    }

    protected int getHeight() {
        return blockchain.getHeight();
    }
}
