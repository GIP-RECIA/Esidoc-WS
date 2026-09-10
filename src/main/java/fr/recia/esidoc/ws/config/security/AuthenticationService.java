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
package fr.recia.esidoc.ws.config.security;

import jakarta.servlet.http.HttpServletRequest;
import fr.recia.esidoc.ws.config.bean.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private static final String AUTH_TOKEN_HEADER_NAME = "x-api-key";

    private final SecurityProperties config;

    public Authentication getAuthentication(HttpServletRequest request) {

        final String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (!this.config.getApiKey().equals(apiKey)) {
            log.warn("Access with token '{}' isn't authorized - access from IP '{}'", apiKey, request.getRemoteAddr());
            throw new BadCredentialsException("Invalid API Key");
        }
        final String clientId = this.config.getApiKey();
        return new ApiKeyAuthentication(clientId, AuthorityUtils.NO_AUTHORITIES);
    }
}
