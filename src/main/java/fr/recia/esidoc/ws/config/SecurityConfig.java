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
package fr.recia.esidoc.ws.config;

import fr.recia.esidoc.ws.config.bean.SecurityProperties;
import fr.recia.esidoc.ws.config.security.AuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final AuthenticationFilter authenticationFilter;

    public SecurityConfig(final SecurityProperties securityProperties, final AuthenticationFilter authenticationFilter) {
        this.securityProperties = securityProperties;
        this.authenticationFilter = authenticationFilter;
    }

    static String buildAccessExpression(final List<String> authorizedIps) {
        final StringBuilder hasIpAddress = new StringBuilder(
                "hasIpAddress('127.0.0.1') or hasIpAddress('::1')"
        );
        for (final String ip : authorizedIps) {
            hasIpAddress.append(" or hasIpAddress('").append(ip).append("')");
        }

        return "isAuthenticated() and (" + hasIpAddress + ")";
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("Access denied for IP '{}' on '{}'", request.getRemoteAddr(), request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) {
        final String accessExpression = buildAccessExpression(this.securityProperties.getAuthorizedIpAccess());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(this.authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health-check").permitAll()
                        .requestMatchers("/api/**").access(new WebExpressionAuthorizationManager(accessExpression))
                        .anyRequest().denyAll())
                .exceptionHandling(handling -> handling.accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }
}