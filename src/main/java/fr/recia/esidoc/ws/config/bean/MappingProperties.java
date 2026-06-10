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
package fr.recia.esidoc.ws.config.bean;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app.mapping")
@Validated
@Data
@Slf4j
public class MappingProperties {


    private Map<String, MappingRanked> sdetbcdi;

    // people who only have some specifics status must not be sent to esidoc, they must be ignored in order to not fallback as "other"
    private List<String> sdetToIgnore;

    String eleveBcdiStatut;

    String autreBcdiStatut;

    @Override
    public String toString() {
        return "MappingProperties{" +
                "sdetbcdi=" + sdetbcdi +
                ", sdetToIgnore=" + sdetToIgnore +
                ", eleveBcdiStatut='" + eleveBcdiStatut + '\'' +
                ", autreBcdiStatut='" + autreBcdiStatut + '\'' +
                '}';
    }

    @Data
    public static class MappingRanked {


        String mapping;
        int rank;

        @Override
        public String toString() {
            return "MappingRanked{" +
                    "mapping='" + mapping + '\'' +
                    ", rank=" + rank +
                    '}';
        }
    }

    @PostConstruct
    public void init(){
        log.info(this.toString());
    }

}
