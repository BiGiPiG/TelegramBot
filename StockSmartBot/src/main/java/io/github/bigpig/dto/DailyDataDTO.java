package io.github.bigpig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public record DailyDataDTO (
    @JsonProperty("1. open") String open,
    @JsonProperty("2. high") String high,
    @JsonProperty("3. low") String low,
    @JsonProperty("4. close") String close,
    @JsonProperty("5. volume") String volume
) {}
