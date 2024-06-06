import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.JSONObject;

public class Testing {


    // some tests for locally testing methods in the server
    @Test
    public void typeWrong() {
        JSONObject req = new JSONObject();
        req.put("type1", "echo");

        JSONObject res = SockServer.testField(req, "type");

        assertEquals(res.getBoolean("ok"), false);
        assertEquals(res.getString("message"), "Field type does not exist in request");
    }

    @Test
    public void echoCorrect() {
        JSONObject req = new JSONObject();
        req.put("type", "echo");
        req.put("data", "whooooo");
        JSONObject res = SockServer.echo(req);

        assertEquals("echo", res.getString("type"));
        assertEquals(res.getBoolean("ok"), true);
        assertEquals(res.getString("echo"), "Here is your echo: whooooo");
    }

    @Test
    public void echoErrors() {
        JSONObject req = new JSONObject();
        req.put("type", "echo");
        req.put("data1", "whooooo");
        JSONObject res = SockServer.echo(req);

        assertEquals(res.getBoolean("ok"), false);
        assertEquals(res.getString("message"), "Field data does not exist in request");

        JSONObject req2 = new JSONObject();
        req2.put("type", "echo");
        req2.put("data", 33);
        JSONObject res2 = SockServer.echo(req2);

        assertEquals(false, res2.getBoolean("ok"));
        assertEquals(res2.getString("message"), "Field data needs to be of type: String");

        JSONObject req3 = new JSONObject();
        req3.put("type", "echo");
        req3.put("data", true);
        JSONObject res3 = SockServer.echo(req3);

        assertEquals(res3.getBoolean("ok"), false);
        assertEquals(res3.getString("message"), "Field data needs to be of type: String");
    }
    @Test
    public void addMovie() {
        JSONObject req = new JSONObject();
        req.put("type", "rating");
        req.put("task", "add");
        req.put("movie", "Berserk");
        req.put("rating", 5);
        req.put("username", "kariver4");

        JSONObject res = SockServer.rating(req);

        assertTrue(res.getBoolean("ok"));
        assertEquals("rating", res.getString("type"));
        JSONArray movies = res.getJSONArray("movies");
        assertEquals(1, movies.length());
        JSONObject movie = movies.getJSONObject(0);
        assertEquals("Berserk", movie.getString("movie"));
        assertEquals(5, movie.getInt("rating"));
        assertEquals("kariver4", movie.getJSONArray("raters").getString(0));
    }

    @Test
    public void addMovieAlreadyExists() {
        JSONObject req = new JSONObject();
        req.put("type", "rating");
        req.put("task", "add");
        req.put("movie", "Berserk");
        req.put("rating", 5);
        req.put("username", "kariver4");

        SockServer.rating(req); // Add first time

        JSONObject res = SockServer.rating(req); // Try to add again

        assertFalse(res.getBoolean("ok"));
        assertEquals("Berserk has already been added.", res.getString("message"));
    }

    @Test
    public void rateMovie() {
        JSONObject req = new JSONObject();
        req.put("type", "rating");
        req.put("task", "add");
        req.put("movie", "Samurai Champloo");
        req.put("rating", 5);
        req.put("username", "kariver4");

        SockServer.rating(req); // Add movie

        JSONObject req2 = new JSONObject();
        req2.put("type", "rating");
        req2.put("task", "rate");
        req2.put("movie", "Samurai Champloo");
        req2.put("rating", 4);
        req2.put("username", "wona");

        JSONObject res = SockServer.rating(req2); // Rate movie

        assertTrue(res.getBoolean("ok"));
        assertEquals("rating", res.getString("type"));
        JSONArray movies = res.getJSONArray("movies");
        assertEquals(1, movies.length());
        JSONObject movie = movies.getJSONObject(0);
        assertEquals("Samurai Champloo", movie.getString("movie"));
        assertEquals(4.0, movie.getInt("rating"), 0.01);
        JSONArray raters = movie.getJSONArray("raters");
        assertEquals(2, raters.length());
        assertTrue(raters.toList().contains("kariver4"));
        assertTrue(raters.toList().contains("wona"));
    }

    @Test
    public void rateMovieAlreadyRated() {
        JSONObject req = new JSONObject();
        req.put("type", "rating");
        req.put("task", "add");
        req.put("movie", "Claymore");
        req.put("rating", 5);
        req.put("username", "kariver4");

        SockServer.rating(req); // Add movie

        JSONObject req2 = new JSONObject();
        req2.put("type", "rating");
        req2.put("task", "rate");
        req2.put("movie", "Claymore");
        req2.put("rating", 4);
        req2.put("username", "kariver4");

        JSONObject res = SockServer.rating(req2); // Rate movie with the same user

        assertFalse(res.getBoolean("ok"));
        assertEquals("You already provided a rating for this movie.", res.getString("message"));
    }

    @Test
    public void viewAllRatings() {
        // Create a request to view all ratings
        JSONObject reqView = new JSONObject();
        reqView.put("type", "rating");
        reqView.put("task", "view");  // Set the task to "view"

        // Get the response from the viewRating method
        JSONObject res = SockServer.viewRating(reqView);

        // Verify the response
        assertTrue(res.getBoolean("ok"));
        assertEquals("rating", res.getString("type"));

        // Get the array of movies from the response
        JSONArray movies = res.getJSONArray("movies");

        // Check the number of movies and their details if needed
        assertEquals(2, movies.length());  // Update this line with the correct expected number of movies


        // Validate the first movie's details
        JSONObject movie1 = movies.getJSONObject(0);
        assertEquals("Samurai Champloo", movie1.getString("movie"));
        assertEquals(4, movie1.getInt("rating"));
        assertEquals("wona", movie1.getJSONArray("raters").getString(0));

        // Validate the second movie's details
        JSONObject movie2 = movies.getJSONObject(1);
        assertEquals("Berserk", movie2.getString("movie"));
        assertEquals(5, movie2.getInt("rating"));
        assertEquals("kariver4", movie2.getJSONArray("raters").getString(0));
    }

    @Test
    public void viewSpecificMovieRating() {
        JSONObject req = new JSONObject();
        req.put("type", "rating");
        req.put("task", "add");
        req.put("movie", "Tokyo Ghoul");
        req.put("rating", 5);
        req.put("username", "kariver4");

        SockServer.rating(req); // Add movie

        JSONObject reqView = new JSONObject();
        reqView.put("type", "rating");
        reqView.put("task", "view");
        reqView.put("movie", "Tokyo Ghoul");

        JSONObject res = SockServer.rating(reqView);

        assertTrue(res.getBoolean("ok"));
        assertEquals("rating", res.getString("type"));
        JSONArray movies = res.getJSONArray("movies");
        assertEquals(1, movies.length());

        JSONObject movie = ((JSONArray) movies).getJSONObject(0);
        assertEquals("Tokyo Ghoul", movie.getString("movie"));
        assertEquals(5, movie.getInt("rating"));
        assertEquals("kariver4", movie.getJSONArray("raters").getString(0));
    }

    @Test
    public void viewSpecificMovieNotAdded() {
        JSONObject reqView = new JSONObject();
        reqView.put("type", "rating");
        reqView.put("task", "view");
        reqView.put("movie", "The Venture Bros");

        JSONObject res = SockServer.rating(reqView);

        assertFalse(res.getBoolean("ok"));
        assertEquals("There are no ratings yet.", res.getString("message"));
    }
}