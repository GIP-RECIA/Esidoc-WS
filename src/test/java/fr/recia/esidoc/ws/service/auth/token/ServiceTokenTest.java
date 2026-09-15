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
package fr.recia.esidoc.ws.service.auth.token;

import fr.recia.esidoc.ws.config.bean.OAuth2Properties;
import fr.recia.esidoc.ws.dto.TokenResponsePayload;
import fr.recia.esidoc.ws.exception.TokenFetchFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceTokenTest {

    @Mock
    RestTemplate restTemplate;

    OAuth2Properties oAuth2Properties;
    ServiceToken serviceToken;

    @BeforeEach
    void setUp() {
        oAuth2Properties = new OAuth2Properties();
        oAuth2Properties.setAuthorizationGrantType("client_credentials");
        oAuth2Properties.setClientId("client-id");
        oAuth2Properties.setClientSecret("secret");
        oAuth2Properties.setOauth2TokenUri("https://auth.example/token");

        serviceToken = new ServiceToken(restTemplate, oAuth2Properties);
    }

    @Test
    void shouldFetchTokenWhenNoneCached() {
        TokenResponsePayload payload = new TokenResponsePayload();
        payload.setAccessToken("abc123");
        payload.setExpiresIn(3600);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(TokenResponsePayload.class)))
                .thenReturn(ResponseEntity.ok(payload));

        final String token = serviceToken.getToken();

        assertThat(token).isEqualTo("abc123");
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),eq(TokenResponsePayload.class));
    }

    @Test
    void shouldNotRefetchWhenCachedTokenStillValid() {
        serviceToken.tokenHolder = new ServiceToken.TokenHolder("cached-token", Instant.now().plusSeconds(60));

        final String token = serviceToken.getToken();

        assertThat(token).isEqualTo("cached-token");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldRefetchWhenCachedTokenExpired() {
        serviceToken.tokenHolder = new ServiceToken.TokenHolder("old-token", Instant.now().minusSeconds(1));
        TokenResponsePayload payload = new TokenResponsePayload();
        payload.setAccessToken("fresh-token");
        payload.setExpiresIn(3600);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),eq(TokenResponsePayload.class)))
                .thenReturn(ResponseEntity.ok(payload));

        final String token = serviceToken.getToken();

        assertThat(token).isEqualTo("fresh-token");
    }

    @Test
    void shouldThrowWhenRestCallFails() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),eq(TokenResponsePayload.class)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> serviceToken.getToken())
                .isInstanceOf(TokenFetchFailedException.class);
    }
}
