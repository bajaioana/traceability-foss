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

package org.eclipse.tractusx.traceability.assets.infrastructure.repository.rest.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.traceability.assets.domain.metrics.RegistryLookupMeterRegistry;
import org.eclipse.tractusx.traceability.assets.domain.metrics.RegistryLookupMetric;
import org.eclipse.tractusx.traceability.assets.domain.model.ShellDescriptor;
import org.eclipse.tractusx.traceability.assets.infrastructure.repository.rest.registry.shelldescriptor.RegistryShellDescriptor;
import org.eclipse.tractusx.traceability.assets.infrastructure.repository.rest.registry.shelldescriptor.RegistryShellDescriptorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class RegistryService {

    private final ObjectMapper objectMapper;
    private final RegistryApiClient registryApiClient;
    private final String applicationBPN;
    private final String manufacturerIdKey;
    private final RegistryLookupMeterRegistry registryLookupMeterRegistry;
    private final Clock clock;


    public RegistryService(ObjectMapper objectMapper,
                           RegistryApiClient registryApiClient,
                           @Value("${traceability.bpn}") String applicationBPN,
                           @Value("${traceability.registry.manufacturerIdKey}") String manufacturerIdKey,
                           RegistryLookupMeterRegistry registryLookupMeterRegistry, Clock clock) {
        this.objectMapper = objectMapper;
        this.registryApiClient = registryApiClient;
        this.applicationBPN = applicationBPN;
        this.manufacturerIdKey = manufacturerIdKey;
        this.registryLookupMeterRegistry = registryLookupMeterRegistry;
        this.clock = clock;
    }

    public List<ShellDescriptor> findOwnShellDescriptors() {
        RegistryLookupMetric registryLookupMetric = RegistryLookupMetric.start(clock);

        log.info("Fetching all shell descriptor IDs for BPN {}.", applicationBPN);

        Map<String, Object> ownManufacturerIdBPNMap = new HashMap<>();

        ownManufacturerIdBPNMap.put("assetIds", getFilterValue(manufacturerIdKey, applicationBPN));

        final List<String> ownAssetIds;
        try {
            ownAssetIds = registryApiClient.getShellsByAssetIds(ownManufacturerIdBPNMap);
        } catch (FeignException e) {
            endMetric(registryLookupMetric);
            log.error("Fetching shell ownShellsRegistryResponse failed", e);
            throw e;
        }
        log.info("Received {} shell descriptor IDs.", ownAssetIds.size());

        log.info("Fetching shell ownShellsRegistryResponse.");

        final RegistryShellDescriptorResponse ownShellsRegistryResponse;
        try {
            ownShellsRegistryResponse = registryApiClient.fetchShellDescriptors(ownAssetIds);
        } catch (FeignException e) {
            endMetric(registryLookupMetric);

            log.error("Fetching shell ownShellsRegistryResponse failed", e);

            throw e;
        }

        log.info("Received {} shell ownShellsRegistryResponse for {} IDs.", ownShellsRegistryResponse.items().size(), ownAssetIds.size());

        List<ShellDescriptor> ownShellDescriptors = ownShellsRegistryResponse.items().stream()
                .filter(it -> Objects.nonNull(it.globalAssetId()))
                .map(aasDescriptor -> {
                    logIncomingDescriptor(aasDescriptor, registryLookupMetric);
                    return aasDescriptor.toShellDescriptor();
                })
                .peek(it -> registryLookupMetric.incrementSuccessShellDescriptorsFetchCount())
                .toList();

        log.info("Found {} shell ownShellsRegistryResponse containing a global asset ID.", ownShellDescriptors.size());

        registryLookupMetric.end(clock);

        registryLookupMeterRegistry.save(registryLookupMetric);

        return ownShellDescriptors;
    }


    private void endMetric(RegistryLookupMetric registryLookupMetric) {
        registryLookupMetric.incrementFailedShellDescriptorsFetchCount();
        registryLookupMetric.end(clock);

        registryLookupMeterRegistry.save(registryLookupMetric);
    }

    private void logIncomingDescriptor(RegistryShellDescriptor descriptor, RegistryLookupMetric registryLookupMetric) {
        if (log.isDebugEnabled()) {
            try {
                String rawDescriptor = objectMapper.writeValueAsString(descriptor);
                log.debug("Received shell descriptor: {}", rawDescriptor);
            } catch (JsonProcessingException e) {
                log.warn("Failed to write rawDescriptor {} as string", descriptor, e);
                registryLookupMetric.incrementFailedShellDescriptorsFetchCount();
            }
        }
    }

    private String getFilterValue(String key, String value) {
        return URLEncoder.encode(String.format("{\"key\":\"%s\",\"value\":\"%s\"}", key, value), StandardCharsets.UTF_8);
    }
}
