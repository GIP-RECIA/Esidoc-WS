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

import fr.recia.esidoc.ws.config.bean.LDAPProperties;
import fr.recia.esidoc.ws.config.bean.MappingProperties;
import fr.recia.esidoc.ws.dao.ILdapDao;
import fr.recia.esidoc.ws.exception.EtablissementMissingException;
import fr.recia.esidoc.ws.model.Emprunteurs;
import fr.recia.esidoc.ws.model.Parent;
import fr.recia.esidoc.ws.service.bean.IExtractOpaqueId;
import fr.recia.esidoc.ws.service.bean.IExtractUIDFromDN;
import fr.recia.esidoc.ws.service.bean.impl.ExtractEntEleveGroup;
import fr.recia.esidoc.ws.service.util.MappingStatusUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Data
@RequiredArgsConstructor
@Slf4j
public class LdapDaoImpl implements ILdapDao {

    private final LdapTemplate ldapTemplate;
    private final LDAPProperties ldapProperties;
    private final IExtractOpaqueId extractOpaqueId;
    private final MappingStatusUtils mappingStatusUtils;
    private final MappingProperties mappingProperties;
    private final IExtractUIDFromDN extractUIDFromDN;

    private Map<String, Parent> findAllParents(String uai) {
        Map<String, Parent> parentMap;
        final Filter filter = new HardcodedFilter(String.format(ldapProperties.getFilters().getParents(), uai));
        ContextMapper<Map.Entry<String, Parent>> mapper = new ParentsAttributesMapper();
        LdapQuery query = LdapQueryBuilder.query()
                .attributes(LdapAttributes.PARENT_ATTRS.toArray(new String[LdapAttributes.PARENT_ATTRS.size()]))
                .base(ldapProperties.getPeopleRootDn()).filter(filter);

        parentMap =
                ldapTemplate.search(query, mapper)
                        .stream()
                        .collect(Collectors.toMap(
                                Entry::getKey,
                                Entry::getValue
                        ));

        return parentMap;
    }

    @Override
    public String getSirenForUai(String uai) {
        final Filter filter = new HardcodedFilter(String.format(ldapProperties.getFilters().getValidUai(), uai));
        LdapQuery query = LdapQueryBuilder.query().countLimit(1)
                .attributes(LdapAttributes.ETAB_ATTRS.toArray(new String[LdapAttributes.ETAB_ATTRS.size()]))
                .base(ldapProperties.getStructureRootDn()).filter(filter);
        ContextMapper<String> mapper = new EtabAttributesMapper();
        List<String> etabs = ldapTemplate.search(query, mapper);
        if(!etabs.isEmpty()){
            return etabs.getFirst();
        }
        throw new EtablissementMissingException("UAI :"+ uai );
    }

    @Override
    public List<Emprunteurs> findAllEmprunteurs(String uai) {
        Map<String, Parent> parentMap = findAllParents(uai);
        final Filter filter = new HardcodedFilter(String.format(ldapProperties.getFilters().getEmprunteurs(), uai));
        String siren = getSirenForUai(uai);
        ContextMapper<Emprunteurs> mapper = new EmprunteursAttributesMapper(
                extractOpaqueId,
                mappingStatusUtils,
                mappingProperties,
                parentMap,
                extractUIDFromDN,
                Pattern.compile(ldapProperties.getAutorizedResponsablePattern()),
                new ExtractEntEleveGroup(Pattern.compile(String.format(ldapProperties.getEleveGroupePattern(), siren)))
        );
        LdapQuery query = LdapQueryBuilder.query()
                .attributes(LdapAttributes.PERSON_ATTRS.toArray(new String[LdapAttributes.PERSON_ATTRS.size()]))
                .base(ldapProperties.getPeopleRootDn()).filter(filter);

        List<Emprunteurs> empruntersListWithNull = ldapTemplate.search(query, mapper);
        return empruntersListWithNull.stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
