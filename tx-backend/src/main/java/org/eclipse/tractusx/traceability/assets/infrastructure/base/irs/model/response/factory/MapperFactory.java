/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.traceability.assets.infrastructure.base.irs.model.response.factory;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.traceability.assets.domain.base.model.AssetBase;
import org.eclipse.tractusx.traceability.assets.domain.base.model.Descriptions;
import org.eclipse.tractusx.traceability.assets.infrastructure.base.irs.model.response.IRSResponse;
import org.eclipse.tractusx.traceability.assets.infrastructure.base.irs.model.response.IrsSubmodel;
import org.eclipse.tractusx.traceability.assets.infrastructure.base.irs.model.response.mapping.relationship.RelationshipMapper;
import org.eclipse.tractusx.traceability.assets.infrastructure.base.irs.model.response.mapping.submodel.SubmodelMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.tractusx.traceability.assets.infrastructure.base.irs.model.response.mapping.submodel.MapperHelper.getOwner;
import static org.eclipse.tractusx.traceability.assets.infrastructure.base.irs.model.response.mapping.submodel.MapperHelper.getShortId;

@Component
@RequiredArgsConstructor
public abstract class MapperFactory {

    private final List<SubmodelMapper> baseMappers;
    private final List<RelationshipMapper> relationshipMappers;

    public List<AssetBase> mapToAssetBaseList(IRSResponse irsResponse) {

        Map<String, List<Descriptions>> descriptionMap = extractRelationshipToDescriptionMap(irsResponse);


        return irsResponse
                .submodels()
                .stream()
                .map(irsSubmodel -> {
                    SubmodelMapper mapper = getMapper(irsSubmodel);
                    AssetBase.AssetBaseBuilder submodel = mapper.extractSubmodel(irsSubmodel);
                    return createAssetBase(irsResponse, submodel, descriptionMap);
                })
                .toList();
    }

    private AssetBase createAssetBase(IRSResponse irsResponse, AssetBase.AssetBaseBuilder assetBaseBuilder, Map<String, List<Descriptions>> descriptionsMap) {
        AssetBase assetBase = assetBaseBuilder.build();

        List<Descriptions> descriptions = descriptionsMap.get(assetBase.getId());

        List<Descriptions> upwardDescriptions = new ArrayList<>();
        List<Descriptions> downwardDescriptions = new ArrayList<>();

        for (Descriptions description : descriptions) {
            if (description.direction() == Direction.UPWARD) {
                upwardDescriptions.add(description);
            } else if (description.direction() == Direction.DOWNWARD) {
                downwardDescriptions.add(description);
            }
        }
        assetBase.setChildRelations(downwardDescriptions);
        assetBase.setParentRelations(upwardDescriptions);
        assetBase.setOwner(getOwner(assetBase, irsResponse));
        assetBase.setIdShort(getShortId(irsResponse.shells(), assetBase.getId()));
        return assetBase;
    }

    @NotNull
    private Map<String, List<Descriptions>> extractRelationshipToDescriptionMap(IRSResponse irsResponse) {
        Map<String, List<Descriptions>> descriptionMap = new HashMap<>();

        irsResponse.relationships().forEach(relationship -> {
            Descriptions descriptions = getRelationshipMapper(relationship).extractDescription(relationship);
            String catenaXIdString = String.valueOf(relationship.getCatenaXId());
            descriptionMap.computeIfAbsent(catenaXIdString, key -> new ArrayList<>()).add(descriptions);
        });
        return descriptionMap;
    }

    private SubmodelMapper getMapper(IrsSubmodel irsSubmodel) {
        return baseMappers.stream().filter(assetBaseMapper -> assetBaseMapper.validMapper(irsSubmodel)).findFirst().orElseThrow();
    }

    private RelationshipMapper getRelationshipMapper(Relationship relationship) {
        return relationshipMappers.stream().filter(relationshipMapper -> relationshipMapper.validMapper(relationship)).findFirst().orElseThrow();
    }

}
