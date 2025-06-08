package io.github.bigpig.services;

import io.github.bigpig.dto.DailyDataDTO;
import io.github.bigpig.dto.StockDataDTO;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ChartService {

    private static final int MAX_DAYS = 50;

    @Value("${alphaVantage.apiKey}")
    String apiKey;

    private final RestTemplate restTemplate;

    public ChartService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void generateChart(String ticker) throws IOException {
//        String urlString = UriComponentsBuilder.fromUriString("https://www.alphavantage.co/query")
//                .queryParam("function", "TIME_SERIES_DAILY")
//                .queryParam("symbol", ticker)
//                .queryParam("apikey", apiKey)
//                .toUriString();
        String urlString = "https://run.mocky.io/v3/f7165331-8668-4b50-bfee-9ec88c072e39";

        // Получение JSON
        ResponseEntity<StockDataDTO> stockDataResponse = restTemplate.getForEntity(urlString, StockDataDTO.class);
        Map<LocalDate, DailyDataDTO> timeSeriesDaily = getLocalDateDailyDataDTOMap(ticker, stockDataResponse);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<LocalDate, DailyDataDTO> sortedTimeSeries = new TreeMap<>(timeSeriesDaily);
        sortedTimeSeries.entrySet().stream()
                .skip(Math.max(0, sortedTimeSeries.size() - MAX_DAYS))
                .forEach(entry -> dataset.addValue(Double.parseDouble(entry.getValue().close()), "Price", entry.getKey().toString()));


        // Построение графика
        JFreeChart chart = buildChart(ticker, dataset);

        // Сохранение графика
        Path chartPath = Paths.get( "tmpDir", ticker + ".png");
        File outputFile = new File(chartPath.toString());
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
    }

    private static Map<LocalDate, DailyDataDTO> getLocalDateDailyDataDTOMap(String ticker, ResponseEntity<StockDataDTO> stockDataResponse) throws IOException {
        if (stockDataResponse.getStatusCode().isError() || stockDataResponse.getBody() == null) {
            throw new IOException("Нет данных о ценах для " + ticker);
        }

        StockDataDTO stockData = stockDataResponse.getBody();
        Map<LocalDate, DailyDataDTO> timeSeriesDaily = stockData.timeSeriesDaily();
        if (timeSeriesDaily == null || timeSeriesDaily.isEmpty()) {
            throw new IOException("Пустые данные по акциям для " + ticker);
        }

        return timeSeriesDaily;
    }

    private JFreeChart buildChart(String ticker, DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createLineChart(
                null, // Заголовок зададим вручную
                "Date",
                "Price (USD)",
                dataset
        );

        // Заголовок графика
        chart.setTitle(new TextTitle("График цены акции " + ticker, new Font("Arial", Font.BOLD, 18)));
        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(240, 240, 240));
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setOutlineVisible(false);

        // Формат осей

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));

        // Отрисовка линии и точек
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(30, 144, 255));
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-2, -2, 4, 4));

        return chart;
    }
}
