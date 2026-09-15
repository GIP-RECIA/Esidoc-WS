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
package fr.recia.esidoc.ws.service.mapping.impl;

import fr.recia.esidoc.ws.dao.ILdapDao;
import fr.recia.esidoc.ws.model.Emprunteurs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MappingServiceImplTest {

    @Mock
    ILdapDao ldapDao;

    MappingServiceImpl mappingService;

    @BeforeEach
    void setUp() throws Exception {
        File importXSD = new ClassPathResource("xsd/Import.xsd").getFile();
        mappingService = new MappingServiceImpl(ldapDao, importXSD);
    }

    @Test
    void shouldDelegateToLdapDaoForEmprunteurs() {
        final Emprunteurs emprunteur = validEmprunteur();
        when(ldapDao.findAllEmprunteurs("uai1")).thenReturn(List.of(emprunteur));

        final List<Emprunteurs> result = mappingService.getEmprunteurs("uai1");

        assertThat(result).containsExactly(emprunteur);
        verify(ldapDao).findAllEmprunteurs("uai1");
    }

    @Test
    void shouldReturnValidatedXmlForValidEmprunteurs() throws Exception {
        final String xml = mappingService.getValidatedXml(List.of(validEmprunteur()));

        assertThat(xml).contains("FICHES_XML", "Jean Dupont");
    }

    @Test
    void shouldThrowWhenRequiredFieldIsMissing() {
        final Emprunteurs invalid = validEmprunteur();
        invalid.setNomPrenom("");

        assertThatThrownBy(() -> mappingService.getValidatedXml(List.of(invalid)))
                .isInstanceOf(SAXException.class);
    }

    private Emprunteurs validEmprunteur() {
        return new Emprunteurs(
                "Jean Dupont",
                null,
                "1 rue de la Paix",
                "45000",
                "Orléans",
                "0600000000",
                "jean.dupont@example.com",
                "Elève",
                null,
                "OUI",
                "motdepasse",
                "identite-ent-1"
        );
    }
}
