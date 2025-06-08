package io.github.bigpig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.bigpig.utils.LocalDateKeyMapDeserializer;

import java.time.LocalDate;
import java.util.TreeMap;

public record StockDataDTO (
    @JsonProperty("Meta Data") MetaDataDTO metaData,

    @JsonProperty("Time Series (Daily)")
    @JsonDeserialize(using = LocalDateKeyMapDeserializer.class)
    TreeMap<LocalDate, DailyDataDTO> timeSeriesDaily
) {}
