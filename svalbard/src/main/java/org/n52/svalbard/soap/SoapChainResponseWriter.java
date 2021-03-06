/*
 * Copyright 2016 52°North.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.svalbard.soap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import javax.inject.Inject;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.encode.AbstractResponseWriter;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.EncoderRepository;
import org.n52.iceland.coding.encode.EncodingException;
import org.n52.iceland.coding.encode.ResponseProxy;
import org.n52.iceland.coding.encode.ResponseWriterKey;
import org.n52.iceland.coding.encode.XmlEncoderKey;
import org.n52.iceland.exception.ows.NoApplicableCodeException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.NoEncoderForKeyException;
import org.n52.iceland.response.NoContentResponse;
import org.n52.iceland.util.http.HTTPStatus;
import org.n52.iceland.w3c.soap.SoapChain;
import org.n52.iceland.w3c.soap.SoapResponse;
import org.n52.svalbard.xml.XmlOptionsHelper;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SoapChainResponseWriter extends AbstractResponseWriter<SoapChain> {

    public static final ResponseWriterKey KEY = new ResponseWriterKey(SoapChain.class);
    private final EncoderRepository encoderRepository;
    private final XmlOptionsHelper xmlOptions;

    SoapChainResponseWriter(EncoderRepository encoderRepository, XmlOptionsHelper xmlOptions) {
        this.encoderRepository = encoderRepository;
        this.xmlOptions = xmlOptions;
    }

    @Override
    public void write(SoapChain chain, OutputStream out, ResponseProxy responseProxy) throws IOException, OwsExceptionReport {
        Object o = encodeSoapResponse(chain);
        if (o != null) {
            if (o instanceof XmlObject) {
                ((XmlObject) o).save(out, this.xmlOptions.create());
            }
        }
    }

    private Object encodeSoapResponse(SoapChain chain) throws OwsExceptionReport {
        EncoderKey key = new XmlEncoderKey(chain.getSoapResponse().getSoapNamespace(), chain.getSoapResponse().getClass());
        Encoder<?, SoapResponse> encoder = this.encoderRepository.getEncoder(key);
        if (encoder != null) {
            try {
                return encoder.encode(chain.getSoapResponse());
            } catch (EncodingException ex) {
                if (ex.getCause() instanceof OwsExceptionReport) {
                    throw (OwsExceptionReport) ex.getCause();
                }
                throw new NoApplicableCodeException().withMessage(ex.getMessage()).causedBy(ex);
            }
        } else {
            throw new NoEncoderForKeyException(key);
        }
    }

    @Override
    public boolean supportsGZip(SoapChain t) {
        return false;
    }

    @Override
    public Set<ResponseWriterKey> getKeys() {
        return Collections.singleton(KEY);
    }

    @Override
    public boolean hasForcedHttpStatus(SoapChain t) {
        return t.getBodyResponse() instanceof NoContentResponse;
    }

    @Override
    public HTTPStatus getForcedHttpStatus(SoapChain t) {
        return t.getBodyResponse() instanceof NoContentResponse ? HTTPStatus.NO_CONTENT : HTTPStatus.OK;
    }

}
