/*
 * Copyright 2013, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.netutil.nio;

import org.apache.mina.filter.SSLFilter;

/**
 *
 */
public class CougarProtocol3 extends CougarProtocol implements ICougarProtocol {

    public CougarProtocol3(boolean server, NioLogger nioLogger, int keepAliveInterval, int keepAliveTimeout, SSLFilter sslFilter, boolean supportsTls, boolean requiresTls, long rpcTimeoutMillis) {
        super(server, nioLogger, keepAliveInterval, keepAliveTimeout, sslFilter, supportsTls, requiresTls, rpcTimeoutMillis);
    }
}
