package lk.tech.tgcontrollerbot.configuration;

import io.netty.channel.ChannelOption;
import io.netty.resolver.DefaultAddressResolverGroup;
import lk.tech.tgcontrollerbot.requests.TelegramHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class TelegramWebConfiguration {

    @Bean("webClientTelegram")
    public WebClient webClientTelegram(
            @Value("${url.telegram}") String url,
            @Value("${telegram.bot.token}") String botToken,
            HttpClient httpClient,
            ExchangeFilterFunction logFilter
    ) {
        return WebClient.builder()
                .baseUrl(url + botToken)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logFilter)
                .codecs(cfg -> {
                    cfg.defaultCodecs().maxInMemorySize(16 * 1024 * 1024); // Для фото/доков
                })
                .build();
    }

    @Bean
    public HttpClient telegramHttpClient(ConnectionProvider provider) {
        return HttpClient.create(provider)
                .resolver(DefaultAddressResolverGroup.INSTANCE)   // 🔥 быстрый DNS
                .keepAlive(true)
                .compress(true)
                .responseTimeout(Duration.ofSeconds(30))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 8000)
                .option(ChannelOption.TCP_NODELAY, true)           // 🔥 уменьшает задержки
                .option(ChannelOption.SO_KEEPALIVE, true);
    }

    @Bean
    public ConnectionProvider telegramConnectionProvider() {
        return ConnectionProvider.builder("telegram-pool")
                .maxConnections(200)
                .pendingAcquireMaxCount(-1)
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .maxIdleTime(Duration.ofSeconds(45))
                .lifo()
                .build();
    }
    @Bean
    public TelegramHttpClient telegramHttpClientWeb(@Qualifier("webClientTelegram") WebClient webClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(TelegramHttpClient.class);
    }
}
