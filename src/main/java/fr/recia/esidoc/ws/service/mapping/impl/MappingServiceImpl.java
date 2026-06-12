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
package fr.recia.esidoc.ws.service.mapping.impl;

import fr.recia.esidoc.ws.dao.ILdapDao;
import fr.recia.esidoc.ws.model.Emprunteurs;
import fr.recia.esidoc.ws.model.FichesXml;
import fr.recia.esidoc.ws.service.mapping.IMappingService;
import fr.recia.esidoc.ws.service.util.XmlValidatorImpl;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class MappingServiceImpl implements IMappingService {

    @Autowired @Qualifier("importXSD")
    private File importXSD;

    private XmlValidatorImpl xmlValidator;

    @PostConstruct
    private void setUp() throws SAXException {
        xmlValidator = new XmlValidatorImpl(importXSD);
    }

    @Autowired
    ILdapDao ldapDao;

    @Override
    public String getValidatedXmlForUai(String uai) throws IOException, SAXException {
        List<Emprunteurs> emprunteursList =  ldapDao.findAllEmprunteurs(uai);
        log.trace(emprunteursList.toString());

        FichesXml fichesXml = new FichesXml();
        fichesXml.setEmprunteurs(emprunteursList);

        XmlMapper mapper = new XmlMapper();

        ObjectWriter objectWriter= mapper.writerWithDefaultPrettyPrinter();
        String xmlBody =  objectWriter.writeValueAsString(fichesXml);
        String xml =
                "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\r\n"
                        + xmlBody;
        log.debug(xml);
            xmlValidator.validate(xml);
        return xml;
    }
}
