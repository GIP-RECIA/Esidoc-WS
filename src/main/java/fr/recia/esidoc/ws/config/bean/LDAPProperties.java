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
package fr.recia.esidoc.ws.config.bean;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Slf4j
@ConfigurationProperties(prefix = "app.ldap")
public class LDAPProperties {

    String url;

    String base;

    String userDn;

    String userOpaqueIdPattern;

    String password;

    boolean pooled;

    boolean anonymousReadOnly;

    int countLimit;

    int timeout;

    Filters filters;

    String peopleRootDn;

    String  userUidExtractorPattern;

    String autorizedResponsablePattern;

    @PostConstruct
    void init(){
        log.info(this.toString());
    }

    @Override
    public String toString() {
        return "LDAPProperties{" +
                "url='" + url + '\'' +
                ", base='" + base + '\'' +
                ", userDn='" + userDn + '\'' +
                ", userOpaqueIdPattern='" + userOpaqueIdPattern + '\'' +
                ", password='" + password + '\'' +
                ", pooled=" + pooled +
                ", anonymousReadOnly=" + anonymousReadOnly +
                ", countLimit=" + countLimit +
                ", timeout=" + timeout +
                ", filters=" + filters +
                ", peopleRootDn='" + peopleRootDn + '\'' +
                ", structureRootDn='" + structureRootDn + '\'' +
                ", userUidExtractorPattern='" + userUidExtractorPattern + '\'' +
                ", autorizedResponsablePattern='" + autorizedResponsablePattern + '\'' +
                '}';
    }
}
