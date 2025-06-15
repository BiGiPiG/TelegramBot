package io.github.bigpig.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bigpig.dto.DailyDataDTO;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class LocalDateKeyMapDeserializer extends JsonDeserializer<Map<LocalDate, DailyDataDTO>> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<LocalDate, DailyDataDTO> deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        Map<LocalDate, DailyDataDTO> result = new TreeMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            LocalDate date = LocalDate.parse(entry.getKey(), formatter);
            DailyDataDTO dailyData = mapper.treeToValue(entry.getValue(), DailyDataDTO.class);
            result.put(date, dailyData);
        }

        return result;
    }
}

