package com.betfair.cougar.core.impl.interceptors;

import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.api.geolocation.GeoLocationDetails;
import com.betfair.cougar.core.api.ev.ExecutionPreProcessor;
import com.betfair.cougar.core.api.ev.InterceptorResult;
import com.betfair.cougar.core.api.ev.InterceptorState;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.core.impl.AddressClassifier;

/**
 * Prevents access to the detailed healthcheck unless we know the call has come from an internal address.
 */
public class PrivateOnlyInterceptor implements ExecutionPreProcessor {

    private AddressClassifier addressClassifier;

    public PrivateOnlyInterceptor(AddressClassifier addressClassifier) {
        this.addressClassifier = addressClassifier;
    }

    @Override
    public InterceptorResult invoke(ExecutionContext ctx, OperationKey key, Object[] args) {
        GeoLocationDetails gld = ctx.getLocation();

        if (addressClassifier.isPrivateAddress(gld.getRemoteAddr()) || addressClassifier.isLocalAddress(gld.getRemoteAddr())) {
            return new InterceptorResult(InterceptorState.CONTINUE);
        }

        return new InterceptorResult(InterceptorState.FORCE_ON_EXCEPTION, new CougarServiceException(ServerFaultCode.BannedLocation, "Access not being made from private network location"));
    }

    @Override
    public String getName() {
        return "OnlyPrivateIPs";
    }
}
