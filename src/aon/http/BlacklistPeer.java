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

import aon.http.APIServlet.APIRequestHandler;
import aon.peer.Peer;
import aon.peer.Peers;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static aon.http.JSONResponses.MISSING_PEER;
import static aon.http.JSONResponses.UNKNOWN_PEER;

public class BlacklistPeer extends APIRequestHandler {

    static final BlacklistPeer instance = new BlacklistPeer();
    
    private BlacklistPeer() {
        super(new APITag[] {APITag.NETWORK}, "peer");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) {
        JSONObject response = new JSONObject();
        
        String peerAddress = request.getParameter("peer");
        if (peerAddress == null) {
            return MISSING_PEER;
        }
        Peer peer = Peers.findOrCreatePeer(peerAddress, true);
        if (peer == null) {
            return UNKNOWN_PEER;
        } else {
            Peers.addPeer(peer);
            peer.blacklist("Manual blacklist");
            response.put("done", true);
        }
        
        return response;
    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
        return true;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

}
