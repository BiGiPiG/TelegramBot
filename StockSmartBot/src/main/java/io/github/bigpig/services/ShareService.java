package io.github.bigpig.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bigpig.dto.GlobalQuoteDTO;
import io.github.bigpig.dto.GlobalQuoteResponse;
import io.github.bigpig.dto.ShareDTO;
import io.github.bigpig.exceptions.ExternalApiException;
import io.github.bigpig.exceptions.ShareNotFoundException;
import io.github.bigpig.exceptions.CalculateValuationException;
import io.github.bigpig.exceptions.SmartAnalysisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

            ShareDTO shareDTO = fetchShareDTO(ticker);

            shareDTO.setGlobalQuote(globalQuoteDTO);

            return shareDTO;
        } catch (NumberFormatException e) {
            throw new CalculateValuationException("Error parsing numeric values from API for ticker: " + ticker, e);
        } catch (RestClientException e) {
            throw new ExternalApiException("Network or API error while fetching data for " + ticker, e);
        } catch (Exception e) {
            throw new CalculateValuationException("Unexpected error occurred while processing ticker: " + ticker, e);
        }
    }

    public GlobalQuoteDTO fetchGlobalQuote(String ticker) {
        //String globalQuoteUrl = buildUrl(ticker, "GLOBAL_QUOTE");

        //для отладки
        String globalQuoteUrl = "https://run.mocky.io/v3/e59fd1a6-83a1-40ff-955e-a705de55bf73";

        ResponseEntity<GlobalQuoteResponse> response = restTemplate.
                getForEntity(globalQuoteUrl, GlobalQuoteResponse.class);

        GlobalQuoteResponse body = extractBodyOrThrow(response, "Failed to fetch global quote data");
        GlobalQuoteDTO globalQuote = body.getGlobalQuote();

        if (globalQuote == null) {
            throw new ShareNotFoundException("Global quote not found for ticker: " + ticker);
        }

        return globalQuote;
    }

    public ShareDTO fetchShareDTO(String ticker) {
        //String overviewUrl = buildUrl(ticker, "OVERVIEW");

        //для отладки
        String overviewUrl = "https://run.mocky.io/v3/90c6759a-0a71-4b8c-94aa-0b9859c51f9a";
        ResponseEntity<ShareDTO> shareDTOResponse = restTemplate.getForEntity(overviewUrl, ShareDTO.class);

        return extractBodyOrThrow(shareDTOResponse, "Failed to fetch shareDTO data");
    }

    private String buildUrl(String ticker, String func) {
        return UriComponentsBuilder.fromUriString("https://www.alphavantage.co/query")
                .queryParam("function", func)
                .queryParam("symbol", ticker)
                .queryParam("apikey", apiKey)
                .toUriString();
    }

    private <T> T extractBodyOrThrow(ResponseEntity<T> response, String errorMessage) {
        if (response.getStatusCode().isError()) {
            throw new ExternalApiException(errorMessage + " Status: " + response.getStatusCode());
        }
        T body = response.getBody();
        if (body == null) {
            throw new ShareNotFoundException(errorMessage + " Response body is null");
        }
        return body;
    }

    @Async
    public CompletableFuture<String> getSmartAnalyse(String ticker) {
        String apiUrl = "http://127.0.0.1:8000/smartAnalyze/";
        try {

            ShareDTO share = calculateValuationMetrics(ticker);
            String jsonBody = objectMapper.writeValueAsString(share);

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
                throw new SmartAnalysisException("Smart API returned error status: " + response.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            throw new SmartAnalysisException("Smart API HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            throw new SmartAnalysisException("Cannot reach Smart Analysis API. Please check if the server is running.", e);
        } catch (Exception e) {
            throw new SmartAnalysisException("Unexpected error while processing smart analysis for " + ticker + e.getMessage(), e);
        }
    }
}