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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final AuthenticationFilter authenticationFilter;

    public SecurityConfig(SecurityProperties securityProperties, AuthenticationFilter authenticationFilter) {
        this.securityProperties = securityProperties;
        this.authenticationFilter = authenticationFilter;
    }

    static String buildAccessExpression(List<String> authorizedIps) {
        StringBuilder hasIpAddress = new StringBuilder(
                "hasIpAddress('127.0.0.1') or hasIpAddress('::1')"
        );
        for (String ip : authorizedIps) {
            hasIpAddress.append(" or hasIpAddress('").append(ip).append("')");
        }

        return "isAuthenticated() and (" + hasIpAddress + ")";
    }

    @Bean
    @Order(1)
    SecurityFilterChain adminChain(HttpSecurity http) {
        String accessExpression = buildAccessExpression(this.securityProperties.getAuthorizedIpAccess());

        http.securityMatcher("/api/**")
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().access(new WebExpressionAuthorizationManager(accessExpression)));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain defaultChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/health-check").permitAll()
                .anyRequest().permitAll());
        return http.build();
    }
}
