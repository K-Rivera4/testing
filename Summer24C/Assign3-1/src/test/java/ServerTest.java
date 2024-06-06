import org.json.JSONArray;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ServerTest {

    Socket sock;
    OutputStream out;
    ObjectOutputStream os;

    DataInputStream in;


    // Establishing a connection to the server, make sure you start the server on localhost and 8888
    @org.junit.Before
    public void setUp() throws Exception {
        // Establish connection to server and create in/out streams
        sock = new Socket("localhost", 8888); // connect to host and socket

        // get output channel
        out = sock.getOutputStream();

        // create an object output writer (Java only)
        os = new ObjectOutputStream(out);

        // setup input stream
        in = new DataInputStream(sock.getInputStream());
    }

    @org.junit.After
    public void close() throws Exception {
        if (out != null)  out.close();
        if (sock != null) sock.close();
    }

    @Test
    public void addRequest() throws IOException {
        // create a correct req for server
        JSONObject req = new JSONObject();
        req.put("type", "add");
        req.put("num1", "1");
        req.put("num2", "2");

        // write the whole message
        os.writeObject(req.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();

        String i = (String) in.readUTF();
        // assuming I get correct JSON back
        JSONObject res = new JSONObject(i);

        // test response
        assertTrue(res.getBoolean("ok"));
        assertEquals("add", res.getString("type"));
        assertEquals(3, res.getInt("result"));

        // Wrong request to server num2 missing
        JSONObject req2 = new JSONObject();
        req2.put("type", "add");
        req2.put("num1", "1");
        // write the whole message
        os.writeObject(req2.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();

        i = (String) in.readUTF();
        // assuming I get correct JSON back
        res = new JSONObject(i);

        System.out.println(res);

        // test response
        assertFalse(res.getBoolean("ok"));
        assertEquals("Field num2 does not exist in request", res.getString("message"));

        // Wrong request to server num1 missing
        JSONObject req3 = new JSONObject();
        req3.put("type", "add");
        req3.put("num2", "1");
        // write the whole message
        os.writeObject(req3.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();

        i = (String) in.readUTF();
        // assuming I get correct JSON back
        res = new JSONObject(i);

        System.out.println(res);

        // test response
        assertFalse(res.getBoolean("ok"));
        assertEquals("Field num1 does not exist in request", res.getString("message"));

        // Wrong request to server num1 num2 missing
        JSONObject req4 = new JSONObject();
        req4.put("type", "add");
        // write the whole message
        os.writeObject(req4.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();

        i = (String) in.readUTF();
        // assuming I get correct JSON back
        res = new JSONObject(i);

        System.out.println(res);

        // test response
        assertFalse(res.getBoolean("ok"));
        assertEquals("Field num1 does not exist in request", res.getString("message"));

        // Wrong request to server num2 missing
        JSONObject req5 = new JSONObject();
        req5.put("type", "add");
        req5.put("num1", "hello");
        req5.put("num2", "2");
        // write the whole message
        os.writeObject(req5.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();

        i = (String) in.readUTF();
        // assuming I get correct JSON back
        res = new JSONObject(i);

        System.out.println(res);

        // test response
        assertFalse(res.getBoolean("ok"));
        assertEquals("Field num1/num2 needs to be of type: int", res.getString("message"));
    }

    @Test
    public void echoRequest() throws IOException {
        // valid request with data
        JSONObject req1 = new JSONObject();
        req1.put("type", "echo");
        req1.put("data", "gimme this back!");
        // write the whole message
        os.writeObject(req1.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();
        String i = (String) in.readUTF();
        // assuming I get correct JSON back
        JSONObject res = new JSONObject(i);
        // test response
        assertTrue(res.getBoolean("ok"));
        assertEquals("echo", res.getString("type"));
        assertEquals("Here is your echo: gimme this back!", res.getString("echo"));

        // Invalid request - no data sent
        JSONObject req2 = new JSONObject();
        req2.put("type", "echo");
        // write the whole message
        os.writeObject(req2.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();
        i = (String) in.readUTF();
        // assuming I get correct JSON back
        res = new JSONObject(i);
        System.out.println(res);
        // test response
        assertFalse(res.getBoolean("ok"));
        assertEquals("Field data does not exist in request", res.getString("message"));
    }

    @Test
    public void addManyRequest() throws IOException {
        // create a correct req for server
        JSONObject req = new JSONObject();
        req.put("type", "addmany");
        List<String> myList = Arrays.asList(
                "12",
                "15",
                "111",
                "42"
        );
        req.put("nums", myList);
        // write the whole message
        os.writeObject(req.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();
        String i = (String) in.readUTF();
        // assuming I get correct JSON back
        JSONObject res = new JSONObject(i);
        // test response
        assertTrue(res.getBoolean("ok"));
        assertEquals("addmany", res.getString("type"));
        assertEquals(180, res.getInt("result"));

        // Invalid request to server
        JSONObject req2 = new JSONObject();
        req2.put("type", "addmany");
        myList = Arrays.asList(
                "two",
                "15",
                "111",
                "42"
        );
        req2.put("nums", myList);
        // write the whole message
        os.writeObject(req2.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();
        i = (String) in.readUTF();
        // assuming I get correct JSON back
        res = new JSONObject(i);
        System.out.println(res);
        // test response
        assertFalse(res.getBoolean("ok"));
        assertEquals("Values in array need to be ints", res.getString("message"));


        // Invalid request to server with just one value in list
        JSONObject req3 = new JSONObject();
        req3.put("type", "addmany");
        myList = Arrays.asList(
                "42"
        );
        req2.put("nums", myList);
        // write the whole message
        os.writeObject(req2.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();
        i = (String) in.readUTF();
        // assuming I get correct JSON back
        res = new JSONObject(i);
        System.out.println(res);
        // test response
        assertFalse(res.getBoolean("ok"));
        assertEquals("Only one value given", res.getString("message"));

    }
    @Test
    public void addMovieRequest() throws IOException {
        // create a correct req for server
        JSONObject req = new JSONObject();
        req.put("type", "rating");
        req.put("task", "add");
        req.put("movie", "Michiko y Hatchin");
        req.put("rating", 5);
        req.put("username", "kariver4");

        // write the whole message
        os.writeObject(req.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();

        // read the response
        String i = (String) in.readUTF();
        // assuming I get correct JSON back
        JSONObject res = new JSONObject(i);

        // Log the response for debugging
        System.out.println("Response from server: " + res.toString());

        // test response
        assertTrue(res.getBoolean("ok"));
        assertEquals("rating", res.getString("type"));

        // Add assertions to check if the movie was added successfully
        assertTrue(res.has("movies"));
        JSONArray moviesArray = res.getJSONArray("movies");
        assertEquals(1, moviesArray.length());
        JSONObject movieJson = moviesArray.getJSONObject(0);
        assertEquals("Michiko y Hatchin", movieJson.getString("movie"));
        assertEquals(5, movieJson.getInt("rating"));
        assertTrue(movieJson.getJSONArray("raters").toString().contains("kariver4"));
    }

    @Test
    public void viewRatingRequest() throws IOException {
        // First, add a movie to the server
        JSONObject addMovieReq = new JSONObject();
        addMovieReq.put("type", "rating");
        addMovieReq.put("task", "add");
        addMovieReq.put("movie", "Attack on Titan");
        addMovieReq.put("rating", 5);
        addMovieReq.put("username", "kariver4");

        // Write the add movie request
        os.writeObject(addMovieReq.toString());
        os.flush();

        // Read the response
        String addMovieResponse = (String) in.readUTF();
        JSONObject addMovieRes = new JSONObject(addMovieResponse);

        // Check if the movie was added successfully
        assertTrue(addMovieRes.getBoolean("ok"));

        // Now, send a view rating request for the added movie
        JSONObject viewRatingReq = new JSONObject();
        viewRatingReq.put("type", "rating");
        viewRatingReq.put("task", "view");
        viewRatingReq.put("movie", "Attack on Titan");

        // Write the view rating request
        os.writeObject(viewRatingReq.toString());
        os.flush();

        // Read the response
        String viewRatingResponse = (String) in.readUTF();
        JSONObject viewRatingRes = new JSONObject(viewRatingResponse);

        // Log the response for debugging
        System.out.println("Response from server for viewRatingRequest: " + viewRatingRes.toString());

        // Test response for viewRatingRequest
        assertTrue(viewRatingRes.getBoolean("ok"));
        assertEquals("rating", viewRatingRes.getString("type"));

        // Verify that the correct movie information is returned
        assertTrue(viewRatingRes.has("movies"));
        JSONArray moviesArray = viewRatingRes.getJSONArray("movies");
        assertEquals(1, moviesArray.length());
        JSONObject movieJson = moviesArray.getJSONObject(0);
        assertEquals("Attack on Titan", movieJson.getString("movie"));
        assertEquals(5, movieJson.getInt("rating"));
        assertTrue(movieJson.getJSONArray("raters").toString().contains("kariver4"));
    }

    @Test
    public void addRatingRequest() throws IOException {
        // First, add a movie to the server
        JSONObject addMovieReq = new JSONObject();
        addMovieReq.put("type", "rating");
        addMovieReq.put("task", "add");
        addMovieReq.put("movie", "Gangsta");
        addMovieReq.put("rating", 5);
        addMovieReq.put("username", "kariver4");

        // Write the add movie request
        os.writeObject(addMovieReq.toString());
        os.flush();

        // Read the response
        String addMovieResponse = (String) in.readUTF();
        JSONObject addMovieRes = new JSONObject(addMovieResponse);

        // Check if the movie was added successfully
        assertTrue(addMovieRes.getBoolean("ok"));
        assertTrue(addMovieRes.has("movies"));
        JSONArray moviesArray = addMovieRes.getJSONArray("movies");
        assertEquals(1, moviesArray.length());
        JSONObject movieJson = moviesArray.getJSONObject(0);
        assertEquals("Gangsta", movieJson.getString("movie"));
        assertEquals(5, movieJson.getInt("rating"));
        assertTrue(movieJson.getJSONArray("raters").toString().contains("kariver4"));

        // Now, add a rating for the movie
        JSONObject addRatingReq = new JSONObject();
        addRatingReq.put("type", "rating");
        addRatingReq.put("task", "rate");
        addRatingReq.put("movie", "Gangsta");
        addRatingReq.put("rating", 4);
        addRatingReq.put("username", "wona");

        // Write the add rating request
        os.writeObject(addRatingReq.toString());
        os.flush();

        // Read the response
        String addRatingResponse = (String) in.readUTF();
        JSONObject addRatingRes = new JSONObject(addRatingResponse);

        // Log the response for debugging
        System.out.println("Response from server for addRatingRequest: " + addRatingRes.toString());

        // Test response for addRatingRequest
        assertTrue(addRatingRes.getBoolean("ok"));
        assertEquals("rating", addRatingRes.getString("type"));

        // Verify that the movie's rating was updated
        assertTrue(addRatingRes.has("movies"));
        JSONArray updatedMoviesArray = addRatingRes.getJSONArray("movies");
        assertEquals(1, updatedMoviesArray.length());
        JSONObject updatedMovieJson = updatedMoviesArray.getJSONObject(0);
        assertEquals("Gangsta", updatedMovieJson.getString("movie"));
        assertEquals(4, updatedMovieJson.getInt("rating"));
        assertTrue(updatedMovieJson.getJSONArray("raters").toString().contains("wona"));
    }



    @Test
    public void notJSON() throws IOException {
        // create a correct req for server
        os.writeObject("a");

        String i = (String) in.readUTF();
        // assuming I get correct JSON back
        JSONObject res = new JSONObject(i);

        // test response
        assertFalse(res.getBoolean("ok"));
        assertEquals("req not JSON", res.getString("message"));

        // calling the other test to make sure server continues to work and the "continue" does what it is supposed to do
        addRequest();
    }

}


