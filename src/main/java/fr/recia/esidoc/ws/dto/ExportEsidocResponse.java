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
package fr.recia.esidoc.ws.dto;

import fr.recia.esidoc.ws.exception.AlreadyExportedException;
import fr.recia.esidoc.ws.exception.GlobalExportAnnuaireException;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExportEsidocResponse extends ExportEsidocPositiveResponse {

    List<String> exceptionUais;
    List<String> alreadyExportedUais;


    public ExportEsidocResponse(String responseFromEsidocApi){
        super(responseFromEsidocApi);
    }

    public ExportEsidocResponse(GlobalExportAnnuaireException globalExportAnnuaireException){
        super(globalExportAnnuaireException.getResponseMessage());
        this.exceptionUais = new ArrayList<>(globalExportAnnuaireException.getExceptionUais());
        this.success = globalExportAnnuaireException.isPartial() ? "partial" : "fail";
        this.alreadyExportedUais = globalExportAnnuaireException.getAlreadyExportedUais();
    }


    public ExportEsidocResponse(ExportEsidocPositiveResponse exportEsidocPositiveResponse){
        super(exportEsidocPositiveResponse.responseFromEsidocApi);
    }

    public ExportEsidocResponse(AlreadyExportedException alreadyExportedException){
        super("");
        this.success = "failed";
        this.alreadyExportedUais = alreadyExportedException.getAlreadyExportedUais();
        this.exceptionUais = new ArrayList<>();
    }



}
