package io.github.bigpig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.bigpig.utils.LocalDateKeyMapDeserializer;
import lombok.Data;

import java.time.LocalDate;
import java.util.TreeMap;

@Data
public class StockDataDTO {
    @JsonProperty("Meta Data")
    public MetaDataDTO metaData;

    @JsonProperty("Time Series (Daily)")
    @JsonDeserialize(using = LocalDateKeyMapDeserializer.class)
    public TreeMap<LocalDate, DailyDataDTO> timeSeriesDaily;
}
