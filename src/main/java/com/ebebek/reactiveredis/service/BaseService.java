package com.ebebek.reactiveredis.service;

import com.ebebek.reactiveredis.model.base.BaseModel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

public abstract class BaseService {

    @Autowired
    protected RedisTemplate redisTemplate;

    protected Jackson2JsonRedisSerializer serializerSetup() {
        Jackson2JsonRedisSerializer<BaseModel> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(BaseModel.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }
}
