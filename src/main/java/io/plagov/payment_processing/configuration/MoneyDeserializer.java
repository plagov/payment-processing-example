package io.plagov.payment_processing.configuration;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.joda.money.Money;

import java.io.IOException;

public class MoneyDeserializer extends JsonDeserializer<Money> {

    @Override
    public Money deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        String value = parser.getValueAsString();
        return Money.parse(value);
    }
}
