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
import fr.recia.esidoc.ws.model.Emprunteurs;
import fr.recia.esidoc.ws.model.Parent;
import fr.recia.esidoc.ws.model.RapportExport;
import fr.recia.esidoc.ws.service.bean.IExtractOpaqueId;
import fr.recia.esidoc.ws.service.bean.IExtractUIDFromDN;
import fr.recia.esidoc.ws.service.util.MappingStatusUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class LdapDaoImpl implements ILdapDao {

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private LDAPProperties ldapProperties;

    @Autowired
    private IExtractOpaqueId extractOpaqueId;

    @Autowired
    private MappingStatusUtils mappingStatusUtils;

    @Autowired
    private MappingProperties mappingProperties;

    private Map<String,Parent> parentMap = new HashMap<>();

    @Autowired
    private IExtractUIDFromDN extractUIDFromDN;

    @Autowired
    RapportExport rapportExport;


     private void findAllparents(String uai) {
        final Filter filter= new HardcodedFilter(String.format(ldapProperties.getFilters().getParents(), uai));
        log.debug("LDAP filter applied : " + filter);
        ContextMapper<Map.Entry<String, Parent>> mapper = new ParentsAttributesMapper();
        LdapQuery query = LdapQueryBuilder.query()
                .attributes(LdapAttributes.PERSON_ATTRS.toArray(new String[LdapAttributes.PERSON_ATTRS.size()]))
                .base(ldapProperties.getPeopleRootDn()).filter(filter);

        parentMap =
                ldapTemplate.search(query, mapper)
                        .stream()
                        .collect(Collectors.toMap(
                                Entry::getKey,
                                Entry::getValue
                        ));

//        return map;
    }

    @Override
    public List<Emprunteurs> findAllEmprunteurs(String uai) {
        findAllparents(uai);
        final Filter filter= new HardcodedFilter(String.format(ldapProperties.getFilters().getEmprunteurs(), uai));
        log.debug("LDAP filter applied : " + filter);

        ContextMapper<Emprunteurs> mapper = new EmprunteursAttributesMapper(
                extractOpaqueId,
                mappingStatusUtils,
                mappingProperties,
                parentMap,
                extractUIDFromDN,
                Pattern.compile(ldapProperties.getAutorizedResponsablePattern()),
                rapportExport
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
