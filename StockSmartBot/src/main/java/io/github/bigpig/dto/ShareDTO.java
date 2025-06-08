package io.github.bigpig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ShareDTO(
        @JsonProperty("Symbol") String symbol,
        @JsonProperty("Name") String name,
        @JsonProperty("Sector") String sector,
        @JsonProperty("Industry") String industry,
        @JsonProperty("Country") String country,
        @JsonProperty("Description") String description,
        @JsonProperty("EPS") double eps,
        @JsonProperty("PriceToSalesRatioTTM") double priceToSales,
        @JsonProperty("MarketCapitalization") double marketCap,
        @JsonProperty("DilutedEPSTTM") double earningsPerShare,
        @JsonProperty("BookValue") double bookValue,
        @JsonProperty("Global Quote") GlobalQuoteDTO globalQuote,
        @JsonProperty("PriceToBookRatio") double pbRatio,
        @JsonProperty("PERatio") double peRatio,
        @JsonProperty("EBITDA") double ebitda,
        @JsonProperty("RevenueTTM") double revenue,
        @JsonProperty("GrossProfitTTM") double grossProfit,
        @JsonProperty("DividendYield") double dividendYield,
        @JsonProperty("ReturnOnEquityTTM") double returnOnEquity,
        @JsonProperty("ProfitMargin") double profitMargin,
        @JsonProperty("PEGRatio") double pegRatio,
        @JsonProperty("EVToEBITDA") double EVToEBITDA,
        @JsonProperty("AnalystTargetPrice") double analystTargetPrice
) {
    public ShareDTO withGlobalQuote(GlobalQuoteDTO newGlobalQuote) {
        return new ShareDTO(
                symbol, name, sector, industry, country, description,
                eps, priceToSales, marketCap, earningsPerShare, bookValue,
                newGlobalQuote,
                pbRatio, peRatio, ebitda, revenue, grossProfit,
                dividendYield, returnOnEquity, profitMargin, pegRatio,
                EVToEBITDA, analystTargetPrice
        );
    }
}