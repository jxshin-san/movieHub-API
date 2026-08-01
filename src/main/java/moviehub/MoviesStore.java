package moviehub;

import java.util.ArrayList;
import java.util.List;

public class MoviesStore {
    private final List<Movie> movies = new ArrayList<>();

    public List<Movie> getAll() {
        return new ArrayList<>(movies);
    }

    public void add(Movie movie) {
        movies.add(movie);
    }

    public void clear() {
        movies.clear();
    }

    public Movie getById(String id) {
        return movies.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public boolean removeById(String id) {
        return movies.removeIf(m -> m.getId().equals(id));
    }
}