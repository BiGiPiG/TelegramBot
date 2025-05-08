package io.github.bigpig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
public class ShareDTO {
    @JsonProperty("Symbol")
    private String symbol;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Sector")
    private String sector;

    @JsonProperty("Industry")
    private String industry;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("EPS")
    private double eps;

    @JsonProperty("PriceToSalesRatioTTM")
    private double priceToSales;

    @JsonProperty("MarketCapitalization")
    private double marketCap;

    @JsonProperty("DilutedEPSTTM")
    private double earningsPerShare;

    @JsonProperty("BookValue")
    private double bookValue;

    @JsonProperty("Global Quote")
    private GlobalQuoteDTO globalQuote;

    @JsonProperty("PriceToBookRatio")
    private double pbRatio;

    @JsonProperty("PERatio")
    private double peRatio;

    @JsonProperty("EBITDA")
    private double ebitda;

    @JsonProperty("RevenueTTM")
    private double revenue;

    @JsonProperty("GrossProfitTTM")
    private double grossProfit;

    @JsonProperty("DividendYield")
    private double dividendYield;

    @JsonProperty("ReturnOnEquityTTM")
    private double returnOnEquity;

    @JsonProperty("ProfitMargin")
    private double profitMargin;

    @JsonProperty("PEGRatio")
    private double pegRatio;

    @JsonProperty("EVToEBITDA")
    private double EVToEBITDA;

    @JsonProperty("AnalystTargetPrice")
    private double analystTargetPrice;
}
