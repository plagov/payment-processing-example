package io.plagov.payment_processing.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.plagov.payment_processing.configuration.ContainersConfig;
import io.plagov.payment_processing.models.PaymentRequest;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Testcontainers
@SpringBootTest
@Import(ContainersConfig.class)
@AutoConfigureMockMvc
class CreatePaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateNewPesaPayment() throws Exception {
        var paymentRequest = new PaymentRequest(
                Money.parse("EUR 150.25"),
                "EE382200221020145685",
                "LT121000011101001000",
                "test details");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("PESA"))
                .andExpect(jsonPath("$.amount").value("EUR 150.25"))
                .andExpect(jsonPath("$.debtorIban").value("EE382200221020145685"))
                .andExpect(jsonPath("$.creditorIban").value("LT121000011101001000"))
                .andExpect(jsonPath("$.details").value("test details"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.cancelledAt").isEmpty())
                .andExpect(jsonPath("$.cancellationFee").isEmpty());
    }

    @ParameterizedTest
    @MethodSource("swiftMethodSource")
    void shouldCreateNewSwiftPayment(CurrencyUnit currency, String debtorIban, String creditorIban) throws Exception {
        var paymentRequest = new PaymentRequest(
                Money.of(currency, 125.50),
                debtorIban,
                creditorIban,
                "test details");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("SWIFT"))
                .andExpect(jsonPath("$.amount").value("%s 125.50".formatted(currency.getCode())))
                .andExpect(jsonPath("$.debtorIban").value(debtorIban))
                .andExpect(jsonPath("$.creditorIban").value(creditorIban))
                .andExpect(jsonPath("$.details").value("test details"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.cancelledAt").isEmpty())
                .andExpect(jsonPath("$.cancellationFee").isEmpty());
    }

    @Test
    void shouldNotCreatePaymentWithUnsupportedCurrency() throws Exception {
        var paymentRequest = new PaymentRequest(
                Money.parse("CHF 150.25"),
                "EE382200221020145685",
                "LT121000011101001000",
                "test details");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private static Stream<Arguments> swiftMethodSource() {
        return Stream.of(
                Arguments.of(CurrencyUnit.USD, "EE382200221020145685", "LT121000011101001000"),
                Arguments.of(CurrencyUnit.EUR, "FR7630006000011234567890189", "LT121000011101001000"),
                Arguments.of(CurrencyUnit.EUR, "EE382200221020145685", "FR7630006000011234567890189")
        );
    }
}
