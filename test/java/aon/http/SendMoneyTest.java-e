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

package aon.http;

import aon.BlockchainTest;
import aon.Constants;
import aon.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class SendMoneyTest extends BlockchainTest {

    @Test
    public void sendMoney() {
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_AON).
                param("feeNQT", Constants.ONE_AON).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);
        // Forger
        Assert.assertEquals(0, FORGY.getBalanceDiff());
        Assert.assertEquals(0, FORGY.getUnconfirmedBalanceDiff());
        // Sender
        Assert.assertEquals(0, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_AON - Constants.ONE_AON, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());
        generateBlock();
        // Forger
        Assert.assertEquals(Constants.ONE_AON, FORGY.getBalanceDiff());
        Assert.assertEquals(Constants.ONE_AON, FORGY.getUnconfirmedBalanceDiff());
        // Sender
        Assert.assertEquals(-100 * Constants.ONE_AON - Constants.ONE_AON, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_AON - Constants.ONE_AON, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(100 * Constants.ONE_AON, BOB.getBalanceDiff());
        Assert.assertEquals(100 * Constants.ONE_AON, BOB.getUnconfirmedBalanceDiff());
    }

    @Test
    public void sendTooMuchMoney() {
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", ALICE.getInitialBalance()).
                param("feeNQT", Constants.ONE_AON).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);
        Assert.assertEquals((long)6, response.get("errorCode"));
    }

    @Test
    public void sendAndReturn() {
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_AON).
                param("feeNQT", Constants.ONE_AON).
                build().invoke();
        Logger.logDebugMessage("sendMoney1: " + response);
        response = new APICall.Builder("sendMoney").
                param("secretPhrase", BOB.getSecretPhrase()).
                param("recipient", ALICE.getStrId()).
                param("amountNQT", 100 * Constants.ONE_AON).
                param("feeNQT", Constants.ONE_AON).
                build().invoke();
        Logger.logDebugMessage("sendMoney2: " + response);
        // Forger
        Assert.assertEquals(0, FORGY.getBalanceDiff());
        Assert.assertEquals(0, FORGY.getUnconfirmedBalanceDiff());
        // Sender
        Assert.assertEquals(0, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_AON - Constants.ONE_AON, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_AON - Constants.ONE_AON, BOB.getUnconfirmedBalanceDiff());
        generateBlock();
        // Forger
        Assert.assertEquals(2*Constants.ONE_AON, FORGY.getBalanceDiff());
        Assert.assertEquals(2*Constants.ONE_AON, FORGY.getUnconfirmedBalanceDiff());
        // Sender
        Assert.assertEquals(-Constants.ONE_AON, ALICE.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_AON, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(-Constants.ONE_AON, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_AON, BOB.getUnconfirmedBalanceDiff());
    }

    @Test
    public void signAndBroadcastBytes() {
        JSONObject response = new APICall.Builder("sendMoney").
                param("publicKey", ALICE.getPublicKeyStr()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_AON).
                param("feeNQT", Constants.ONE_AON).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);
        generateBlock();
        // No change transaction not broadcast
        Assert.assertEquals(0, ALICE.getBalanceDiff());
        Assert.assertEquals(0, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());

        response = new APICall.Builder("signTransaction").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("unsignedTransactionBytes", (String)response.get("unsignedTransactionBytes")).
                build().invoke();
        Logger.logDebugMessage("signTransaction: " + response);

        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", (String)response.get("transactionBytes")).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        generateBlock();

        // Sender
        Assert.assertEquals(-100 * Constants.ONE_AON - Constants.ONE_AON, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_AON - Constants.ONE_AON, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(100 * Constants.ONE_AON, BOB.getBalanceDiff());
        Assert.assertEquals(100 * Constants.ONE_AON, BOB.getUnconfirmedBalanceDiff());
    }

    @Test
    public void signAndBroadcastJSON() {
        JSONObject response = new APICall.Builder("sendMoney").
                param("publicKey", ALICE.getPublicKeyStr()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_AON).
                param("feeNQT", Constants.ONE_AON).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);
        generateBlock();
        // No change transaction not broadcast
        Assert.assertEquals(0, ALICE.getBalanceDiff());
        Assert.assertEquals(0, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());

        response = new APICall.Builder("signTransaction").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("unsignedTransactionJSON", response.get("transactionJSON").toString()).
                build().invoke();
        Logger.logDebugMessage("signTransaction: " + response);

        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", (String)response.get("transactionBytes")).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        generateBlock();

        // Sender
        Assert.assertEquals(-100 * Constants.ONE_AON - Constants.ONE_AON, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_AON - Constants.ONE_AON, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(100 * Constants.ONE_AON, BOB.getBalanceDiff());
        Assert.assertEquals(100 * Constants.ONE_AON, BOB.getUnconfirmedBalanceDiff());
    }
}
