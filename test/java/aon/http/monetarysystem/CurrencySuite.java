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

package aon.http.monetarysystem;

import aon.http.AbstractHttpApiSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestCurrencyIssuance.class,
        TestCurrencyExchange.class,
        TestCurrencyReserveAndClaim.class,
        TestCurrencyMint.class,
        aon.TestMintCalculations.class,
        DeleteCurrencyTest.class,
})

public class CurrencySuite extends AbstractHttpApiSuite { }