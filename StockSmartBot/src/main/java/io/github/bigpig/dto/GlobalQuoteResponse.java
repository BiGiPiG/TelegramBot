package io.github.bigpig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GlobalQuoteResponse {
    @JsonProperty("Global Quote")
    private GlobalQuoteDTO globalQuote;
}