package io.plagov.payment_processing.models;

public record IpApiResponse(
        String query,
        String status,
        String country,
        String countryCode
) { }
