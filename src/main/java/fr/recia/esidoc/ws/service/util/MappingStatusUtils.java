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
package fr.recia.esidoc.ws.service.util;

import fr.recia.esidoc.ws.config.bean.MappingProperties;
import fr.recia.esidoc.ws.config.bean.MappingProperties.MappingRanked;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MappingStatusUtils {

    private final MappingProperties mappingProperties;

    @Nullable
    public String getMainStatusMM(List<String> statusSdetList) {
        return statusSdetList.stream()
                .min(Comparator.comparingInt(this::rankOf))
                .map(this::statusOf)
                .orElse(null);
    }

    private int rankOf(String statusSdet) {
        MappingRanked mapping = mappingProperties.getSdetbcdi().get(statusSdet);
        return mapping != null ? mapping.getRank() : Integer.MAX_VALUE;
    }

    private String statusOf(String statusSdet) {
        MappingRanked mapping = mappingProperties.getSdetbcdi().get(statusSdet);
        return mapping != null ? mapping.getMapping() : mappingProperties.getAutreBcdiStatut();
    }
}
