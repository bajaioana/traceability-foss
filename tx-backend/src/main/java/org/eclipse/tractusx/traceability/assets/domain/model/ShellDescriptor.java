/********************************************************************************
 * Copyright (c) 2022, 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2022, 2023 ZF Friedrichshafen AG
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.traceability.assets.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Collections;


@Data
@Builder
public class ShellDescriptor {
    private String shellDescriptorId;
    private String globalAssetId;
    private String idShort;
    private String partInstanceId;
    private String manufacturerPartId;
    private String manufacturerId;
    private String batchId;

    private static String defaultValue(String value) {
        final String EMPTY_TEXT = "--";
        if (!StringUtils.hasText(value)) {
            return EMPTY_TEXT;
        }
        return value;
    }

    public Asset toAsset(String manufacturerName) {
        return Asset.builder()
                .id(getGlobalAssetId())
                .idShort(getIdShort())
                .nameAtManufacturer(getIdShort())
                .manufacturerPartId(defaultValue(getManufacturerPartId()))
                .partInstanceId(defaultValue(getPartInstanceId()))
                .manufacturerId(defaultValue(getManufacturerId()))
                .batchId(defaultValue(getBatchId()))
                .manufacturerName(manufacturerName)
                .nameAtCustomer(getIdShort())
                .customerPartId(getManufacturerPartId())
                .manufacturingCountry("--")
                .owner(Owner.OWN)
                .childDescriptions(Collections.emptyList())
                .parentDescriptions(Collections.emptyList())
                .underInvestigation(false)
                .qualityType(QualityType.OK)
                .van("--")
                .build();
    }

}
