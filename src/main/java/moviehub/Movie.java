package moviehub;

public class Movie {
    private final String id;
    private final String title;
    private final Integer year;

    public Movie(String id, String title, Integer year) {
        this.id = id;
        this.title = title;
        this.year = year;
    }

    public Movie(String id, String title) {
        this.id = id;
        this.title = title;
        this.year = null;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }
}