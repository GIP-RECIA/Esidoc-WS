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
/**
 *
 */
package fr.recia.esidoc.ws.dao.impl;


import fr.recia.esidoc.ws.config.bean.MappingProperties;
import fr.recia.esidoc.ws.model.Emprunteurs;
import fr.recia.esidoc.ws.model.Parent;
import fr.recia.esidoc.ws.model.RapportExport;
import fr.recia.esidoc.ws.service.bean.IExtractOpaqueId;
import fr.recia.esidoc.ws.service.bean.IExtractUIDFromDN;
import fr.recia.esidoc.ws.service.util.MappingStatusUtils;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.naming.NamingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@Slf4j
public class EmprunteursAttributesMapper implements ContextMapper<Emprunteurs> {

//	@NotNull
//	private IIDMapper userMapper;
//
//	@NotNull

	private IExtractOpaqueId extractOpaqueId;

	private MappingStatusUtils mappingStatusUtils;

	private MappingProperties mappingProperties;

	private Map<String, Parent> parentMap;

	@NotNull
	private IExtractUIDFromDN extractUIDFromDN;

	@NotNull
	private Pattern responsablePattern;

	private RapportExport rapportExport;

	@Override
	public Emprunteurs mapFromContext(Object ctx) throws NamingException {
		Emprunteurs emprunteurs = new Emprunteurs();

		String eleveStatus = mappingProperties.getEleveBcdiStatut();

		DirContextAdapter context = (DirContextAdapter) ctx;


		//todo ne pas utiliser
		final String uid = context.getStringAttribute(LdapAttributes.UID);


		String nom = context.getStringAttribute(LdapAttributes.SN);
		String prenom = context.getStringAttribute(LdapAttributes.GIVEN_NAME);

		String  nomPrenom = String.format("%s %s", nom , prenom);

		emprunteurs.setNomPrenom(nomPrenom);
		Assert.hasText(emprunteurs.getNomPrenom(), "Le nom prenom ne peut être vide !");


		String statusToUse = mappingStatusUtils.getMainStatusMM(List.of(context.getStringAttributes(LdapAttributes.ENT_PERSON_PROFILS)));

		if(Objects.isNull(statusToUse)){
			return null;
		}

		// STATUS
		emprunteurs.setStatut(statusToUse);


		if(eleveStatus.equals(statusToUse)){
			//si eleve, il faut renseigner la classe
			String[] classes= context.getStringAttributes(LdapAttributes.ENT_ELEVE_CLASSES);

			String[] array = classes[0].split("\\$");
			emprunteurs.setClasse(array[array.length-1]);
			// si eleve, il faut obtenir via parent
			final String[] parents = context.getStringAttributes(LdapAttributes.ENT_ELEVE_PERS_REL_ELEVE);
			List<String> resp = new ArrayList<>();
			if (parents != null) {
				for (String parent : parents) {
					if (responsablePattern.matcher(parent).matches()) {
						final String parentUid = extractUIDFromDN.getUidFromDN(parent);
						if(parentMap.containsKey(parentUid)){

							Parent parentEntity = parentMap.get(parentUid);
							emprunteurs.setAdresse(parentEntity.getAdresse());

							// code postal
							emprunteurs.setCodePostal(parentEntity.getCodePostal());

							// ville
							emprunteurs.setVille(parentEntity.getVille());


							//todo tel
							emprunteurs.setTel(parentEntity.getTel());


							break;
						}
					}
					log.error("TUT not having adresse");
				}
			}else{
				log.error("ELV without TUT");
			}



		}else{
			// sinon on récupere direct

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
			emprunteurs.setAdresse(addresseToUse);
			emprunteurs.setCodePostal(context.getStringAttribute(LdapAttributes.ENT_PERSON_CODE_POSTAL));
			emprunteurs.setVille(context.getStringAttribute(LdapAttributes.ENT_PERSON_VILLE));
			emprunteurs.setTel(context.getStringAttribute(LdapAttributes.ENT_PERSON_MOBILE_SMS));
		}



		emprunteurs.setMail(context.getStringAttribute(LdapAttributes.MAIL));

		emprunteurs.setIdentiteEnt(extractOpaqueId.getOpaqueId(context));

		emprunteurs.setDateNaissance(context.getStringAttribute(LdapAttributes.ENT_PERSON_DATE_NAISSANCE));


		String date = context.getStringAttribute(LdapAttributes.ENT_PERSON_DATE_NAISSANCE);
		emprunteurs.setDateNaissance(date);

		char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
				.toCharArray();

		RandomStringGenerator generator = new RandomStringGenerator.Builder()
				.selectFrom(chars)
				.usingRandom(index -> ThreadLocalRandom.current().nextInt(chars.length))
				.build();

		emprunteurs.setMotDePasse(generator.generate(7));


		//obligatoire = date naissance, nom prenom compte, mot de passe et identite ent

        try {
            Assert.hasText(emprunteurs.getDateNaissance(), "DATE_NAISSANCE_M (dateNaissance) should not be null or blank");
            Assert.hasText(emprunteurs.getNomPrenom(), "EMPRUNTEUR_M (nomPrenom) should not be null or blank");
            Assert.hasText(emprunteurs.getCompte(), "COMPTE_M (compte) should not be null or blank");
            Assert.hasText(emprunteurs.getMotDePasse(), "MOT_DE_PASSE_M (motDePass) should not be null or blank");
            Assert.hasText(emprunteurs.getIdentiteEnt(), "IDENTITE_ENT_M (identiteEnt (External ID)) should not be null or blank");
        } catch (Exception e) {

			log.info("status is {}", statusToUse);
			if(mappingProperties.getAutreBcdiStatut().equals(statusToUse)){
				rapportExport.getWarnings().add(String.format("Skipped user with uid %s since STATUT_M_M is Autre and this population is not required", uid));
				return null;
			}

            throw new RuntimeException(e);
        }


        // classe non obligatoire mais devrait etre rempli si eleve
		if(eleveStatus.equals(emprunteurs.getStatut())){
			if(isNullOrEmpty(emprunteurs.getClasse())){
				rapportExport.getWarnings().add(String.format("No CLASSE_M  for person with uid %s and statut %s", uid, statusToUse));
			}
		}

		//adresse cide postal ville tel mel

		if(isNullOrEmpty(emprunteurs.getAdresse())){
			rapportExport.getWarnings().add(String.format("No ADRESSE_M for person with uid %s and statut %s", uid, statusToUse));
			emprunteurs.setAdresse("");
		}

		if(isNullOrEmpty(emprunteurs.getCodePostal())){
			rapportExport.getWarnings().add(String.format("No CODE_POSTAL_M for person with uid %s and statut %s", uid, statusToUse));
			emprunteurs.setCodePostal("");
		}

		if(isNullOrEmpty(emprunteurs.getVille())){
			rapportExport.getWarnings().add(String.format("No VILLE_M for person with uid %s and statut %s", uid, statusToUse));
			emprunteurs.setVille("");
		}

		if(isNullOrEmpty(emprunteurs.getTel())){
			rapportExport.getWarnings().add(String.format("No TEL_M for person with uid %s and statut %s", uid, statusToUse));
			emprunteurs.setTel("");
		}

		if(isNullOrEmpty(emprunteurs.getMail())){
			rapportExport.getWarnings().add(String.format("No MEL_M for person with uid %s and statut %s", uid, statusToUse));
			emprunteurs.setMail("");
		}

		return emprunteurs;
	}


	boolean isNullOrEmpty(@Nullable String value){
		return Objects.isNull(value) || value.isBlank();
	}

}
