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
import fr.recia.esidoc.ws.config.bean.EsidocProperties;
import fr.recia.esidoc.ws.dao.ILdapDao;
import fr.recia.esidoc.ws.dto.ExportEsidocPositiveResponse;
import fr.recia.esidoc.ws.exception.AlreadyExportedException;
import fr.recia.esidoc.ws.exception.ExportAnnuaireException;
import fr.recia.esidoc.ws.exception.GlobalExportAnnuaireException;
import fr.recia.esidoc.ws.exception.MappingValidationException;
import fr.recia.esidoc.ws.model.Emprunteurs;
import fr.recia.esidoc.ws.service.IExportEsidocService;
import fr.recia.esidoc.ws.service.IStructureRegroupeeService;
import fr.recia.esidoc.ws.service.delay.IDelayService;
import fr.recia.esidoc.ws.service.export.IExportService;
import fr.recia.esidoc.ws.service.mapping.IMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportEsidocServiceImpl implements IExportEsidocService {


    private final EsidocProperties esidocProperties;
    private final Function<String, String> uaiToUseSelector;
    private final ILdapDao ldapDao;
    private final IExportService exportService;
    private final IMappingService mappingService;
    private final ConfProperties confProperties;
    private final IStructureRegroupeeService structureRegroupeeService;
    private final IDelayService delayService;


    public ExportEsidocPositiveResponse exportAnnuaireForUai(String uai) throws GlobalExportAnnuaireException {

        List<String> uais = structureRegroupeeService.getUaisRegroupement(uai);
        String parentUai = uaiToUseSelector.apply(structureRegroupeeService.getParentUai(uai));

        if (!delayService.canSendRequestToEsidocApi(parentUai)) {
            throw new AlreadyExportedException("All export were already done", parentUai);
        }


        List<Emprunteurs> allEmprunteurs = new ArrayList<>();

        for (String uaiIterated : uais) {
            // try catch in the for so exceptions for some uai does not prevent other to be exported
            try {
                List<Emprunteurs> emprunteursList;
                emprunteursList = mappingService.getEmprunteurs(uaiIterated);
                allEmprunteurs.addAll(emprunteursList);
            } catch (MappingValidationException e) {
                log.error("Mapping exception when trying to export to {}", uaiIterated, e);
            } catch (ExportAnnuaireException e) {
                log.error("Export exception when trying to export to {}", uaiIterated, e);
            } catch (Exception e) {
                log.error("Unexpected exception", e);
            }
        }

        try {
            String xml = mappingService.getValidatedXml(allEmprunteurs);
            exportService.exportMappingToUai(parentUai, xml);
        } catch (IOException | SAXException e) {
            throw new MappingValidationException("Error when trying to validate XML for " + parentUai, e);
        }

        delayService.applyDelayToUai(parentUai);
        return new ExportEsidocPositiveResponse(parentUai);
    }
}