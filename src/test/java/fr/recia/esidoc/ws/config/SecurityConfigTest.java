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
package fr.recia.esidoc.ws.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SecurityConfigTest {
    @ParameterizedTest
    @MethodSource("ipsProvider")
    void shouldBuildAccessExpression(List<String> authorizedIps, String expected){
        String result = SecurityConfig.buildAccessExpression(authorizedIps);
        assertThat(result).isEqualTo(expected);
    }


    static Stream<Arguments> ipsProvider(){
        return Stream.of(
                Arguments.of(
                        List.of(),
                        "isAuthenticated() and (hasIpAddress('127.0.0.1') or hasIpAddress('::1'))"),
                Arguments.of(
                        List.of("10.0.0.5"),
                        "isAuthenticated() and (hasIpAddress('127.0.0.1') or hasIpAddress('::1') or hasIpAddress('10.0.0.5'))"),
                Arguments.of(
                        List.of("10.0.0.5", "10.0.0.6"),
                        "isAuthenticated() and (hasIpAddress('127.0.0.1') or hasIpAddress('::1') or hasIpAddress('10.0.0.5') or hasIpAddress('10.0.0.6'))")
        );
    }
}
