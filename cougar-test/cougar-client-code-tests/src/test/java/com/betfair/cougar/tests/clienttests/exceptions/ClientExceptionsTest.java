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

// Originally from ClientTests/Transport/StandardTesting/Client_Rescript_Post_QueryParam_NonMandatory_NotSet.xls;
package com.betfair.cougar.tests.clienttests.exceptions;

import com.betfair.baseline.v2.enumerations.SimpleExceptionErrorCodeEnum;
import com.betfair.baseline.v2.exception.SimpleException;
import com.betfair.baseline.v2.to.MandatoryParamsRequest;
import com.betfair.cougar.api.ResponseCode;
import com.betfair.cougar.core.api.exception.CougarServiceException;
import com.betfair.cougar.core.api.exception.ServerFaultCode;
import com.betfair.cougar.tests.clienttests.ClientTestsHelper;
import com.betfair.cougar.tests.clienttests.CougarClientWrapper;
import junit.framework.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * Ensure that when a request with a non mandatory query parameter not set is performed against cougar via a cougar client the request is sent and the response is handled correctly
 */
public class ClientExceptionsTest {

    @Test(dataProvider = "TransportType")
    public void runtimeException(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);
        // Create body parameter to be passed
        MandatoryParamsRequest request = new MandatoryParamsRequest();
        request.setBodyParameter1("postBodyParamString1");

        request.setBodyParameter2("postBodyParamString2");
        // Make call to the method via client and validate the response is as expected
        try {
            wrapper.getClient().testException(wrapper.getCtx(), "abc", "throwRuntime");
            fail("Expected exception");
        }
        catch (CougarServiceException cse) {
            assertEquals(ServerFaultCode.ServiceRuntimeException, cse.getServerFaultCode());
            assertEquals("Server fault received from remote server: ServiceRuntimeException(DSC-0005)", cse.getMessage());
            // this is optional
            if (cse.getCause() != null) {
                assertEquals("Exception thrown by service method", cse.getCause().getMessage());
            }
        }
    }
    @Test(dataProvider = "TransportType")
    public void serviceException(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);
        // Create body parameter to be passed
        MandatoryParamsRequest request = new MandatoryParamsRequest();
        request.setBodyParameter1("postBodyParamString1");

        request.setBodyParameter2("postBodyParamString2");
        // Make call to the method via client and validate the response is as expected
        try {
            wrapper.getClient().testException(wrapper.getCtx(), "BusinessException", "GENERIC");
            fail("Expected exception");
        }
        catch (SimpleException se) {
            assertEquals(ResponseCode.BusinessException, se.getResponseCode());
            assertEquals(SimpleExceptionErrorCodeEnum.GENERIC, se.getErrorCode());
            assertEquals("GENERIC", se.getReason());
            assertEquals("SEX-0001", se.getExceptionCode());
            Assert.assertEquals("responseCode=BusinessException, errorCode=GENERIC (SEX-0001), reason=GENERIC", se.getMessage());
        }
    }
    @Test(dataProvider = "TransportType")
    public void readTimedOut(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);
        // Make call to the method via client and validate the response is as expected
        try {
            // this is gonna timeout - transport timeouts are set at 30s
            // we have the timeout set at 24h so it doesn't randomly log in the middle of another test whcih doesn't expect it
            wrapper.getClient().testSleep(wrapper.getCtx(), 86400000L);
            fail("Expected exception");
        }
        catch (CougarServiceException cse) {
            cse.printStackTrace();
            assertEquals(ServerFaultCode.Timeout, cse.getServerFaultCode());

            assertOneOf("Exception occurred in Client: Read timed out: ", getUrls(tt, "sleep?sleep=86400000"), cse.getMessage());
            // this is optional
            if (cse.getCause() != null) {
                assertEquals("Read timed out", cse.getCause().getMessage());
            }
        }
        finally {
            wrapper.getClient().cancelSleeps(wrapper.getCtx());
        }
    }

    private void assertOneOf(String prefix, String[] expected, String actual) {
        boolean found = false;
        for (String e : expected) {
            if ((prefix+e).equals(actual)) {
                found = true;
                break;
            }
        }
        assertTrue("String '"+actual+"' not found in: "+prefix+"+"+ Arrays.toString(expected), found);
    }

    private String[] getUrls(CougarClientWrapper.TransportType tt, String httpOperationPathAndParams) {
        switch (tt.getUnderlyingTransport()) {
            case HTTP:
                if (tt.isSecure()) {
                    return new String[] { "https://127.0.0.1:8443/cougarBaseline/v2/simple/"+httpOperationPathAndParams };
                }
                else {
                    return new String[] { "http://127.0.0.1:8080/cougarBaseline/v2/simple/"+httpOperationPathAndParams };
                }
            case Socket:
                return new String[] {
                        "tcp" + (tt.isSecure()?"+ssl":"") + "://localhost:9003" ,
                        "tcp" + (tt.isSecure()?"+ssl":"") + "://localhost.localdomain:9003" // to work around build server shenanigans
                };
            default:
                throw new IllegalArgumentException("Unknown transport: "+tt);
        }

    }

    @Test(dataProvider = "TransportType")
    public void operationTimedOut(CougarClientWrapper.TransportType tt) throws Exception {
        CougarClientWrapper wrapper = CougarClientWrapper.getInstance(tt);
        // Make call to the method via client and validate the response is as expected
        try {
            // this is gonna timeout!
            wrapper.getClient().testSleep(wrapper.getCtx(), 1000L, 1L);
            if (tt.isAsync()) {
                fail("Expected exception");
            }
        }
        catch (TimeoutException te) {
            if (tt.isAsync()) {
                assertEquals("Operation testSleep timed out!", te.getMessage());
            }
            else {
                Assert.fail("Don't expect a timeout on a sync transport");
            }
        }
    }

    @DataProvider(name="TransportType")
    public Object[][] clients() {
        return ClientTestsHelper.clientsToTest();
    }

}
