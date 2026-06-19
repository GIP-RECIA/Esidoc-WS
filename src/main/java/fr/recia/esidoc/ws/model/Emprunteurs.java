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
package fr.recia.esidoc.ws.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "EMPRUNTEURS")
public class Emprunteurs {

    // TODO dois etre EMPRUNTEUR_M en balise dans le XML
    @JacksonXmlProperty(localName = "EMPRUNTEUR_M")
    String nomPrenom;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JacksonXmlProperty(localName = "DIVEMPR_M")
    String groupe;

    @JacksonXmlProperty(localName = "ADRESSE_M")
    String adresse ="";

    @JacksonXmlProperty(localName = "CODE_POSTAL_M")
    String codePostal ="";

    @JacksonXmlProperty(localName = "VILLE_M")
    String ville ="";

    @JacksonXmlProperty(localName = "TEL_M")
    String tel ="";

    @JacksonXmlProperty(localName = "MEL_M")
    String mail ="";

    @JacksonXmlProperty(localName = "STATUT_M_M")
    String statut;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JacksonXmlProperty(localName = "CLASSE_M")
    String classe;

    @JacksonXmlProperty(localName = "COMPTE_M")
    String compte = "OUI";

    @JacksonXmlProperty(localName = "MOT_DE_PASSE_M")
    String motDePasse;

    @JacksonXmlProperty(localName = "IDENTITE_ENT_M")
    String identiteEnt;

//    @JacksonXmlProperty(localName = "TYPE_EMPRUNTEUR_M")
//    String typeEmptrunteur = "Usager";




}
