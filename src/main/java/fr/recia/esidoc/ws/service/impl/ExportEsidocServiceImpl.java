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
import fr.recia.esidoc.ws.exception.InvalidUAIException;
import fr.recia.esidoc.ws.exception.MappingValidationException;
import fr.recia.esidoc.ws.model.Emprunteurs;
import fr.recia.esidoc.ws.service.IExportEsidocService;
import fr.recia.esidoc.ws.service.IStructureRegroupeeService;
import fr.recia.esidoc.ws.service.delay.IDelayService;
import fr.recia.esidoc.ws.service.export.IExportService;
import fr.recia.esidoc.ws.service.mapping.IMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class ExportEsidocServiceImpl implements IExportEsidocService {


    @Autowired
    @Qualifier("uaiToUseSelector")
    Function<String, String> uaiToUseSelector;

    @Autowired
    EsidocProperties esidocProperties;

    @Autowired
    private Environment environment;

    @Autowired
    ILdapDao ldapDao;

    @Autowired
    IExportService exportService;

    @Autowired
    IMappingService mappingService;

    @Autowired
    ConfProperties confProperties;

    @Autowired
    IStructureRegroupeeService structureRegroupeeService;

    @Autowired
    IDelayService delayService;

    public ExportEsidocPositiveResponse exportAnnuaireForUai(String uai) throws GlobalExportAnnuaireException {

        List<String> uais = structureRegroupeeService.getUaisRegroupement(uai);

        if (!delayService.canSendRequestToEsidocApi(uai)) {
            throw new AlreadyExportedException("All export were already done", uai);
        }


        List<Emprunteurs> allEmprunteurs = new ArrayList<>();

        for (String uaiIterated : uais) {
            // try catch in the for so exceptions for some uai does not prevent other to be exported
            try {
                List<Emprunteurs> emprunteursList;
                emprunteursList = mappingService.getEmprunteurs(uaiIterated);
                allEmprunteurs.addAll(emprunteursList);
            } catch (Exception e) {
                if (e instanceof MappingValidationException) {
                    log.error("Mapping exception when trying to export to {}", uaiIterated, e);
                } else if (e instanceof ExportAnnuaireException) {
                    log.error("Export exception when trying to export to {}", uaiIterated, e);
                } else {
                    log.error("Unexpected exception", e);
                }
            }
        }

        try {
            String xml = mappingService.getValidatedXml(allEmprunteurs);
            exportService.exportMappingToUai(uai, xml);
        } catch (IOException | SAXException e) {
            throw new MappingValidationException("Error when trying to validate XML for " + uai);
        }


        delayService.applyDelayToUai(uai);
        return new ExportEsidocPositiveResponse(uai);
    }
}
