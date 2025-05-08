package io.github.bigpig.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DailyDataDTO {
    @JsonProperty("1. open")
    public String open;

    @JsonProperty("2. high")
    public String high;

    @JsonProperty("3. low")
    public String low;

    @JsonProperty("4. close")
    public String close;

    @JsonProperty("5. volume")
    public String volume;
}
