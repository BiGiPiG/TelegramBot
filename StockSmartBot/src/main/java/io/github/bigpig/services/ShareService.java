package io.github.bigpig.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bigpig.dto.GlobalQuoteDTO;
import io.github.bigpig.dto.GlobalQuoteResponse;
import io.github.bigpig.dto.ShareDTO;
import io.github.bigpig.exceptions.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Service
public class ShareService {

    @Value("${alphaVantage.apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ShareService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ShareDTO calculateValuationMetrics(String ticker) {
        try {
            GlobalQuoteDTO globalQuoteDTO = fetchGlobalQuote(ticker);
            return fetchShareDTO(ticker).withGlobalQuote(globalQuoteDTO);
        } catch (NumberFormatException e) {
            throw new CalculateValuationException("Error parsing numeric values from API for ticker: " + ticker, e);
        } catch (RestClientException e) {
            throw new ExternalApiException("Network or API error while fetching data for " + ticker, e);
        }
    }

    public GlobalQuoteDTO fetchGlobalQuote(String ticker) {
        String globalQuoteUrl = buildUrl(ticker, "GLOBAL_QUOTE");

        // для отладки
        // String globalQuoteUrl = "https://run.mocky.io/v3/e59fd1a6-83a1-40ff-955e-a705de55bf73";

        ResponseEntity<GlobalQuoteResponse> response = restTemplate.
                getForEntity(globalQuoteUrl, GlobalQuoteResponse.class);

        GlobalQuoteResponse body = extractBodyOrThrow(response, "Failed to fetch global quote data");
        GlobalQuoteDTO globalQuote = body.globalQuote();

        if (globalQuote == null) {
            throw new GlobalQuoteNotFoundException("Global quote not found for ticker: " + ticker);
        }

        return globalQuote;
    }

    public ShareDTO fetchShareDTO(String ticker) {
        String overviewUrl = buildUrl(ticker, "OVERVIEW");

        ResponseEntity<ShareDTO> shareDTOResponse = restTemplate.getForEntity(overviewUrl, ShareDTO.class);

        return extractBodyOrThrow(shareDTOResponse, "Failed to fetch shareDTO data");
    }

    public String buildUrl(String ticker, String func) {
        return UriComponentsBuilder.fromUriString("https://www.alphavantage.co/query")
                .queryParam("function", func)
                .queryParam("symbol", ticker)
                .queryParam("apikey", apiKey)
                .toUriString();
    }

    public <T> T extractBodyOrThrow(ResponseEntity<T> response, String errorMessage) {
        if (response.getStatusCode().isError()) {
            throw new ExternalApiException(errorMessage + ". Status: " + response.getStatusCode());
        }
        T body = response.getBody();
        if (body == null) {
            throw new NullResponseBodyException(errorMessage + ". Response body is null");
        }
        return body;
    }

    @Async
    public CompletableFuture<String> getSmartAnalyse(String ticker) {
        String apiUrl = "http://localhost:8000/smartAnalyze/";
        try {

            ShareDTO share = calculateValuationMetrics(ticker);
            String jsonBody = objectMapper.writeValueAsString(share);

            System.out.println(jsonBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.completedFuture(response.getBody());
            } else {
                return CompletableFuture.failedFuture(
                        new SmartAnalysisException("Smart API returned error status: " + response.getStatusCode())
                );
            }
        } catch (HttpStatusCodeException e) {
            throw new SmartAnalysisException("Smart API HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            throw new SmartAnalysisException("Cannot reach Smart Analysis API. Please check if the server is running.", e);
        } catch (Exception e) {
            throw new SmartAnalysisException("Unexpected error while processing smart analysis for " + ticker + Arrays.toString(e.getStackTrace()), e);
        }
    }
}