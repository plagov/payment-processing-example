package io.plagov.payment_processing.exceptions;

import org.springframework.http.HttpStatusCode;

import java.util.Map;

public record ValidationErrorResponse(
        HttpStatusCode statusCode,
        Map<String, String> fieldErrors
) {
}
