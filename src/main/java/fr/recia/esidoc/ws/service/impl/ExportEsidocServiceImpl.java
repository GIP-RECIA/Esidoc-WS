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

import fr.recia.esidoc.ws.config.bean.EsidocProperties;
import fr.recia.esidoc.ws.dao.ILdapDao;
import fr.recia.esidoc.ws.exception.InvalidUAIException;
import fr.recia.esidoc.ws.model.RapportExport;
import fr.recia.esidoc.ws.service.IExportEsidocService;
import fr.recia.esidoc.ws.service.export.IExportService;
import fr.recia.esidoc.ws.service.mapping.IMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;

@Slf4j
@Service
public class ExportEsidocServiceImpl implements IExportEsidocService {


    @Autowired
    EsidocProperties esidocProperties;

    @Autowired
    private Environment environment;

    @Autowired
    private RapportExport rapportExport;

    @Autowired
    ILdapDao ldapDao;

    @Autowired
    IExportService exportService;

    @Autowired
    IMappingService mappingService;


    public void exportAnnuaireForUai(String uai) {
        checkUai(uai);
        String xml = null;
        try {
            xml = mappingService.getValidatedXmlForUai(uai);
        } catch (IOException | SAXException e) {
            rapportExport.setFailureReason(e.getMessage());
            rapportExport.setFailure(true);
            return;
        }
        String uaiToExport = environment.acceptsProfiles(Profiles.of("local","dev","test","ci"))
                ? esidocProperties.getRneDevQualif()
                : uai;
        exportService.exportMappingToUai(uaiToExport, xml);
    }


    private void checkUai(String uai){
        if(!ldapDao.isValidUai(uai)){
            throw new InvalidUAIException(String.format("Could not found strucuture with UAI %s in LDAP", uai));
        }
    }
}
