package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 限时特卖模块的 Redis 相关配置。
 * 当前只注册抢购入口使用的 Lua 脚本。
 */
@Configuration
public class FlashSaleRedisConfiguration {

    /**
     * 注册抢购脚本。
     * 该脚本会在 Redis 内部原子完成活动状态判断、库存扣减和用户限购次数累加。
     */
    @Bean
    public DefaultRedisScript<Long> flashSalePurchaseScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/flashsale_purchase.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}
