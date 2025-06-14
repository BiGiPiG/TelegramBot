import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bigpig.dto.GlobalQuoteDTO;
import io.github.bigpig.dto.GlobalQuoteResponse;
import io.github.bigpig.dto.ShareDTO;
import io.github.bigpig.exceptions.ExternalApiException;
import io.github.bigpig.exceptions.GlobalQuoteNotFoundException;
import io.github.bigpig.exceptions.NullResponseBodyException;
import io.github.bigpig.services.ShareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ShareServiceTest {

    private ShareService shareService;
    private RestTemplate restTemplate;
    private String ticker;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        shareService = new ShareService(restTemplate, objectMapper);
        ticker = "IBM";

        ReflectionTestUtils.setField(shareService, "apiKey", "demo");
    }

    @Test
    @DisplayName("buildUrl Test")
    void buildUrlTest() {
        String expected = shareService.buildUrl("IBM", "OVERVIEW");
        String actual = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=IBM&apikey=demo";

        assertEquals(actual, expected);
    }

    @Test
    @DisplayName("fetchShareDTO shouldReturnExpectedDto Test")
    void fetchShareDTO_shouldReturnExpectedDto() {

        ShareDTO expected = buildExpectedShareDTO();

        when(restTemplate.getForEntity(anyString(), eq(ShareDTO.class)))
                .thenReturn(new ResponseEntity<>(expected, HttpStatus.OK));

        ShareDTO actual = shareService.fetchShareDTO(ticker);

        assertEquals(actual, expected);
    }

    @Test
    @DisplayName("fetchShareDTO shouldThrowNullResponseBodyException Test")
    void fetchShareDTO_shouldThrowNullResponseBodyException() {

        when(restTemplate.getForEntity(anyString(), eq(ShareDTO.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        NullResponseBodyException exception = assertThrows(
                NullResponseBodyException.class,
                () -> shareService.fetchShareDTO(ticker));
        assertEquals("Failed to fetch shareDTO data. Response body is null",
                exception.getMessage());
    }

    @Test
    @DisplayName("fetchShareDTO shouldThrowExternalApiException Test")
    void fetchShareDTO_shouldThrowExternalApiException() {

        when(restTemplate.getForEntity(anyString(), eq(ShareDTO.class)))
                .thenReturn(new ResponseEntity<>(buildExpectedShareDTO(), HttpStatus.NOT_FOUND));

        ExternalApiException exception = assertThrows(
                ExternalApiException.class,
                () -> shareService.fetchShareDTO(ticker));
        assertEquals("Failed to fetch shareDTO data. Status: 404 NOT_FOUND",
                exception.getMessage());
    }

    @Test
    @DisplayName("fetchGlobalQuote shouldReturnExpectedDto Test")
    void fetchGlobalQuote_shouldReturnExpectedDto() {

        GlobalQuoteResponse response = new GlobalQuoteResponse(buildExpectedGlobalQuoteDTO());

        when(restTemplate.getForEntity(anyString(), eq(GlobalQuoteResponse.class))).
                thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        GlobalQuoteDTO expected = buildExpectedGlobalQuoteDTO();
        GlobalQuoteDTO actual = shareService.fetchGlobalQuote(ticker);
        assertEquals(actual, expected);
    }

    @Test
    @DisplayName("fetchGlobalQuote shouldThrowNullResponseBodyException")
    void fetchGlobalQuote_shouldThrowNullResponseBodyException() {

        when(restTemplate.getForEntity(anyString(), eq(GlobalQuoteResponse.class))).
                thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        NullResponseBodyException exception = assertThrows(
                NullResponseBodyException.class,
                () -> shareService.fetchGlobalQuote(ticker));
        assertEquals("Failed to fetch global quote data. Response body is null",
                exception.getMessage());
    }

    @Test
    @DisplayName("fetchGlobalQuote shouldThrowExternalApiException Test")
    void fetchGlobalQuote_shouldThrowExternalApiException() {

        GlobalQuoteResponse response = new GlobalQuoteResponse(buildExpectedGlobalQuoteDTO());

        when(restTemplate.getForEntity(anyString(), eq(GlobalQuoteResponse.class))).
                thenReturn(new ResponseEntity<>(response, HttpStatus.NOT_FOUND));

        ExternalApiException exception = assertThrows(
                ExternalApiException.class,
                () -> shareService.fetchGlobalQuote(ticker));
        assertEquals("Failed to fetch global quote data. Status: 404 NOT_FOUND",
                exception.getMessage());
    }

    @Test
    @DisplayName("fetchGlobalQuote shouldThrowNotFoundGlobalQuoteException")
    void fetchGlobalQuote_shouldThrowNotFoundGlobalQuoteException() {

        GlobalQuoteResponse response = new GlobalQuoteResponse(null);

        when(restTemplate.getForEntity(anyString(), eq(GlobalQuoteResponse.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        GlobalQuoteNotFoundException exception = assertThrows(
            GlobalQuoteNotFoundException.class,
            () -> shareService.fetchGlobalQuote(ticker));

        assertEquals("Global quote not found for ticker: IBM",
                exception.getMessage());
    }

    @Test
    @DisplayName("calculateValuationMetrics shouldReturnExpectedValuationMetrics Test")
    void calculateValuationMetrics_shouldReturnExpectedValuationMetrics() {

        GlobalQuoteResponse response = new GlobalQuoteResponse(buildExpectedGlobalQuoteDTO());
        ShareDTO shareDTO = buildExpectedShareDTO();

        when(restTemplate.getForEntity(anyString(), eq(GlobalQuoteResponse.class))).thenReturn(
                new ResponseEntity<>(response, HttpStatus.OK)
        );

        when(restTemplate.getForEntity(anyString(), eq(ShareDTO.class))).thenReturn(
                new ResponseEntity<>(shareDTO, HttpStatus.OK)
        );

        ShareDTO actual = shareService.calculateValuationMetrics(ticker);
        ShareDTO expected = buildExpectedShareDTO().withGlobalQuote(buildExpectedGlobalQuoteDTO());
        assertEquals(actual, expected);
    }

    private ShareDTO buildExpectedShareDTO() {
        return new ShareDTO(
            "IBM",
            "International Business Machines",
            "TECHNOLOGY",
            "COMPUTER & OFFICE EQUIPMENT",
            "USA",
            "International Business Machines Corporation (IBM) is an American multinational technology company" +
                    " headquartered in Armonk, New York, with operations in over 170 countries. The company began in 1911," +
                    " founded in Endicott, New York, as the Computing-Tabulating-Recording Company (CTR) and was renamed" +
                    " International Business Machines in 1924. IBM is incorporated in New York. IBM produces and sells " +
                    "computer hardware, middleware and software, and provides hosting and consulting services in areas " +
                    "ranging from mainframe computers to nanotechnology. IBM is also a major research organization, " +
                    "holding the record for most annual U.S. patents generated by a business (as of 2020) for 28 " +
                    "consecutive years. Inventions by IBM include the automated teller machine (ATM), the floppy disk, " +
                    "the hard disk drive, the magnetic stripe card, the relational database, the SQL programming language, " +
                    "the UPC barcode, and dynamic random-access memory (DRAM). The IBM mainframe, exemplified by the " +
                    "System/360, was the dominant computing platform during the 1960s and 1970s.",
            5.85,
            4.024,
            2.52870328E11,
            5.85,
            28.92,
            null,
            9.41,
            46.51,
            1.395E10,
            6.2832001E10,
            3.584E10,
            0.0247,
            0.218,
            0.0871,
            2.059,
            24.51,
            253.27
        );
    }

    public GlobalQuoteDTO buildExpectedGlobalQuoteDTO() {
        return new GlobalQuoteDTO(
            "IBM",
            "249.4500",
            "254.4700",
            "248.8320",
            "253.3700",
            "3400001",
            "2025-05-07",
            "249.1200",
            "4.2500",
            "1.7060%"
        );
    }
}
