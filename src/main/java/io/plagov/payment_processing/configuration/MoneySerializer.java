package io.plagov.payment_processing.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.money.Money;

import java.io.IOException;

public class MoneySerializer extends JsonSerializer<Money> {

    @Override
    public void serialize(Money value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("currency", value.getCurrencyUnit().getCode());
        gen.writeFieldName("amount");
        gen.writeNumber(value.getAmount());
        gen.writeEndObject();
    }
}
