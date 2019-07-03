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

import aon.Account;
import aon.AON;
import aon.NxtException;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetGuaranteedBalance extends APIServlet.APIRequestHandler {

    static final GetGuaranteedBalance instance = new GetGuaranteedBalance();

    private GetGuaranteedBalance() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.FORGING}, "account", "numberOfConfirmations");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Account account = ParameterParser.getAccount(req);
        int numberOfConfirmations = ParameterParser.getNumberOfConfirmations(req);

        JSONObject response = new JSONObject();
        if (account == null) {
            response.put("guaranteedBalanceNQT", "0");
        } else {
            response.put("guaranteedBalanceNQT", String.valueOf(account.getGuaranteedBalanceNQT(numberOfConfirmations, AON.getBlockchain().getHeight())));
        }

        return response;
    }

}
