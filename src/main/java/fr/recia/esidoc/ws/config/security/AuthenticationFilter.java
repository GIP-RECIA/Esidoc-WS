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

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class AuthenticationFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            log.debug("doFilter - call from request {}", ((HttpServletRequest)request).getRequestURI());
            if (((HttpServletRequest)request).getRequestURI().contains("/api/")) {
                log.debug("doFilter - try to authenticate");
                Authentication authentication = AuthenticationService.getAuthentication((HttpServletRequest) request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("doFilter - authentication is done");
            }
        } catch (Exception ex) {
            log.debug("doFilter - authentication failed with exception", ex);
        }
        log.debug("doFilter - continue filterChain");
        filterChain.doFilter(request, response);
    }
}