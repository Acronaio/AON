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

package aon.peer;

import aon.Block;
import aon.AON;
import aon.NxtException;
import aon.util.Convert;
import aon.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

final class ProcessBlock extends PeerServlet.PeerRequestHandler {

    static final ProcessBlock instance = new ProcessBlock();

    private ProcessBlock() {}

    @Override
    JSONStreamAware processRequest(final JSONObject request, final Peer peer) {
        String previousBlockId = (String)request.get("previousBlock");
        Block lastBlock = AON.getBlockchain().getLastBlock();
        if (lastBlock.getStringId().equals(previousBlockId) ||
                (Convert.parseUnsignedLong(previousBlockId) == lastBlock.getPreviousBlockId()
                        && lastBlock.getTimestamp() > Convert.parseLong(request.get("timestamp")))) {
            Peers.peersService.submit(() -> {
                try {
                    AON.getBlockchainProcessor().processPeerBlock(request);
                } catch (NxtException | RuntimeException e) {
                    if (peer != null) {
                        peer.blacklist(e);
                    }
                }
            });
        }
        return JSON.emptyJSON;
    }

    @Override
    boolean rejectWhileDownloading() {
        return true;
    }

}
