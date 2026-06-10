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

import fr.recia.esidoc.ws.config.bean.LDAPProperties;
import fr.recia.esidoc.ws.model.RapportExport;
import fr.recia.esidoc.ws.service.bean.IExtractOpaqueId;
import fr.recia.esidoc.ws.service.bean.IExtractUIDFromDN;
import fr.recia.esidoc.ws.service.bean.impl.ExtractOpaqueIdImpl;
import fr.recia.esidoc.ws.service.bean.impl.ExtractUIDFromDNImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.context.annotation.RequestScope;



@Slf4j
@Configuration
public class EsidocWSConfiguration {

    @Autowired
    private LDAPProperties ldapProperties;

    @Bean
    public IExtractOpaqueId opaqueIdExtractor() {
        return new ExtractOpaqueIdImpl(ldapProperties.getUserOpaqueIdPattern());
    }



    @Bean
    @RequestScope
    public RapportExport rapportExport(){
        return new RapportExport();
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
    public LdapTemplate ldapTemplate() throws Exception{
        final LdapTemplate ldapTemplate = new LdapTemplate();
        ldapTemplate.setContextSource(contextSource());
        ldapTemplate.setDefaultCountLimit(ldapProperties.getCountLimit());
        ldapTemplate.setDefaultTimeLimit(ldapProperties.getTimeout());

        return ldapTemplate;
    }
}
