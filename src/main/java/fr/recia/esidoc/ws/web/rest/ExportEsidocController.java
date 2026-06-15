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
package fr.recia.esidoc.ws.web.rest;

import fr.recia.esidoc.ws.dto.ExportEsidocResponse;
import fr.recia.esidoc.ws.service.IExportEsidocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api")
public class ExportEsidocController {


    @Autowired
    IExportEsidocService exportEsidocService;

    @GetMapping(value = "/export/{uai}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExportEsidocResponse> healthCheck(@PathVariable String uai  ) {
        ExportEsidocResponse responseEsidoc = new ExportEsidocResponse(exportEsidocService.exportAnnuaireForUai(uai));
        return ResponseEntity.ok().body(responseEsidoc);
    }

}
