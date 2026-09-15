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

import fr.recia.esidoc.ws.config.bean.DebugProperties;
import fr.recia.esidoc.ws.config.bean.LDAPProperties;
import fr.recia.esidoc.ws.service.bean.IExtractOpaqueId;
import fr.recia.esidoc.ws.service.bean.IExtractUIDFromDN;
import fr.recia.esidoc.ws.service.bean.impl.ExtractOpaqueIdImpl;
import fr.recia.esidoc.ws.service.bean.impl.ExtractUIDFromDNImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class EsidocWSConfiguration {

    private final LDAPProperties ldapProperties;
    private final DebugProperties debugProperties;
    private final Environment environment;


    @Bean(name = "uaiToUseSelector")
    public Function<String, String> uaiToUseSelector() {
        if (shouldUseDebugUai()) {
            log.debug("uaiToUseSelector returned is _ -> debugProperties.getDebugUai()");
            return _ -> debugProperties.getDebugUai();
        }
        log.debug("uaiToUseSelector returned is x -> x");
        return Function.identity();
    }

    private boolean shouldUseDebugUai() {
        if (debugProperties.getDebugMode() != DebugProperties.DebugMode.PROFILES) {
            return false;
        }
        Profiles profiles = Profiles.of(String.join(" | ", debugProperties.getProfiles()));
        return environment.acceptsProfiles(profiles);
    }

    @Bean
    public IExtractOpaqueId opaqueIdExtractor() {
        return new ExtractOpaqueIdImpl(ldapProperties.getUserOpaqueIdPattern());
    }

    @Bean
    public IExtractUIDFromDN uidFromDNExtractor() {
        return new ExtractUIDFromDNImpl(ldapProperties.getUserUidExtractorPattern());
    }

    @Bean
    public LdapContextSource contextSource() {
        final LdapContextSource contextSource = new LdapContextSource();

        contextSource.setAnonymousReadOnly(ldapProperties.isAnonymousReadOnly());
        contextSource.setBase(ldapProperties.getBase());
        contextSource.setUrl(ldapProperties.getUrl());
        contextSource.setUserDn(ldapProperties.getUserDn());
        contextSource.setPassword(ldapProperties.getPassword());
        contextSource.setPooled(ldapProperties.isPooled());

        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        final LdapTemplate ldapTemplate = new LdapTemplate();
        ldapTemplate.setContextSource(contextSource());
        ldapTemplate.setDefaultCountLimit(ldapProperties.getCountLimit());
        ldapTemplate.setDefaultTimeLimit(ldapProperties.getTimeout());

        return ldapTemplate;
    }

    @Bean("importXSD")
    public File importChiffreXSD() throws IOException {
        try {
            return new ClassPathResource("xsd/Import.xsd").getFile();
        } catch (Exception e) {
            throw new IllegalStateException("Le fichier xsd/Import.xsd n'a pas été trouvé.", e);
        }
    }
}
