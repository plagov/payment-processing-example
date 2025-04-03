package io.plagov.payment_processing.configuration;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.io.IOException;
import java.math.BigDecimal;

public class MoneyDeserializer extends JsonDeserializer<Money> {

    @Override
    public Money deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = parser.getCodec().readTree(parser);
        var currency = node.get("currency").asText();
        var amount = new BigDecimal(node.get("amount").asText());
        return Money.of(CurrencyUnit.of(currency), amount);
    }
}
