package io.github.bigpig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalQuoteDTO {

    @JsonProperty("01. symbol")
    private String symbol;

    @JsonProperty("02. open")
    private String openPrice; // раньше было Double

    @JsonProperty("03. high")
    private String highPrice;

    @JsonProperty("04. low")
    private String lowPrice;

    @JsonProperty("05. price")
    private String currentPrice;

    @JsonProperty("06. volume")
    private String volume;

    @JsonProperty("07. latest trading day")
    private String latestTradingDay;

    @JsonProperty("08. previous close")
    private String previousClose;

    @JsonProperty("09. change")
    private String priceChange;

    @JsonProperty("10. change percent")
    private String changePercent;
}