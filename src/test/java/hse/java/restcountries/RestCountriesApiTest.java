package hse.java.restcountries;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestCountriesApiTest {

    private static final String BASE_URL = "https://restcountries.com/v3.1";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode getArray(String path) throws Exception {
        HttpResponse<String> response = get(path);
        assertEquals(200, response.statusCode());
        JsonNode root = MAPPER.readTree(response.body());
        assertTrue(root.isArray());
        return root;
    }

    private boolean containsCommonName(JsonNode countries, String expectedName) {
        for (JsonNode country : countries) {
            if (expectedName.equals(country.path("name").path("common").asText())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCapital(JsonNode countries, String expectedCapital) {
        for (JsonNode country : countries) {
            JsonNode capital = country.path("capital");
            if (capital.isArray()) {
                for (JsonNode item : capital) {
                    if (expectedCapital.equals(item.asText())) {
                        return true;
                    }
                }
            } else if (expectedCapital.equals(capital.asText())) {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("GET /all возвращает непустой список стран")
    void allReturnsNonEmptyList() throws Exception {
        JsonNode countries = getArray("/all");
        assertTrue(countries.size() > 0);
    }

    @Test
    @DisplayName("GET /name/russia возвращает страну со столицей Moscow")
    void russiaHasMoscowCapital() throws Exception {
        JsonNode countries = getArray("/name/russia");
        assertTrue(containsCapital(countries, "Moscow"));
    }

    @Test
    @DisplayName("GET /alpha/de возвращает страну Germany")
    void alphaDeReturnsGermany() throws Exception {
        JsonNode countries = getArray("/alpha/de");
        assertTrue(containsCommonName(countries, "Germany"));
    }

    @Test
    @DisplayName("GET /name/nonexistentcountryxyz возвращает 404")
    void nonexistentCountryReturns404() throws Exception {
        HttpResponse<String> response = get("/name/nonexistentcountryxyz");
        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("GET /all возвращает страны с population больше нуля")
    void allCountriesHavePositivePopulation() throws Exception {
        JsonNode countries = getArray("/all");
        for (JsonNode country : countries) {
            assertTrue(country.path("population").asLong(0) > 0);
        }
    }

    @Test
    @DisplayName("GET /name/canada возвращает страну Canada")
    void nameCanadaReturnsCanada() throws Exception {
        JsonNode countries = getArray("/name/canada");
        assertTrue(containsCommonName(countries, "Canada"));
    }

    @Test
    @DisplayName("GET /alpha/jp возвращает страну Japan")
    void alphaJpReturnsJapan() throws Exception {
        JsonNode countries = getArray("/alpha/jp");
        assertTrue(containsCommonName(countries, "Japan"));
    }
}

