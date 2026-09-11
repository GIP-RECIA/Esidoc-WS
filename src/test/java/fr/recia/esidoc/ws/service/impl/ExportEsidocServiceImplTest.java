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

import fr.recia.esidoc.ws.dto.ExportEsidocPositiveResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
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
    void shouldExportUnderTheParentUaiWhenCalledWithAChildUai() throws Exception {
        final Emprunteurs fromParent = new Emprunteurs();
        fromParent.setNomPrenom("parent");
        final Emprunteurs fromChild = new Emprunteurs();
        fromChild.setNomPrenom("child");

        when(structureRegroupeeService.getUaisRegroupement("uaiChild")).thenReturn(List.of("uaiParent", "uaiChild"));
        when(structureRegroupeeService.getParentUai("uaiChild")).thenReturn("uaiParent");
        when(delayService.canSendRequestToEsidocApi("uaiParent")).thenReturn(true);
        when(mappingService.getEmprunteurs("uaiParent")).thenReturn(List.of(fromParent));
        when(mappingService.getEmprunteurs("uaiChild")).thenReturn(List.of(fromChild));
        when(mappingService.getValidatedXml(anyList())).thenReturn("<xml/>");

        final ExportEsidocPositiveResponse response = service.exportAnnuaireForUai("uaiChild");

        verify(mappingService).getEmprunteurs("uaiParent");
        verify(mappingService).getEmprunteurs("uaiChild");
        verify(mappingService).getValidatedXml(List.of(fromParent, fromChild));
        verify(delayService).canSendRequestToEsidocApi("uaiParent");
        verify(exportService).exportMappingToUai("uaiParent", "<xml/>");
        verify(delayService).applyDelayToUai("uaiParent");
        assertThat(response.getSuccessfulUais()).containsExactly("uaiParent");
    }

    @Test
    void shouldQueryOnlyTheGivenUaiWhenNotGrouped() throws Exception {
        final Emprunteurs solo = new Emprunteurs();
        solo.setNomPrenom("solo");

        when(structureRegroupeeService.getUaisRegroupement("uaiSolo")).thenReturn(List.of("uaiSolo"));
        when(structureRegroupeeService.getParentUai("uaiSolo")).thenReturn("uaiSolo");
        when(delayService.canSendRequestToEsidocApi("uaiSolo")).thenReturn(true);
        when(mappingService.getEmprunteurs("uaiSolo")).thenReturn(List.of(solo));
        when(mappingService.getValidatedXml(anyList())).thenReturn("<xml/>");

        service.exportAnnuaireForUai("uaiSolo");

        verify(mappingService).getEmprunteurs("uaiSolo");
        verify(mappingService).getValidatedXml(List.of(solo));
        verify(exportService).exportMappingToUai("uaiSolo", "<xml/>");
        verify(delayService).applyDelayToUai("uaiSolo");
    }
}