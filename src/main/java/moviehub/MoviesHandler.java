package moviehub;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore store;
    private final Gson gson = new Gson();

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if (method.equalsIgnoreCase("GET")) {
            if (path.equals("/movies") || path.equals("/movies/")) {
                handleGetValues(ex);
            } else {
                handleGetById(ex, path);
            }
        } else if (method.equalsIgnoreCase("POST")) {
            handlePost(ex);
        } else if (method.equalsIgnoreCase("DELETE")) {
            handleDelete(ex, path);
        } else {
            ex.sendResponseHeaders(405, -1);
        }
    }

    private void handleGetValues(HttpExchange ex) throws IOException {
        List<Movie> movies = store.getAll();
        sendJson(ex, 200, gson.toJson(movies));
    }

    private void handleGetById(HttpExchange ex, String path) throws IOException {
        String[] pathParts = path.split("/");

        if (pathParts.length > 2) {
            String id = pathParts[2];
            Movie movie = store.getById(id);

            if (movie != null) {
                sendJson(ex, 200, gson.toJson(movie));
            } else {
                sendError(ex, 404, "Фильм с id " + id + " не найден");
            }
        } else {
            sendError(ex, 400, "Некорректный запрос");
        }
    }

    private void handlePost(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        try {
            Movie movie = gson.fromJson(body, Movie.class);

            if (movie.getId() == null || movie.getTitle() == null || movie.getTitle().isBlank()) {
                sendError(ex, 400, "Некорректные данные фильма");
                return;
            }

            store.add(movie);
            ex.sendResponseHeaders(201, -1);

        } catch (Exception e) {
            sendError(ex, 400, "Ошибка парсинга JSON");
        }
    }

    private void handleDelete(HttpExchange ex, String path) throws IOException {
        String[] pathParts = path.split("/");

        if (pathParts.length > 2) {
            String id = pathParts[2];
            boolean removed = store.removeById(id);

            if (removed) {
                sendNoContent(ex);
            } else {
                sendError(ex, 404, "Фильм с id " + id + " не найден");
            }
        } else {
            sendError(ex, 400, "Некорректный запрос");
        }
    }

    private void sendError(HttpExchange ex, int status, String message) throws IOException {
        ErrorResponse errorObj = new ErrorResponse(message);
        sendJson(ex, status, gson.toJson(errorObj));
    }
}