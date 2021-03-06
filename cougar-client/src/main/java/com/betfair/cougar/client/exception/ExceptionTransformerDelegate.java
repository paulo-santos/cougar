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

package com.betfair.cougar.client.exception;

import com.betfair.cougar.core.api.exception.ExceptionFactory;
import com.betfair.cougar.marshalling.api.databinding.DataBindingFactory;
import com.betfair.cougar.marshalling.api.databinding.FaultUnMarshaller;

import java.io.InputStream;

/**
 * Delegates the exception transformation call to the appropriate implementation
 * This class is designed to be removed when the need for cougar 1.3 dies
 */
public class ExceptionTransformerDelegate implements ExceptionTransformer {
    private ExceptionTransformer exceptionTransformer;

    private DataBindingFactory dataBindingFactory;

    public void init() {
        exceptionTransformer = new HTTPErrorToCougarExceptionTransformer(dataBindingFactory.getFaultUnMarshaller());
    }

    public void setDataBindingFactory(DataBindingFactory dataBindingFactory) {
        this.dataBindingFactory = dataBindingFactory;
    }

    @Override
    public Exception convert(InputStream inputStream, ExceptionFactory exceptionFactory, int httpStatusCode) {
        return exceptionTransformer.convert(inputStream, exceptionFactory, httpStatusCode);
    }
}
