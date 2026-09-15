/*
 * Copyright © 2026 GIP-RECIA (https://www.recia.fr/)
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
package fr.recia.esidoc.ws.service.impl;

import fr.recia.esidoc.ws.config.bean.ConfProperties;
import fr.recia.esidoc.ws.service.IStructureRegroupeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StructureRegroupeeServiceImpl implements IStructureRegroupeeService {

    private final ConfProperties confProperties;

    @Override
    public List<String> getUaisRegroupement(final String uai) {
        final List<String> groupePartage = findStructuresPartagees(uai);
        if (!groupePartage.isEmpty()) {
            return groupePartage;
        }

        final String parentUai = getParentUai(uai);
        final List<String> children = confProperties.getStructuresRegroupees().get(parentUai);
        if (children == null) {
            return List.of(uai);
        }
        final List<String> uais = new ArrayList<>();
        uais.add(parentUai);
        uais.addAll(children);
        return uais;
    }

    @Override
    public String getParentUai(final String uai) {
        if (!findStructuresPartagees(uai).isEmpty()) {
            return uai;
        }
        if (confProperties.getStructuresRegroupees().containsKey(uai)) {
            return uai;
        }
        for (final Map.Entry<String, List<String>> entry : confProperties.getStructuresRegroupees().entrySet()) {
            if (entry.getValue().contains(uai)) {
                return entry.getKey();
            }
        }
        return uai;
    }

    private List<String> findStructuresPartagees(final String uai) {
        for (final List<String> groupe : confProperties.getStructuresPartagees()) {
            if (groupe.contains(uai)) {
                return groupe;
            }
        }
        return List.of();
    }

}