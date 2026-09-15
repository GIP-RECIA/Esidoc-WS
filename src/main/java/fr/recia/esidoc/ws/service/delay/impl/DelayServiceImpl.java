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
import fr.recia.esidoc.ws.service.delay.IDelayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@Service
public class DelayServiceImpl implements IDelayService {

    private final DelayProperties delayProperties;
    private final RedisProperties redisProperties;
    private final RedisTemplate<String, String> redisTemplate;


    @Autowired
    public  DelayServiceImpl(
            DelayProperties delayProperties,
            RedisProperties redisProperties,
            @Qualifier("customRedisTemplate") RedisTemplate<String, String> redisTemplate
    ){
                this.delayProperties = delayProperties;
                this.redisProperties = redisProperties;
                this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean canSendRequestToEsidocApi(String uai) {
        if(!delayProperties.isUseDelay()){
            return true;
        }
       String value = redisTemplate.opsForValue().get(keyFromUai(uai));
        return Objects.isNull(value);
    }

    @Override
    public void applyDelayToUai(String uai) {
        if(!delayProperties.isUseDelay()){
            return;
        }
        redisTemplate.opsForValue().set(keyFromUai(uai), "delay", Duration.ofMinutes(delayProperties.getDurationInMinutes()));
    }

    private String keyFromUai(String uai){
        return String.format("%s:%s", redisProperties.getMappingPrefix(), uai);
    }

}
