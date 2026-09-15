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
package fr.recia.esidoc.ws.service.delay.impl;

import fr.recia.esidoc.ws.config.bean.DelayProperties;
import fr.recia.esidoc.ws.config.bean.RedisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DelayServiceImplTest {

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    RedisProperties redisProperties;
    DelayProperties delayProperties;
    DelayServiceImpl service;

    @BeforeEach
    void setUp() {
        delayProperties = new DelayProperties();
        delayProperties.setUseDelay(true);
        delayProperties.setDurationInMinutes(30);

        redisProperties = new RedisProperties();
        redisProperties.setMappingPrefix("esidoc");
        service = new DelayServiceImpl(delayProperties, redisProperties, redisTemplate);
    }

    @Test
    void shouldAllowRequestWhenDelayIsDisabled() {
        final DelayProperties disabledDelayProperties = new DelayProperties();
        disabledDelayProperties.setUseDelay(false);
        final DelayServiceImpl disabledService = new DelayServiceImpl(disabledDelayProperties, redisProperties, redisTemplate);

        final boolean result = disabledService.canSendRequestToEsidocApi("uai1");

        assertThat(result).isTrue();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldAllowRequestWhenNoDelayKeyIsSet() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("esidoc:uai1")).thenReturn(null);

        final boolean result = service.canSendRequestToEsidocApi("uai1");

        assertThat(result).isTrue();
    }

    @Test
    void shouldDenyRequestWhenDelayKeyIsSet() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("esidoc:uai1")).thenReturn("delay");

        final boolean result = service.canSendRequestToEsidocApi("uai1");

        assertThat(result).isFalse();
    }

    @Test
    void shouldNotWriteToRedisWhenDelayIsDisabled() {
        final DelayProperties disabledDelayProperties = new DelayProperties();
        disabledDelayProperties.setUseDelay(false);
        final DelayServiceImpl disabledService = new DelayServiceImpl(disabledDelayProperties, redisProperties, redisTemplate);
        disabledService.applyDelayToUai("uai1");

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldSetDelayKeyWithConfiguredDuration() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.applyDelayToUai("uai1");

        verify(valueOperations).set("esidoc:uai1", "delay", Duration.ofMinutes(30));
    }
}