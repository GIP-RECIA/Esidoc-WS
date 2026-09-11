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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StructureRegroupeeServiceImplTest {

    private StructureRegroupeeServiceImpl service;

    @BeforeEach
    void setUp() {
        final ConfProperties confProperties = new ConfProperties();
        confProperties.setStructuresRegroupees(Map.of("A", List.of("B", "C")));
        service = new StructureRegroupeeServiceImpl(confProperties);
    }

    @Test
    void shouldReturnParentAsItselfWhenCalledWithParentUai() {
        assertThat(service.getParentUai("A")).isEqualTo("A");
    }

    @Test
    void shouldReturnParentWhenCalledWithChildUai() {
        assertThat(service.getParentUai("B")).isEqualTo("A");
        assertThat(service.getParentUai("C")).isEqualTo("A");
    }

    @Test
    void shouldReturnItselfAsParentWhenNotGrouped() {
        assertThat(service.getParentUai("D")).isEqualTo("D");
    }

    @Test
    void shouldReturnFullGroupRegardlessOfWhichUaiIsCalled() {
        assertThat(service.getUaisRegroupement("A")).containsExactlyInAnyOrder("A", "B", "C");
        assertThat(service.getUaisRegroupement("B")).containsExactlyInAnyOrder("A", "B", "C");
    }

    @Test
    void shouldReturnOnlyItselfWhenNotGrouped() {
        assertThat(service.getUaisRegroupement("D")).containsExactly("D");
    }
}