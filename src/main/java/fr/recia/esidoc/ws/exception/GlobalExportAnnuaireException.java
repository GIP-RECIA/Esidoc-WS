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
package fr.recia.esidoc.ws.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GlobalExportAnnuaireException extends RuntimeException {
    public GlobalExportAnnuaireException(String message, List<String> exceptionUais, boolean partial, String responseMessage, List<String> alreadyExportedUais) {
        super(message);
        this.exceptionUais = new ArrayList<>(exceptionUais);
        this.partial = partial;
        this.responseMessage = responseMessage;
        this.alreadyExportedUais = alreadyExportedUais;
    }

    private final boolean partial;
    private final String responseMessage;
    private final List<String> exceptionUais;
    private final List<String> alreadyExportedUais;
}
