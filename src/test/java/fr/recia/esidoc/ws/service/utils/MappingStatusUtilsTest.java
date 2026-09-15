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
package fr.recia.esidoc.ws.service.utils;

import fr.recia.esidoc.ws.config.bean.MappingProperties;
import fr.recia.esidoc.ws.config.bean.MappingProperties.MappingRanked;
import fr.recia.esidoc.ws.service.util.MappingStatusUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MappingStatusUtilsTest {

    MappingStatusUtils utils;

    @BeforeEach
    void setUp() {
        final MappingProperties mappingProperties = new MappingProperties();
        mappingProperties.setAutreBcdiStatut("Autre");

        final MappingRanked eleve = new MappingRanked();
        eleve.setMapping("Elève");
        eleve.setRank(1);

        final MappingRanked professeur = new MappingRanked();
        professeur.setMapping("Professeur");
        professeur.setRank(2);

        mappingProperties.setSdetbcdi(Map.of(
                "NomGroupeSDET", eleve,
                "NomGroupeSDET_B", professeur
        ));

        utils = new MappingStatusUtils(mappingProperties);
    }

    @Test
    void shouldReturnNullWhenStatusListIsEmpty() {
        assertThat(utils.getMainStatusMM(List.of())).isNull();
    }

    @Test
    void shouldReturnMappingOfBestRankedStatus() {
        final String result = utils.getMainStatusMM(List.of("NomGroupeSDET_B", "NomGroupeSDET"));

        assertThat(result).isEqualTo("Elève");
    }

    @Test
    void shouldReturnAutreStatusWhenNoneAreRanked() {
        final String result = utils.getMainStatusMM(List.of("Inconnu1", "Inconnu2"));

        assertThat(result).isEqualTo("Autre");
    }

    @Test
    void shouldPreferRankedStatusOverUnknownOnes() {
        final String result = utils.getMainStatusMM(List.of("Inconnu", "NomGroupeSDET_B"));

        assertThat(result).isEqualTo("Professeur");
    }

    @Test
    void shouldPreferRankedStatusEvenWithNonContiguousRanks() {
        final MappingRanked rare = new MappingRanked();
        rare.setMapping("StatutRare");
        rare.setRank(100);

        final MappingProperties props = new MappingProperties();
        props.setAutreBcdiStatut("Autre");
        props.setSdetbcdi(Map.of("NomGroupeRare", rare));

        final MappingStatusUtils dedicatedUtils = new MappingStatusUtils(props);

        final String result = dedicatedUtils.getMainStatusMM(List.of("Inconnu", "NomGroupeRare"));

        assertThat(result).isEqualTo("StatutRare");
    }
}
