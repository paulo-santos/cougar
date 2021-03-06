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

package com.betfair.cougar.transport.api.protocol.http;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.ExecutionContextWithTokens;
import com.betfair.cougar.api.RequestUUID;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.api.security.IdentityChain;
import com.betfair.cougar.api.security.IdentityToken;
import com.betfair.cougar.util.RequestUUIDImpl;
import com.betfair.cougar.util.geolocation.GeoIPLocator;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generate an execution context
 */
public class ExecutionContextFactory {

    public static final String TRACE_ME_HEADER_PARAM = "X-Trace-Me";

    public static ExecutionContextWithTokens resolveExecutionContext(final HttpCommand command, final List<IdentityToken> tokens,
                                                       final String uuidHeader, GeoLocationDeserializer geoLocationDeserializer,
                                                       final GeoIPLocator geoIPLocator,
                                                       final int transportSecurityStrengthFactor) {

        return resolveExecutionContext(command, tokens, uuidHeader, geoLocationDeserializer, geoIPLocator, null, transportSecurityStrengthFactor);
    }

    public static ExecutionContextWithTokens resolveExecutionContext(final HttpCommand command, final List<IdentityToken> tokens,
                                                       final String uuidHeader, GeoLocationDeserializer geoLocationDeserializer,
                                                       final GeoIPLocator geoIPLocator,
                                                       final String inferredCountry,
                                                       final int transportSecurityStrengthFactor) {
        final HttpServletRequest request = command.getRequest();
        String uuidString = request.getHeader(uuidHeader);

        return resolveExecutionContext(tokens, uuidString, request.getRemoteAddr(), geoLocationDeserializer.deserialize(request, request.getRemoteAddr()), geoIPLocator, inferredCountry, request.getHeader(TRACE_ME_HEADER_PARAM), transportSecurityStrengthFactor);
    }

    public static ExecutionContextWithTokens resolveExecutionContext(final HttpCommand command, final List<IdentityToken> tokens,
                                                       final String uuidHeader, GeoLocationDeserializer geoLocationDeserializer,
                                                       final GeoIPLocator geoIPLocator,
                                                       final String inferredCountry,
                                                       final int transportSecurityStrengthFactor, final boolean ignoreSubsequentWritesOfIdentity) {
        final HttpServletRequest request = command.getRequest();
        String uuidString = request.getHeader(uuidHeader);

        return resolveExecutionContext(tokens, uuidString, request.getRemoteAddr(), geoLocationDeserializer.deserialize(request, request.getRemoteAddr()), geoIPLocator, inferredCountry, request.getHeader(TRACE_ME_HEADER_PARAM), transportSecurityStrengthFactor, ignoreSubsequentWritesOfIdentity);
    }

    public static ExecutionContextWithTokens resolveExecutionContext(List<IdentityToken> tokens,
                                                       final String uuidString,
                                                       final String remoteAddress,
                                                       final List<String> resolvedAddresses,
                                                       final GeoIPLocator geoIPLocator,
                                                       final String inferredCountry,
                                                       final String traceMeHeaderParamValue,
                                                       final int transportSecurityStrengthFactor) {
        return resolveExecutionContext(tokens, uuidString, remoteAddress, resolvedAddresses, geoIPLocator, inferredCountry, traceMeHeaderParamValue, transportSecurityStrengthFactor, false);
    }

    private static ExecutionContextWithTokens resolveExecutionContext(List<IdentityToken> tokens,
                                                       final String uuidString,
                                                       final String remoteAddress,
                                                       final List<String> resolvedAddresses,
                                                       final GeoIPLocator geoIPLocator,
                                                       final String inferredCountry,
                                                       final String traceMeHeaderParamValue,
                                                       final int transportSecurityStrengthFactor,
                                                       final boolean ignoreSubsequentWritesOfIdentity) {
        if (tokens == null) {
            tokens = new ArrayList<IdentityToken>();
        }
        final Date receivedTime = new Date();
        final RequestUUID requestUUID;
        if (uuidString != null) {
            requestUUID = new RequestUUIDImpl(uuidString);
        } else {
            requestUUID = new RequestUUIDImpl();
        }
        final boolean traceEnabled = traceMeHeaderParamValue != null;

        GeoLocationDetails geoDetails = geoIPLocator.getGeoLocation(remoteAddress, resolvedAddresses, inferredCountry);

        return resolveExecutionContext(tokens, requestUUID, geoDetails, receivedTime, traceEnabled, transportSecurityStrengthFactor, ignoreSubsequentWritesOfIdentity);
    }

    public static ExecutionContextWithTokens resolveExecutionContext(final List<IdentityToken> tokens, final RequestUUID requestUUID, final GeoLocationDetails geoDetails, final Date receivedTime, final boolean traceEnabled, final int transportSecurityStrengthFactor) {
        return resolveExecutionContext(tokens, requestUUID, geoDetails, receivedTime, traceEnabled, transportSecurityStrengthFactor, false);
    }

    private static ExecutionContextWithTokens resolveExecutionContext(final List<IdentityToken> tokens, final RequestUUID requestUUID, final GeoLocationDetails geoDetails, final Date receivedTime, final boolean traceEnabled, final int transportSecurityStrengthFactor, final boolean ignoreSubsequentWritesOfIdentity) {
        if (tokens == null) {
            throw new IllegalArgumentException("Tokens must not be null");
        }

        return new ExecutionContextWithTokens() {

            private IdentityChain identityChain;

            @Override
            public List<IdentityToken> getIdentityTokens() {
                return tokens;
            }

            @Override
            public IdentityChain getIdentity() {
                return identityChain;
            }

            @Override
            public GeoLocationDetails getLocation() {
                return geoDetails;
            }

            @Override
            public RequestUUID getRequestUUID() {
                return requestUUID;
            }

            @Override
            public Date getReceivedTime() {
                return receivedTime;
            }

            @Override
            public boolean traceLoggingEnabled() {
                return traceEnabled;
            }

            @Override
            public int getTransportSecurityStrengthFactor() {
                return transportSecurityStrengthFactor;
            }

            @Override
            public boolean isTransportSecure() {
                return transportSecurityStrengthFactor > 1;
            }

            @Override
            public void setIdentityChain(IdentityChain chain) {
                if (identityChain != null && !ignoreSubsequentWritesOfIdentity) {
                    throw new IllegalStateException("Can't overwrite identity chain once set");
                }
                identityChain = chain;
            }

            public String toString() {
                StringBuilder sb = new StringBuilder();

                sb.append("geoLocationDetails=").append(getLocation()).append("|");
                sb.append("tokens=").append(tokens).append("|");
                sb.append("requestUUID=").append(requestUUID).append("|");
                sb.append("receivedTime=").append(receivedTime).append("|");
                sb.append("traceLoggingEnabled=").append(traceEnabled);

                return sb.toString();
            }
        };
    }

    public static ExecutionContext resolveExecutionContext(final ExecutionContextWithTokens tokenContext, final IdentityChain chain) {
        return new ExecutionContext() {
            @Override
            public GeoLocationDetails getLocation() {
                return tokenContext.getLocation();
            }

            @Override
            public IdentityChain getIdentity() {
                return chain;
            }

            @Override
            public RequestUUID getRequestUUID() {
                return tokenContext.getRequestUUID();
            }

            @Override
            public Date getReceivedTime() {
                return tokenContext.getReceivedTime();
            }

            @Override
            public boolean traceLoggingEnabled() {
                return tokenContext.traceLoggingEnabled();
            }

            @Override
            public int getTransportSecurityStrengthFactor() {
                return tokenContext.getTransportSecurityStrengthFactor();
            }

            @Override
            public boolean isTransportSecure() {
                return tokenContext.isTransportSecure();
            }
        };
    }
}
