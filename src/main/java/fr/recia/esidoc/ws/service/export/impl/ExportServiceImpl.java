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
import fr.recia.esidoc.ws.exception.ExportAnnuaireException;
import fr.recia.esidoc.ws.model.EsidocError;
import fr.recia.esidoc.ws.model.RapportExport;
import fr.recia.esidoc.ws.service.auth.token.ServiceToken;
import fr.recia.esidoc.ws.service.export.IExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
public class ExportServiceImpl implements IExportService {

    private static final String RNE_SLUG = "{rne}";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    EsidocProperties esidocProperties;

    @Autowired
    ServiceToken serviceToken;

    @Autowired
    private RapportExport rapportExport;

    @Override
    public void exportMappingToUai(String uai, String xml){
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
                rapportExport.setEsidocApiResponse(response.getBody());
            }
            assert response.getBody() != null;
        } catch ( HttpMessageNotReadableException e) {
               log.error("An exception occured when trying to send XML to Esidoc", e);
            rapportExport.setFailure(true);
            rapportExport.setEsidocApiResponse(e.getMessage());
            rapportExport.setFailureReason(String.format("Encountered error %s during POST request to %s", e.getMessage(), url));
        }
        catch (HttpStatusCodeException e){
            if(e.getStatusCode().equals(HttpStatus.BAD_REQUEST)){
                log.error("Esidoc error : {}",  e.getResponseBodyAs(EsidocError.class));
            }

           rapportExport.setFailure(true);
           rapportExport.setEsidocApiResponse(e.getResponseBodyAsString());
           rapportExport.setFailureReason(String.format("Encountered error %s during POST request to %s", e.getStatusCode(), url));
        }
        catch (RestClientException e) {
            throw new ExportAnnuaireException(e.getMessage());
        }
    }
}
