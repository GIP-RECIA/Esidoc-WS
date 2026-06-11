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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LdapAttributes {
    public static final String MAIL ="mail";
    public static final String UID = "uid";
    public static final String SN = "sn";
    public static final String GIVEN_NAME = "givenName";
    public static final String ENT_PERSON_ADRESSE = "ENTPersonAdresse";
    public static final String ENT_PERSON_CODE_POSTAL = "ENTPersonCodePostal";
    public static final String ENT_PERSON_VILLE = "ENTPersonVille";
    public static final String ENT_PERSON_DATE_NAISSANCE = "ENTPersonDateNaissance";
    public static final String ENT_ELEVE_PERS_REL_ELEVE = "ENTElevePersRelEleve";
    public static final String ESCO_PERSON_EXTERNAL_IDS = "ESCOPersonExternalIds";
    public static final String ENT_PERSON_PROFILS= "ENTPersonProfils";
    public static final String ENT_PERSON_MOBILE_SMS = "ENTPersonMobileSMS";
    public static final String ENT_ELEVE_CLASSES = "ENTEleveClasses";

    public static final Set<String> PERSON_ATTRS =
            Stream.of(UID, SN, GIVEN_NAME, ENT_PERSON_CODE_POSTAL, ENT_PERSON_VILLE, ENT_PERSON_ADRESSE, ENT_PERSON_DATE_NAISSANCE, ENT_ELEVE_PERS_REL_ELEVE, ESCO_PERSON_EXTERNAL_IDS, ENT_PERSON_PROFILS, MAIL, ENT_PERSON_MOBILE_SMS, ENT_ELEVE_CLASSES)
                    .collect(Collectors.toSet());

    public static Set<String> PARENT_ATTRS =
            Stream.of(ENT_PERSON_ADRESSE, ENT_PERSON_CODE_POSTAL, ENT_PERSON_VILLE, ENT_PERSON_MOBILE_SMS, UID).collect(Collectors.toSet());
}
