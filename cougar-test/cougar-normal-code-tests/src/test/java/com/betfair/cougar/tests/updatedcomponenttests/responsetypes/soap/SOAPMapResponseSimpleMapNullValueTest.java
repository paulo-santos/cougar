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

// Originally from UpdatedComponentTests/ResponseTypes/SOAP/SOAP_MapResponse_SimpleMap_NullValue.xls;
package com.betfair.cougar.tests.updatedcomponenttests.responsetypes.soap;

import com.betfair.testing.utils.cougar.misc.XMLHelpers;
import com.betfair.testing.utils.cougar.assertions.AssertionUtils;
import com.betfair.testing.utils.cougar.beans.HttpCallBean;
import com.betfair.testing.utils.cougar.beans.HttpResponseBean;
import com.betfair.testing.utils.cougar.manager.CougarManager;
import com.betfair.testing.utils.cougar.manager.RequestLogRequirement;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Test that when SOAP request operation is performed against Cougar, passing in a Map with a null value , it is correctly  processed and the correct response map is returned.
 */
public class SOAPMapResponseSimpleMapNullValueTest {
    @Test
    public void doTest() throws Exception {
        
        XMLHelpers xMLHelpers1 = new XMLHelpers();
        Document createAsDocument1 = xMLHelpers1.getXMLObjectFromString("<TestSimpleMapGetRequest><inputMap><entry key=\"aaa\"><String>Value for aaa</String></entry><entry key=\"bbb\"><String>Value for bbb</String></entry><entry key=\"ccc\"><String>Value for ccc</String></entry><entry key=\"ddd\"><String/></entry></inputMap></TestSimpleMapGetRequest>");
        
        CougarManager cougarManager2 = CougarManager.getInstance();
        HttpCallBean getNewHttpCallBean2 = cougarManager2.getNewHttpCallBean("87.248.113.14");
        cougarManager2 = cougarManager2;
        
        
        getNewHttpCallBean2.setServiceName("Baseline", "cougarBaseline");
        
        getNewHttpCallBean2.setVersion("v2");
        
        getNewHttpCallBean2.setPostObjectForRequestType(createAsDocument1, "SOAP");
        

        Timestamp getTimeAsTimeStamp7 = new Timestamp(System.currentTimeMillis());
        
        cougarManager2.makeSoapCougarHTTPCalls(getNewHttpCallBean2);
        
        XMLHelpers xMLHelpers4 = new XMLHelpers();
        Document createAsDocument9 = xMLHelpers4.getXMLObjectFromString("<response><entry key=\"aaa\"><String>Value for aaa</String></entry><entry key=\"ddd\"><String/></entry><entry key=\"ccc\"><String>Value for ccc</String></entry><entry key=\"bbb\"><String>Value for bbb</String></entry></response>");
        
        Map<String, Object> convertResponseToSOAP10 = cougarManager2.convertResponseToSOAP(createAsDocument9, getNewHttpCallBean2);
        
        HttpResponseBean response5 = getNewHttpCallBean2.getResponseObjectsByEnum(com.betfair.testing.utils.cougar.enums.CougarMessageProtocolResponseTypeEnum.SOAP);
        AssertionUtils.multiAssertEquals(convertResponseToSOAP10.get("SOAP"), response5.getResponseObject());
        
        // generalHelpers.pauseTest(2000L);
        
        
        cougarManager2.verifyRequestLogEntriesAfterDate(getTimeAsTimeStamp7, new RequestLogRequirement("2.8", "testSimpleMapGet") );
    }

}
