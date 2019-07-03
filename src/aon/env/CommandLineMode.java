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

package aon.env;

import java.io.File;
import java.net.URI;

public class CommandLineMode implements RuntimeMode {

    @Override
    public void init() {}

    @Override
    public void setServerStatus(ServerStatus status, URI wallet, File logFileDir) {}

    @Override
    public void launchDesktopApplication() {}

    @Override
    public void shutdown() {}

    @Override
    public void alert(String message) {}
}