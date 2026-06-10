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

import fr.recia.esidoc.ws.model.Emprunteurs;
import fr.recia.esidoc.ws.model.Parent;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.util.StringUtils;

import javax.naming.NamingException;
import java.awt.event.ItemListener;
import java.util.AbstractMap;
import java.util.Map.Entry;

public class ParentsAttributesMapper  implements ContextMapper<Entry<String, Parent>> {


//    private final LdapAttributes ldapAttributes;

    @Override
    public Entry<String, Parent> mapFromContext(Object ctx) throws NamingException {

        Parent parent = new Parent();

        DirContextAdapter context = (DirContextAdapter) ctx;

        String key = context.getStringAttribute(LdapAttributes.UID);

        //addresse
        String addresseToUse = "";
        final String adresse = (context.getStringAttribute(LdapAttributes.ENT_PERSON_ADRESSE));
        if (StringUtils.hasText(adresse)) {
            final String[] addr = adresse.split("\\$");
            addresseToUse += (addr[0]);
            if (addr.length > 1)
                addresseToUse += (addr[1]);
            if (addr.length > 2)
                addresseToUse += (addr[2]);
            if (addr.length > 3)
                addresseToUse += (addr[3]);
        }

        parent.setAdresse(addresseToUse);

        // code postal
        parent.setCodePostal(context.getStringAttribute(LdapAttributes.ENT_PERSON_CODE_POSTAL));

        // ville
        parent.setVille(context.getStringAttribute(LdapAttributes.ENT_PERSON_VILLE));

        //tel
        parent.setTel(context.getStringAttribute(LdapAttributes.ENT_PERSON_MOBILE_SMS));

        return new AbstractMap.SimpleEntry<>(key, parent);
    }

}
