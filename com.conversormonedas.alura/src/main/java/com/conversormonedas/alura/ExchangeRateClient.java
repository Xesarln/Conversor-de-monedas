package com.conversormonedas.alura;
import com.google.gson.Gson;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class ExchangeRateClient {

    private final String apiKey;
    private final HttpClient http;
    private final Gson gson = new Gson();

    public ExchangeRateClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Map<String, BigDecimal> getRates(String baseCurrency) throws IOException, InterruptedException {
        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/" + baseCurrency;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new IOException("HTTP " + resp.statusCode() + " - " + resp.body());
        }

        RateResponse data = gson.fromJson(resp.body(), RateResponse.class);

        if (data == null || data.conversion_rates == null) {
            throw new IOException("Respuesta inválida de la API.");
        }

        if (data.result != null && !data.result.equalsIgnoreCase("success")) {
            throw new IOException("API error: " + data.result + " / " + data.error_type);
        }

        return data.conversion_rates;
    }

    static class RateResponse {
        String result;
        String error_type;
        String base_code;
        Map<String, BigDecimal> conversion_rates;
    }
}