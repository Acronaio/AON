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

import aon.AON;
import aon.NxtException;
import aon.Poll;
import aon.Vote;
import aon.VoteWeighting;
import aon.util.JSON;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetPollVote extends APIServlet.APIRequestHandler  {
    static final GetPollVote instance = new GetPollVote();

    private GetPollVote() {
        super(new APITag[] {APITag.VS}, "poll", "account", "includeWeights");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Poll poll = ParameterParser.getPoll(req);
        long accountId = ParameterParser.getAccountId(req, true);
        boolean includeWeights = "true".equalsIgnoreCase(req.getParameter("includeWeights"));
        Vote vote = Vote.getVote(poll.getId(), accountId);
        if (vote != null) {
            int countHeight;
            JSONData.VoteWeighter weighter = null;
            if (includeWeights && (countHeight = Math.min(poll.getFinishHeight(), AON.getBlockchain().getHeight()))
                    >= AON.getBlockchainProcessor().getMinRollbackHeight()) {
                VoteWeighting voteWeighting = poll.getVoteWeighting();
                VoteWeighting.VotingModel votingModel = voteWeighting.getVotingModel();
                weighter = voterId -> votingModel.calcWeight(voteWeighting, voterId, countHeight);
            }
            return JSONData.vote(vote, weighter);
        }
        return JSON.emptyJSON;
    }
}
