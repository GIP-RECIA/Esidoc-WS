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
package fr.recia.esidoc.ws.service.export.impl;


import fr.recia.esidoc.ws.config.bean.EsidocProperties;
import fr.recia.esidoc.ws.dao.ILdapDao;
import fr.recia.esidoc.ws.exception.ExportAnnuaireException;
import fr.recia.esidoc.ws.exception.InvalidUAIException;
import fr.recia.esidoc.ws.model.Emprunteurs;
import fr.recia.esidoc.ws.model.FichesXml;
import fr.recia.esidoc.ws.model.RapportExport;
import fr.recia.esidoc.ws.service.auth.token.ServiceToken;
import fr.recia.esidoc.ws.service.export.IExportEsidocService;
import fr.recia.esidoc.ws.service.util.XmlValidatorImpl;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.dataformat.xml.XmlMapper;


import java.io.File;
import java.io.IOException;
import java.util.List;


@Slf4j
@Service
public class ExportEsidocServiceImpl implements IExportEsidocService {

    private static final String EDITEUR_SLUG = "{editeur}";
    private static final String RNE_SLUG = "{rne}";
    private static final String IDENTITE_ENT_SLUG = "{identite_ent}";

    //todo work in progress from esidoc-api repo
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    EsidocProperties esidocProperties;

    @Autowired
    ServiceToken serviceToken;

    @Autowired
    private Environment environment;

    @Autowired
    private RapportExport rapportExport;

    @Autowired @Qualifier("importXSD")
    private File importXSD;

    private XmlValidatorImpl xmlValidator;

    @PostConstruct
    private void setUp() throws SAXException {
        xmlValidator = new XmlValidatorImpl(importXSD);
    }

    @Autowired
    ILdapDao ldapDao;


    public void exportAnnuaireForUai(String uai) {

        checkUai(uai);
        List<Emprunteurs> emprunteursList = getEmprunteurs(uai);

        FichesXml fichesXml = new FichesXml();
        fichesXml.setEmprunteurs(emprunteursList);

        XmlMapper mapper = new XmlMapper();

        ObjectWriter objectWriter= mapper.writerWithDefaultPrettyPrinter();
        String xmlBody =  objectWriter.writeValueAsString(fichesXml);
        String xml =
                "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\r\n"
                        + xmlBody;
        //todo change severity to debug
        log.info(xml);

        try {
            xmlValidator.validate(xml);
        } catch (IOException | SAXException e) {
            rapportExport.setFailureReason(e.getMessage());
            rapportExport.setFailure(true);
            return;
        }

        String uaiToExport = environment.acceptsProfiles(Profiles.of("local","dev","test","ci"))
                ? esidocProperties.getRneDevQualif()
                : uai;

        export(uaiToExport, xml);

    }


    private void checkUai(String uai){
        if(!ldapDao.isValidUai(uai)){
            throw new InvalidUAIException(String.format("Could not found strucuture with UAI %s in LDAP", uai));
        }
    }


    private List<Emprunteurs> getEmprunteurs(String uai){



       List<Emprunteurs> emprunteursList =  ldapDao.findAllEmprunteurs(uai);
       log.trace(emprunteursList.toString());
       return emprunteursList;
    }


    // todo work in progress request to esidoc api
    private void export(String uai, String xml){
        // If value is not in cache then we need to make a request
        log.info("xml variable in export method {}", xml);
        String url = esidocProperties.getExportAnnuaireUri().replace(RNE_SLUG, uai);
        try {
            log.debug("Requesting {}", url);
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.APPLICATION_XML);
            requestHeaders.setBearerAuth(serviceToken.getToken());
            HttpEntity<String> requestEntity = new HttpEntity<String>(xml, requestHeaders);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if(response.hasBody()){
                log.trace("Received response from API : {}", response.getBody());
            }

            ObjectMapper objectMapper = new ObjectMapper();

            assert response.getBody() != null;
            String responseBodyContent = response.getBody();



        } catch ( HttpMessageNotReadableException e) {
          // TODO adapt error message
            //   log.error("An exception occured when trying to get prets for user {}", identiteEnt, e);
            throw new ExportAnnuaireException(e.getMessage());
        }
        catch (HttpStatusCodeException e){
           rapportExport.setFailure(true);
           rapportExport.setFailureReason(String.format("Encountered error %s during POST request to %s", e.getStatusCode(), url));
        }
        catch (RestClientException e) {
            throw new ExportAnnuaireException(e.getMessage());
        }
    }
}
