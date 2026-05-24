package moviehub;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тесты API фильмов")
public class MoviesApiTest {

    private static final String BASE = "http://localhost:8080";

    private static MoviesServer server;
    private static MoviesStore store;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(store);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        store.clear();
    }

    @AfterAll
    static void afterAll() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("Получение фильмов на пустом логе")
    void testGetMoviesEmpty() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        assertEquals(200, response.statusCode());

        String contentType = response.headers()
                .firstValue("Content-Type")
                .orElse("");

        assertEquals("application/json; charset=UTF-8", contentType);

        String body = response.body().trim();

        assertTrue(body.startsWith("[") && body.endsWith("]"));
        assertEquals("[]", body);
    }

    @Test
    @DisplayName("Успешное добавление фильма")
    void testAddMovie() throws Exception {
        String movieJson = "{\"id\":\"1\",\"title\":\"Interstellar\"}";

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        movieJson,
                        StandardCharsets.UTF_8
                ))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> postResponse = client.send(
                postRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        assertEquals(201, postResponse.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(
                getRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        assertEquals(
                "[{\"id\":\"1\",\"title\":\"Interstellar\"}]",
                getResponse.body().trim()
        );
    }

    @Test
    @DisplayName("Добавление фильма с пустым названием")
    void testAddMovieWithEmptyTitle() throws Exception {
        String movieJson = "{\"id\":\"2\",\"title\":\"\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        movieJson,
                        StandardCharsets.UTF_8
                ))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        assertEquals(400, response.statusCode());

        String body = response.body().trim();

        assertTrue(body.contains("\"error\""));
        assertTrue(body.contains("Некорректные данные фильма"));
    }

    @Test
    @DisplayName("Получение фильма по id")
    void testGetMovieById() throws Exception {
        store.add(new Movie("10", "Inception"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/10"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"title\":\"Inception\""));
    }

    @Test
    @DisplayName("Получение несуществующего фильма")
    void testGetMovieByIdNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Фильм с id 999 не найден"));
    }

    @Test
    @DisplayName("Удаление фильма")
    void testDeleteMovie() throws Exception {
        store.add(new Movie("55", "The Matrix"));

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/55"))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(
                deleteRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        assertEquals(204, deleteResponse.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/55"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(
                getRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        assertEquals(404, getResponse.statusCode());
    }
}