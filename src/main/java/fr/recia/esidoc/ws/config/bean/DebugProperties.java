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
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@Slf4j
@ConfigurationProperties(prefix = "app.debug")
public class DebugProperties {

    public enum DebugMode{
        NONE,
        PROFILES,
    }
    @Setter
    @Getter(value = AccessLevel.NONE)
    String debugMode;
    String debugUai;
    List<String> profiles;

    @Setter(value = AccessLevel.NONE)
    @Getter(value = AccessLevel.NONE)
    DebugMode debugModeEnumValue;

    public DebugMode getDebugMode() {
        return debugModeEnumValue;
    }

    @PostConstruct
    void init(){
        try {
            debugModeEnumValue = DebugMode.valueOf(debugMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            debugModeEnumValue = DebugProperties.DebugMode.NONE;
        }
        log.info(this.toString());
    }

    @Override
    public String toString() {
        return "DebugProperties{" +
                "debugMode='" + debugMode + '\'' +
                ", debugUai='" + debugUai + '\'' +
                ", profiles=" + profiles +
                ", debugModeEnumValue=" + debugModeEnumValue +
                '}';
    }
}
