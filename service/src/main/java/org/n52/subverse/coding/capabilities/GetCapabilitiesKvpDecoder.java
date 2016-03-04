/*
 * Copyright 2015 52°North Initiative for Geospatial Open Source Software GmbH.
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
package org.n52.subverse.coding.capabilities;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.exception.ows.InvalidParameterValueException;
import org.n52.iceland.exception.ows.MissingParameterValueException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.request.GetCapabilitiesRequest;
import org.n52.iceland.util.KvpHelper;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.coding.KvpDecoder;

public class GetCapabilitiesKvpDecoder extends KvpDecoder<GetCapabilitiesRequest> {

    private static final Set<DecoderKey> KEYS = Sets.newHashSet(createKey(SubverseConstants.SERVICE, null, OWSConstants.Operations.GetCapabilities.toString()),
            createKey(SubverseConstants.SERVICE, SubverseConstants.VERSION, OWSConstants.Operations.GetCapabilities.toString()));

    @Override
    public Set<DecoderKey> getKeys() {
        return Collections.unmodifiableSet(KEYS);
    }

    @Override
    protected GetCapabilitiesRequest createRequest() {
        return new GetCapabilitiesRequest(SubverseConstants.SERVICE);
    }

    @Override
    protected void decodeParameter(GetCapabilitiesRequest request, String name, String values) throws OwsExceptionReport {
        switch (name.toLowerCase()) {
            case SubverseConstants.OperationParameter.service:
                request.setService(KvpHelper.checkParameterSingleValue(values, name));
                break;
            case SubverseConstants.OperationParameter.request:
                KvpHelper.checkParameterSingleValue(values, name);
                break;
            case SubverseConstants.GetCapabilitiesParameter.acceptversions:
                if (values.isEmpty()) {
                    throw new MissingParameterValueException(name);
                }
                request.setAcceptVersions(Arrays.asList(values.split(",")));
                break;
//            case ACCEPT_FORMATS:
//                request.setAcceptFormats(KvpHelper.checkParameterMultipleValues(values, name));
//                break;
//            case UPDATE_SEQUENCE:
//                request.setUpdateSequence(KvpHelper.checkParameterSingleValue(values, name));
//                break;
//            case SECTIONS:
//                request.setSections(KvpHelper.checkParameterMultipleValues(values, name));
//                break;
//            case LANGUAGE:
//                Extension<String> le = new LanguageExtension(KvpHelper.checkParameterSingleValue(values, name));
//                request.addExtension(le);
//                break;
            default:
                throw new InvalidParameterValueException(name, values).withMessage("The parameter '%s' is not supported.", name); // OptionNotSupportedException (and thereby ParameterNotSupportedException) are OWS 1.1.0
        }
    }

}