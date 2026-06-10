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
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MappingStatusUtils {

    @Autowired
    MappingProperties mappingProperties;

    @Nullable
    public String getMainStatusMM(List<String> statusSdetList){
        statusSdetList = new ArrayList<>(statusSdetList);
        // remove all status that must not be exported to bcdi
        statusSdetList.removeAll(mappingProperties.getSdetToIgnore());
        if(statusSdetList.isEmpty()){
            return null;
        }

        String statusMM = mappingProperties.getAutreBcdiStatut();
        int bestRank = Integer.MAX_VALUE;
        for(String statusSdet: statusSdetList){
            int currentRank = getSdetRank(statusSdet);
            if(currentRank < bestRank){
                bestRank = currentRank;
                statusMM = convertSdetToStatusMM(statusSdet);
            }
        }
        return statusMM;
    }

    private String convertSdetToStatusMM(String statusSdet){
        if(mappingProperties.getSdetbcdi().containsKey(statusSdet)){
            return mappingProperties.getSdetbcdi().get(statusSdet).getMapping();
        }
        return mappingProperties.getAutreBcdiStatut();
    }

    private int getSdetRank(String statusSdet){
        if(mappingProperties.getSdetbcdi().containsKey(statusSdet)){
            return mappingProperties.getSdetbcdi().get(statusSdet).getRank();
        }
        return mappingProperties.getSdetbcdi().size()+1;
    }
}
