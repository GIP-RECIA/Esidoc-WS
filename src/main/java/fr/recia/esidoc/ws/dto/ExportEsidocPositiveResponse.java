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

import lombok.Data;

import java.util.List;

@Data
public class ExportEsidocPositiveResponse {

    public ExportEsidocPositiveResponse(List<String> successfulUais){
        this.successfulUais = successfulUais;
        success = "success";
    }

    List<String> successfulUais;

    // "success" "partial" ou "fail"
    String success;

}
