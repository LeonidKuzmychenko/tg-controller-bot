package lk.tech.tgcontrollerbot.configuration;


import com.fasterxml.jackson.databind.ObjectMapper;
import lk.tech.tgcontrollerbot.requests.HttpRequests;
import lk.tech.tgcontrollerbot.requests.TelegramHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class ServerConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean("webClient")
    public WebClient webClient(@Value("${url.socket}") String url,
                               ExchangeFilterFunction logFilter) {
        return WebClient.builder()
                .baseUrl(url)
                .filter(logFilter)
                .build();
    }



    @Bean
    public HttpRequests jsonPlaceholderClient(@Qualifier("webClient") WebClient webClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(HttpRequests.class);
    }



    @Bean
    public ExchangeFilterFunction logFilter() {
        return (request, next) -> {
            log.info("REQUEST {} {}", request.method(), request.url());
            return next.exchange(request)
                    .doOnNext(response ->
                            log.info("RESPONSE {} {}", response.statusCode(), request.url())
                    );
        };
    }
}
