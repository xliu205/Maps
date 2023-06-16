package edu.brown.cs32.student.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.Moshi;
import edu.brown.cs32.student.server.filter.FilterHandler;
import edu.brown.cs32.student.server.note.NoteHandler;
import edu.brown.cs32.student.server.utils.Data;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import okio.BufferedSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testng.annotations.BeforeClass;
import spark.Spark;

public class IntegrationTest {

  private final int TESTTIME = 1000;
  private final Random random = new Random();

  @BeforeClass
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  @BeforeEach
  void setUp() {
    Spark.get("/filter", new FilterHandler());
    Spark.get("/note", new NoteHandler());
    Spark.init();
    Spark.awaitInitialization(); // don't continue until the server is listening
  }

  @AfterEach
  void tearDown() {
    Spark.unmap("/filter");
    Spark.unmap("/note");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  Data.GeoData deserializeFilterResponse(BufferedSource buffer) throws Exception {
    Moshi moshi = new Moshi.Builder().build();
    Data.FilterResponse response = moshi.adapter(Data.FilterResponse.class).fromJson(buffer);
    return response.data();
  }

  List<Data.Note> deserializeNoteResponse(BufferedSource buffer) throws Exception {
    Moshi moshi = new Moshi.Builder().build();
    Data.NoteResponse response = moshi.adapter(Data.NoteResponse.class).fromJson(buffer);
    return response.data();
  }

  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    clientConnection.connect();
    return clientConnection;
  }

  /** test Success filter */
  @Test
  void testFilterSuccess() throws Exception {
    HttpURLConnection clientConnection =
        tryRequest(
            "filter?filter={\"minLat\":\"40\",\"maxLat\":\"42\",\"minLon\":\"-72\",\"maxLon\":\"-70\"}");
    assertEquals(200, clientConnection.getResponseCode());

    Data.GeoData filtered =
        deserializeFilterResponse(new Buffer().readFrom(clientConnection.getInputStream()));

    for (Data.GeoFeature feature : filtered.features()) {
      for (List<Double> coordinate : feature.geometry().coordinates().get(0).get(0)) {
        double lon = coordinate.get(0);
        double lat = coordinate.get(1);
        assertTrue(40 <= lat && lat <= 42 && -72 <= lon && lon <= -70);
      }
    }
    assertEquals(44, filtered.features().size());
    clientConnection.disconnect();
  }

  /** test Failed (invalid provided params) */
  @Test
  void testFilterFailure2() throws Exception {
    HttpURLConnection clientConnection =
        tryRequest(
            "filter?filter={\"minLat\":\"40\",\"maxLat\":\"39\",\"minLon\":\"-72\",\"maxLon\":\"-70\"}");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    GeneralResponse response =
        new GeneralResponse().deserialize(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals("error_bad_request", response.responseMap().get("result"));
    clientConnection.disconnect();
  }

  /** test Success Note */
  @Test
  void testNoteSuccess() throws Exception {
    HttpURLConnection clientConnection =
        tryRequest(
            "note?note={\"title\":\"city\",\"note\":\"nothing\",\"latitude\":\"41.0\",\"longitude\":\"-71.0\"}");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    List<Data.Note> data =
        deserializeNoteResponse(new Buffer().readFrom(clientConnection.getInputStream()));

    List<Data.Note> expectedNotes = new ArrayList<>();
    expectedNotes.add(
        new Data.Note(
            "New York",
            "8,175,133 people, big city, best choice, welcome all!",
            40.6643,
            -73.9385));
    expectedNotes.add(new Data.Note("Boston", "0 people, highest rent ever", 42.3611, -71.0570));
    expectedNotes.add(
        new Data.Note(
            "Los Angeles",
            "3,792,621 people, big city, best choice, welcome all!",
            34.0194,
            -118.4108));
    expectedNotes.add(new Data.Note("Providence", "Brown University", 41.8393, -71.4162));
    expectedNotes.add(new Data.Note("city", "nothing", 41.0, -71.0));
    assertEquals(expectedNotes, data);
    clientConnection.disconnect();
  }

  /**
   * Fuzz filter test (random constraints)
   *
   * @throws Exception
   */
  @Test
  void testFilterFuzzTest1() throws Exception {
    for (int i = 0; i < TESTTIME; i++) {
      double minLat = generateRandomDouble(-1000, 1000);
      double maxLat = generateRandomDouble(-1000, 1000);
      double minLon = generateRandomDouble(-1000, 1000);
      double maxLon = generateRandomDouble(-1000, 1000);
      String apiCall =
          "filter?filter={\"minLat\":\""
              + minLat
              + "\",\"maxLat\":\""
              + maxLat
              + "\",\"minLon\":\""
              + minLon
              + "\",\"maxLon\":\""
              + maxLon
              + "\"}";
      HttpURLConnection clientConnection = tryRequest(apiCall);
      assertEquals(200, clientConnection.getResponseCode());
      clientConnection.disconnect();
    }
  }

  /**
   * Fuzz filter test (random apicall)
   *
   * @throws Exception
   */
  @Test
  void testFilterFuzzTest2() throws Exception {
    for (int i = 0; i < TESTTIME; i++) {
      String apiCall = "filter?" + generateRandomString(100);
      HttpURLConnection clientConnection = tryRequest(apiCall);
      assertEquals(200, clientConnection.getResponseCode());
      clientConnection.disconnect();
    }
  }

  public String generateRandomString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      char c = (char) (random.nextInt(95) + 32);
      if (c == ' ') continue;
      if (c == '%') continue;
      sb.append(c);
    }
    return sb.toString();
  }

  private double generateRandomDouble(int start, int end) {
    double random = new Random().nextDouble();
    double result = start + (random * (end - start));
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(3);
    df.setRoundingMode(RoundingMode.FLOOR);
    return Double.parseDouble(df.format(result));
  }
}
//
//  /*
//   * load csv successful
//   * */
//  @Test
//  void testCanLoadCSV() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    assertEquals("success", response.responseMap().get("result"));
//    assertEquals("Successfully loaded file: ten-star.csv", response.responseMap().get("detail"));
//    clientConnection.disconnect();
//  }
//
//  /*
//   * load csv unsuccessful
//   * */
//  @Test
//  void testCanNotLoadCSV() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=random.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    assertEquals("error_datasource", response.responseMap().get("result"));
//    assertEquals("Fail to load file: random.csv", response.responseMap().get("detail"));
//    clientConnection.disconnect();
//  }
//
//  /*
//   * load csv without specifying header
//   * */
//  @Test
//  void testLoadCSVMissingArgument() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    assertEquals("error_bad_request", response.responseMap().get("result"));
//    assertEquals("Missing Args: [header]", response.responseMap().get("detail"));
//    clientConnection.disconnect();
//  }
//
//  /*
//   * load csv without wrong header argument
//   * */
//  @Test
//  void testLoadCSVWrongHeader() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=1");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    assertEquals("error_bad_request", response.responseMap().get("result"));
//    assertEquals(
//        "header should be either true or false, but get 1", response.responseMap().get("detail"));
//    clientConnection.disconnect();
//  }
//  /*
//   * load two csv file and view
//   * */
//  @Test
//  void testLoadAndViewTwoCSV() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("viewcsv");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("loadcsv?filepath=test.csv&header=false");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("viewcsv");
//    assertEquals(200, clientConnection.getResponseCode());
//    List<List<String>> expected = new ArrayList<>();
//    expected.add(List.of(new String[] {"John Doe", "20", "Male", "Computer Science", "3.5"}));
//    expected.add(List.of(new String[] {"Jane Smith", "", "Female", "Business", "3.2"}));
//    expected.add(
//        List.of(new String[] {"David Lee", "21", "Male", "Mechanical Engineering", "3.9"}));
//    expected.add(List.of(new String[] {"Amy Chen", "18", "Female", "Biology", "3.7"}));
//    expected.add(List.of(new String[] {"Michael Johnson", "22", "Male", "History", "3.1"}));
//    expected.add(List.of(new String[] {"Emily Brown", "20", "Female", "Psychology", "3.8"}));
//    expected.add(List.of(new String[] {"Grace Lee", "21", "Female", "Chemistry", "3.6"}));
//    expected.add(List.of(new String[] {"Daniel Park", "18", "Male", "English", "3.3"}));
//    expected.add(List.of(new String[] {"Sophia Lee", "20", "Female", "Mathematics", "3.8"}));
//
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put("header", false);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /*
//   * successfully viewed csv with header
//   * */
//  @Test
//  void testViewCSVwithHeader() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("viewcsv");
//    assertEquals(200, clientConnection.getResponseCode());
//
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    List<List<String>> expected = new ArrayList<>();
//    expected.add(List.of(new String[] {"StarID", "ProperName", "X", "Y", "Z"}));
//    expected.add(List.of(new String[] {"0", "Sol", "0", "0", "0"}));
//    expected.add(List.of(new String[] {"1", "", "282.43485", "0.00449", "5.36884"}));
//    expected.add(List.of(new String[] {"2", "", "43.04329", "0.00285", "-15.24144"}));
//    expected.add(List.of(new String[] {"3", "", "277.11358", "0.02422", "223.27753"}));
//    expected.add(List.of(new String[] {"3759", "96 G. Psc", "7.26388", "1.55643", "0.68697"}));
//    expected.add(
//        List.of(new String[] {"70667", "Proxima Centauri", "-0.47175", "-0.36132", "-1.15037"}));
//    expected.add(
//        List.of(new String[] {"71454", "Rigel Kentaurus B", "-0.50359", "-0.42128", "-1.1767"}));
//    expected.add(
//        List.of(new String[] {"71457", "Rigel Kentaurus A", "-0.50362", "-0.42139", "-1.17665"}));
//    expected.add(
//        List.of(new String[] {"87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"}));
//    expected.add(List.of(new String[] {"118721", "", "-2.28262", "0.64697", "0.29354"}));
//
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put("header", true);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /*
//   * successfully viewed csv without header
//   * */
//  @Test
//  void testViewNoHeaderCSV() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=test.csv&header=false");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("viewcsv");
//    assertEquals(200, clientConnection.getResponseCode());
//
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    List<List<String>> expected = new ArrayList<>();
//    expected.add(List.of(new String[] {"John Doe", "20", "Male", "Computer Science", "3.5"}));
//    expected.add(List.of(new String[] {"Jane Smith", "", "Female", "Business", "3.2"}));
//    expected.add(
//        List.of(new String[] {"David Lee", "21", "Male", "Mechanical Engineering", "3.9"}));
//    expected.add(List.of(new String[] {"Amy Chen", "18", "Female", "Biology", "3.7"}));
//    expected.add(List.of(new String[] {"Michael Johnson", "22", "Male", "History", "3.1"}));
//    expected.add(List.of(new String[] {"Emily Brown", "20", "Female", "Psychology", "3.8"}));
//    expected.add(List.of(new String[] {"Grace Lee", "21", "Female", "Chemistry", "3.6"}));
//    expected.add(List.of(new String[] {"Daniel Park", "18", "Male", "English", "3.3"}));
//    expected.add(List.of(new String[] {"Sophia Lee", "20", "Female", "Mathematics", "3.8"}));
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put("header", false);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /*
//   * view csv unsuccessful
//   * */
//  @Test
//  void testCanNotViewCSV() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("viewcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    assertEquals("error_bad_request", response.responseMap().get("result"));
//    assertEquals("No CSV data loaded", response.responseMap().get("detail"));
//    clientConnection.disconnect();
//  }
//
//  /*
//   * no csv data loaded before search
//   * */
//  @Test
//  void testCSVNotFound() throws Exception {
//    HttpURLConnection clientConnection =
//        tryRequest("searchcsv?query=or(Barnard%27s%20Star,and(0;3;idx,not(5.36884;Z;name)))");
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    ExpecteResponseMap.put("result", "error_bad_request");
//    ExpecteResponseMap.put("detail", "No CSV data loaded");
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /*
//   * no query entered when search
//   * */
//  @Test
//  void testNoQueryFound() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?bdnc");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    ExpecteResponseMap.put("result", "error_bad_request");
//    ExpecteResponseMap.put("detail", "Need query field to search.");
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /*
//   * search CSV data with header using basic query without identifier successfully, display result
//   * */
//  @Test
//  void testSearchBasicQuery() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=-0.01729");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "-0.01729");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    expected.add(
//        List.of(new String[] {"87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"}));
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put(
//        "headerData", List.of(new String[] {"StarID", "ProperName", "X", "Y", "Z"}));
//    ExpecteResponseMap.put("header", true);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /*
//   * search CSV data without header using basic query without identifier successfully, display
// result
//   * */
//  @Test
//  void testSearchBasicQueryNoHeader() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=test.csv&header=false");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=3.8");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "3.8");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    expected.add(List.of(new String[] {"Emily Brown", "20", "Female", "Psychology", "3.8"}));
//    expected.add(List.of(new String[] {"Sophia Lee", "20", "Female", "Mathematics", "3.8"}));
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put("headerData", List.of(new String[] {}));
//    ExpecteResponseMap.put("header", false);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /*
//   * Search CSV data with header using basic query with name identifier, and the result is
//   * not empty
//   * */
//  @Test
//  void testSearchWithHeaderIdentifier() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=-0.01729;X;name");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "-0.01729;X;name");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    expected.add(
//        List.of(new String[] {"87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"}));
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put(
//        "headerData", List.of(new String[] {"StarID", "ProperName", "X", "Y", "Z"}));
//    ExpecteResponseMap.put("header", true);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /*
//   * Search CSV data with header using basic query with index identifier, and the result is
//   * empty
//   */
//  @Test
//  void testSearchWithHeaderIdentifierNoResult() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=-0.01729;Y;name");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "-0.01729;Y;name");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put(
//        "headerData", List.of(new String[] {"StarID", "ProperName", "X", "Y", "Z"}));
//    ExpecteResponseMap.put("header", true);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /*
//   * Search CSV data without header using basic query with index identifier, and the result
//   * is not empty
//   */
//  @Test
//  void testSearchWOHeaderIdentifierIDX() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=test.csv&header=false");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=3.8;4;idx");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "3.8;4;idx");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    expected.add(List.of(new String[] {"Emily Brown", "20", "Female", "Psychology", "3.8"}));
//    expected.add(List.of(new String[] {"Sophia Lee", "20", "Female", "Mathematics", "3.8"}));
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put("headerData", List.of(new String[] {}));
//    ExpecteResponseMap.put("header", false);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /* Search CSV data with header using basic query with index identifier, and the result is
//  empty
//  */
//  @Test
//  void testSearchWithHeaderIdentifierIDXNoResult() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=-0.01729;3;idx");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "-0.01729;3;idx");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put(
//        "headerData", List.of(new String[] {"StarID", "ProperName", "X", "Y", "Z"}));
//    ExpecteResponseMap.put("header", true);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /* Search CSV data without header using basic query with index identifier, and the result is
//    empty
//  */
//  @Test
//  void testSearchWithNOHeaderIdentifierIDXNoResult() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=test.csv&header=false");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=3.8;1;idx");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "3.8;1;idx");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put("headerData", List.of(new String[] {}));
//    ExpecteResponseMap.put("header", false);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /* Search CSV data with header using basic query with index identifier,
//   and the result is empty
//  */
//  @Test
//  void testSearchNoTargetExist() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=-0.12345;1;idx");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "-0.12345;1;idx");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put(
//        "headerData", List.of(new String[] {"StarID", "ProperName", "X", "Y", "Z"}));
//    ExpecteResponseMap.put("header", true);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /*Test Wrong format of query
//   * */
//  @Test
//  void testWrongQuery() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=-0.12345;1");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "-0.12345;1");
//    ExpecteResponseMap.put("result", "error_bad_request");
//    ExpecteResponseMap.put("request", query);
//    ExpecteResponseMap.put("detail", "Wrong query format! Received 2 args, but should be 1 or 3");
//
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /* test not query*/
//  @Test
//  void testNotQuery() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=not(-0.01729;2;idx)");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "not(-0.01729;2;idx)");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put(
//        "headerData", List.of(new String[] {"StarID", "ProperName", "X", "Y", "Z"}));
//    ExpecteResponseMap.put("header", true);
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    expected.add(List.of(new String[] {"0", "Sol", "0", "0", "0"}));
//    expected.add(List.of(new String[] {"1", "", "282.43485", "0.00449", "5.36884"}));
//    expected.add(List.of(new String[] {"2", "", "43.04329", "0.00285", "-15.24144"}));
//    expected.add(List.of(new String[] {"3", "", "277.11358", "0.02422", "223.27753"}));
//    expected.add(List.of(new String[] {"3759", "96 G. Psc", "7.26388", "1.55643", "0.68697"}));
//    expected.add(
//        List.of(new String[] {"70667", "Proxima Centauri", "-0.47175", "-0.36132", "-1.15037"}));
//    expected.add(
//        List.of(new String[] {"71454", "Rigel Kentaurus B", "-0.50359", "-0.42128", "-1.1767"}));
//    expected.add(
//        List.of(new String[] {"71457", "Rigel Kentaurus A", "-0.50362", "-0.42139", "-1.17665"}));
//    expected.add(List.of(new String[] {"118721", "", "-2.28262", "0.64697", "0.29354"}));
//    ExpecteResponseMap.put("detail", expected);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//
//  /* test or query*/
//  @Test
//  void testOrQuery() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=or(-0.01729;2;idx,Proxima%20Centauri)");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "or(-0.01729;2;idx,Proxima Centauri)");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    expected.add(
//        List.of(new String[] {"70667", "Proxima Centauri", "-0.47175", "-0.36132", "-1.15037"}));
//    expected.add(
//        List.of(new String[] {"87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"}));
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put(
//        "headerData", List.of(new String[] {"StarID", "ProperName", "X", "Y", "Z"}));
//    ExpecteResponseMap.put("header", true);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
//  /* test and query*/
//  @Test
//  void testAndQuery() throws Exception {
//    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=ten-star.csv&header=true");
//    assertEquals(200, clientConnection.getResponseCode());
//    clientConnection = tryRequest("searchcsv?query=and(-0.01729;2;idx,Barnard's%20Star)");
//    assertEquals(200, clientConnection.getResponseCode());
//    GeneralResponse response =
//        new GeneralResponse().deserialize(new
// Buffer().readFrom(clientConnection.getInputStream()));
//    Map<String, Object> ExpecteResponseMap = new HashMap<>();
//    Map<String, Object> query = new HashMap<>();
//    query.put("query", "and(-0.01729;2;idx,Barnard's Star)");
//    ExpecteResponseMap.put("result", "success");
//    ExpecteResponseMap.put("request", query);
//    List<List<String>> expected = new ArrayList<>();
//    expected.add(
//        List.of(new String[] {"87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"}));
//    ExpecteResponseMap.put("detail", expected);
//    ExpecteResponseMap.put(
//        "headerData", List.of(new String[] {"StarID", "ProperName", "X", "Y", "Z"}));
//    ExpecteResponseMap.put("header", true);
//    assertEquals(response.responseMap(), ExpecteResponseMap);
//    clientConnection.disconnect();
//  }
// }
