package io.github.bigpig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GlobalQuoteResponse (
    @JsonProperty("Global Quote") GlobalQuoteDTO globalQuote
) {}