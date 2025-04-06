package io.plagov.payment_processing.interceptor;

import io.plagov.payment_processing.models.IpApiResponse;
import io.plagov.payment_processing.service.ConsumerCountryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CountryLoggerInterceptor implements HandlerInterceptor {

    private final ConsumerCountryService consumerCountryService;

    private static final Logger LOGGER = LogManager.getLogger(CountryLoggerInterceptor.class.getName());

    // to avoid extra calls to a 3rd party service for the same IP address, I introduced a local cache
    // the cache is cleaned up hourly by a scheduled `cleanupCache()` method below in this class
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public CountryLoggerInterceptor(ConsumerCountryService consumerCountryService) {
        this.consumerCountryService = consumerCountryService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var ipAddress = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR"))
                .orElse(request.getRemoteAddr());

        consumerCountryService.resolveCountryOfIpAddress(ipAddress)
                .doOnSuccess(countryResponse -> logConsumerCountry(ipAddress, countryResponse))
                .doOnError(error -> LOGGER.error("Failed to resolve consumer country", error))
                .subscribe();

        return true;
    }

    private void logConsumerCountry(String ipAddress, IpApiResponse response) {
        var country = cache.computeIfAbsent(ipAddress, k -> response.country());
        LOGGER.info("Consumer country: {}", country);
    }

    @Scheduled(cron = "@hourly")
    private void cleanupCache() {
        cache.clear();
    }
}
