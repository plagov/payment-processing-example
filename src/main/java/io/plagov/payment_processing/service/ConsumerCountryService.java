package io.plagov.payment_processing.service;

import io.plagov.payment_processing.models.IpApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ConsumerCountryService {

    private final WebClient webClient;

    public ConsumerCountryService(
            WebClient.Builder webClientBuilder,
            @Value("${ip-api.baseUrl}") String ipApiBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(ipApiBaseUrl).build();
    }

    public Mono<IpApiResponse> resolveCountryOfIpAddress(String ipAddress) {
        return webClient.get().uri("/json/{ipAddress}", ipAddress).retrieve().bodyToMono(IpApiResponse.class);
    }
}
