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

import fr.recia.esidoc.ws.model.Emprunteurs;
import fr.recia.esidoc.ws.service.IStructureRegroupeeService;
import fr.recia.esidoc.ws.service.delay.IDelayService;
import fr.recia.esidoc.ws.service.export.IExportService;
import fr.recia.esidoc.ws.service.mapping.IMappingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportEsidocServiceImplTest {

    @Mock
    IStructureRegroupeeService structureRegroupeeService;

    @Mock
    IDelayService delayService;

    @Mock
    IMappingService mappingService;

    @Mock
    IExportService exportService;

    @InjectMocks
    ExportEsidocServiceImpl service;

    @Test
    void shouldQueryAndMergeEmprunteursFromEveryUaiOfTheGroup() throws Exception {
        final Emprunteurs fromA = new Emprunteurs();
        fromA.setNomPrenom("A");
        final Emprunteurs fromB = new Emprunteurs();
        fromB.setNomPrenom("B");

        when(structureRegroupeeService.getUaisRegroupement("uaiA")).thenReturn(List.of("uaiA", "uaiB"));
        when(delayService.canSendRequestToEsidocApi("uaiA")).thenReturn(true);
        when(mappingService.getEmprunteurs("uaiA")).thenReturn(List.of(fromA));
        when(mappingService.getEmprunteurs("uaiB")).thenReturn(List.of(fromB));
        when(mappingService.getValidatedXml(anyList())).thenReturn("<xml/>");

        service.exportAnnuaireForUai("uaiA");

        verify(mappingService).getEmprunteurs("uaiA");
        verify(mappingService).getEmprunteurs("uaiB");
        verify(mappingService).getValidatedXml(List.of(fromA, fromB));
    }

    @Test
    void shouldQueryOnlyTheGivenUaiWhenNotGrouped() throws Exception {
        final Emprunteurs solo = new Emprunteurs();
        solo.setNomPrenom("solo");

        when(structureRegroupeeService.getUaisRegroupement("uaiSolo")).thenReturn(List.of("uaiSolo"));
        when(delayService.canSendRequestToEsidocApi("uaiSolo")).thenReturn(true);
        when(mappingService.getEmprunteurs("uaiSolo")).thenReturn(List.of(solo));
        when(mappingService.getValidatedXml(anyList())).thenReturn("<xml/>");

        service.exportAnnuaireForUai("uaiSolo");

        verify(mappingService).getEmprunteurs("uaiSolo");
        verify(mappingService).getValidatedXml(List.of(solo));
    }
}
