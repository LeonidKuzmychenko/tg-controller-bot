package lk.tech.learntgbot.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lk.tech.learntgbot.model.UserData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<Long, UserData> userDataCache() {
        return Caffeine.newBuilder()
                .recordStats()
                .build();
    }
}
