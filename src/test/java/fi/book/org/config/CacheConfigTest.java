package fi.book.org.config;

import com.github.benmanes.caffeine.cache.Caffeine;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void shouldCreateCaffeineConfig() {
        CacheConfig config = new CacheConfig();
        Caffeine caffeine = config.caffeineConfig();

        assertThat(caffeine).isNotNull();
    }

    @Test
    void shouldCreateCacheManager() {
        CacheConfig config = new CacheConfig();
        Caffeine caffeine = config.caffeineConfig();
        CacheManager cacheManager = config.cacheManager(caffeine);

        assertThat(cacheManager).isNotNull();
    }
}
