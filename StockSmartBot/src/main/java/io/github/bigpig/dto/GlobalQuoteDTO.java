package io.github.bigpig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GlobalQuoteDTO (
    @JsonProperty("01. symbol") String symbol,
    @JsonProperty("02. open") String openPrice, // раньше было Double
    @JsonProperty("03. high") String highPrice,
    @JsonProperty("04. low") String lowPrice,
    @JsonProperty("05. price") String currentPrice,
    @JsonProperty("06. volume") String volume,
    @JsonProperty("07. latest trading day") String latestTradingDay,
    @JsonProperty("08. previous close") String previousClose,
    @JsonProperty("09. change") String priceChange,
    @JsonProperty("10. change percent") String changePercent
) {}