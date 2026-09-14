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
import fr.recia.esidoc.ws.service.auth.token.ServiceToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    ServiceToken serviceToken;

    @Captor
    ArgumentCaptor<HttpEntity<String>> captor;

    ExportServiceImpl exportService;

    @BeforeEach
    void setUp() {
        final EsidocProperties esidocProperties = new EsidocProperties();
        esidocProperties.setEditeur("editeur");
        esidocProperties.setExportAnnuaireUri("https://esidoc.example/{rne}");
        exportService = new ExportServiceImpl(restTemplate, esidocProperties, serviceToken);
    }

    @Test
    void shouldPostXmlWithBearerTokenAndReturnResponseBody() {
        when(serviceToken.getToken()).thenReturn("token123");
        when(restTemplate.exchange(eq("https://esidoc.example/uai1"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("<result/>"));

        final String result = exportService.exportMappingToUai("uai1", "<xml/>");

        assertThat(result).isEqualTo("<result/>");

        verify(restTemplate).exchange(eq("https://esidoc.example/uai1"), eq(HttpMethod.POST), captor.capture(), eq(String.class));
        assertThat(captor.getValue().getBody()).isEqualTo("<xml/>");
        assertThat(captor.getValue().getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer token123");
    }

    @Test
    void shouldWrapHttpClientErrorIntoExportAnnuaireException() {
        when(serviceToken.getToken()).thenReturn("token123");
        final HttpClientErrorException exception = mock(HttpClientErrorException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAs(EsidocError.class)).thenReturn(null);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        assertThatThrownBy(() -> exportService.exportMappingToUai("uai1", "<xml/>"))
                .isInstanceOf(ExportAnnuaireException.class);
    }

    @Test
    void shouldWrapHttpServerErrorIntoExportAnnuaireException() {
        when(serviceToken.getToken()).thenReturn("token123");
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> exportService.exportMappingToUai("uai1", "<xml/>"))
                .isInstanceOf(ExportAnnuaireException.class);
    }

    @Test
    void shouldWrapConnectionFailureIntoExportAnnuaireException() {
        when(serviceToken.getToken()).thenReturn("token123");
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        assertThatThrownBy(() -> exportService.exportMappingToUai("uai1", "<xml/>"))
                .isInstanceOf(ExportAnnuaireException.class);
    }
}