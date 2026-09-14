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
package fr.recia.esidoc.ws.dao.impl;


import fr.recia.esidoc.ws.config.bean.Filters;
import fr.recia.esidoc.ws.config.bean.LDAPProperties;
import fr.recia.esidoc.ws.config.bean.MappingProperties;
import fr.recia.esidoc.ws.model.Emprunteurs;
import fr.recia.esidoc.ws.service.bean.IExtractOpaqueId;
import fr.recia.esidoc.ws.service.bean.IExtractUIDFromDN;
import fr.recia.esidoc.ws.service.util.MappingStatusUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LdapDaoImplTest {
    @Mock
    LdapTemplate ldapTemplate;

    @Mock
    IExtractOpaqueId extractOpaqueId;

    @Mock
    MappingStatusUtils mappingStatusUtils;

    @Mock
    MappingProperties mappingProperties;

    @Mock
    IExtractUIDFromDN extractUIDFromDN;

    LdapDaoImpl dao;

    @BeforeEach
    void setUp(){
        final Filters filters = new Filters();
        filters.setEmprunteurs("(uid=%s)");
        filters.setParents("(uid=%s)");
        filters.setValidUai("(uai=%s)");

        final LDAPProperties ldapProperties = new LDAPProperties();
        ldapProperties.setPeopleRootDn("ou=people");
        ldapProperties.setStructureRootDn("ou=structures");
        ldapProperties.setAutorizedResponsablePattern(".*");
        ldapProperties.setEleveGroupePattern("GROUPE-%s");
        ldapProperties.setFilters(filters);

        dao = new LdapDaoImpl(ldapTemplate, ldapProperties, extractOpaqueId, mappingStatusUtils,
                mappingProperties, new HashMap<>(), extractUIDFromDN);
    }
    @Test
    void shouldReturnFirstSirenFound() {
        when(ldapTemplate.search(any(LdapQuery.class), isA(EtabAttributesMapper.class))).thenReturn(List.of("siren123"));

        final String siren = dao.getSirenForUai("uai1");

        assertThat(siren).isEqualTo("siren123");
    }

    @Test
    void shouldThrowWhenNoEtabFoundForUai() {
        when(ldapTemplate.search(any(LdapQuery.class), isA(EtabAttributesMapper.class))).thenReturn(List.of());

        assertThatThrownBy(() -> dao.getSirenForUai("uaiInconnu"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldQueryParentsSirenAndPeopleThenFilterOutNulls() {
        final Emprunteurs person = new Emprunteurs();
        person.setNomPrenom("Jean Dupont");

        when(ldapTemplate.search(any(LdapQuery.class), isA(ParentsAttributesMapper.class))).thenReturn(List.of());
        when(ldapTemplate.search(any(LdapQuery.class), isA(EtabAttributesMapper.class))).thenReturn(List.of("siren123"));
        when(ldapTemplate.search(any(LdapQuery.class), isA(EmprunteursAttributesMapper.class))).thenReturn(Arrays.asList(person, null));

        final List<Emprunteurs> result = dao.findAllEmprunteurs("uai1");

        assertThat(result).containsExactly(person);
        verify(ldapTemplate).search(any(LdapQuery.class), isA(ParentsAttributesMapper.class));
        verify(ldapTemplate).search(any(LdapQuery.class), isA(EtabAttributesMapper.class));
        verify(ldapTemplate).search(any(LdapQuery.class), isA(EmprunteursAttributesMapper.class));
    }

    @Test
    void shouldThrowWhenNoEtabFoundDuringFindAllEmprunteurs() {
        when(ldapTemplate.search(any(LdapQuery.class), isA(ParentsAttributesMapper.class))).thenReturn(List.of());
        when(ldapTemplate.search(any(LdapQuery.class), isA(EtabAttributesMapper.class))).thenReturn(List.of());

        assertThatThrownBy(() -> dao.findAllEmprunteurs("uaiInconnu"))
                .isInstanceOf(NoSuchElementException.class);
    }
}
