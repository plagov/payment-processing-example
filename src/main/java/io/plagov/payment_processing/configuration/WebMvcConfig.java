package io.plagov.payment_processing.configuration;

import io.plagov.payment_processing.interceptor.CountryLoggerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CountryLoggerInterceptor countryLoggerInterceptor;

    public WebMvcConfig(CountryLoggerInterceptor countryLoggerInterceptor) {
        this.countryLoggerInterceptor = countryLoggerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(countryLoggerInterceptor);
    }
}
