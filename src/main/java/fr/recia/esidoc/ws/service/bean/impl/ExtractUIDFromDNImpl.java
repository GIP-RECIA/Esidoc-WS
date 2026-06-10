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
package fr.recia.esidoc.ws.service.bean.impl; /**
 * Copyright (C) 2020 GIP-RECIA https://www.recia.fr/
 * @Author (C) 2020 Julien Gribonvald <julien.gribonvald@recia.fr>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *                 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.validation.constraints.NotBlank;

import fr.recia.esidoc.ws.service.bean.IExtractUIDFromDN;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtractUIDFromDNImpl implements IExtractUIDFromDN {

    private Pattern uidDNPattern;

    public ExtractUIDFromDNImpl(final @NotBlank String uidDNPattern) {
        this.uidDNPattern = Pattern.compile(uidDNPattern);
    }

    @Override
    public String getUidFromDN(String dn) {
        Matcher matcher = uidDNPattern.matcher(dn);
        if (matcher.find()) {
            log.debug(String.format("Matcher found opaqueIdPattern [%s] in [%s]", uidDNPattern.pattern(), dn));
            return matcher.group(1);
        }
        throw new IllegalArgumentException("The Pattern to get the uid value isn't matching on " + dn + ", check your configuration !");
    }

    @Override
    public List<String> getUidsFromDNs(final List<String> dns) {
        List<String> uids = new ArrayList<String>();
        for (String dn: dns) {
            uids.add(this.getUidFromDN(dn));
        }
        log.debug(String.format("Returning uids retrieved [%s] from [%s]", uids, dns));
        return uids;
    }
}
