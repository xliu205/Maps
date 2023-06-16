package edu.brown.cs32.student.server.note;

import edu.brown.cs32.student.server.GeneralResponse;
import edu.brown.cs32.student.server.utils.Data.Note;
import edu.brown.cs32.student.server.utils.JsonReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.StringUtils;

/** handle note api call */
public class NoteHandler implements Route {
  private List<Note> notes;

  public NoteHandler() {
    notes = new ArrayList<>();
    notes.add(
        new Note(
            "New York",
            "8,175,133 people, big city, best choice, welcome all!",
            40.6643,
            -73.9385));
    notes.add(new Note("Boston", "0 people, highest rent ever", 42.3611, -71.0570));
    notes.add(
        new Note(
            "Los Angeles",
            "3,792,621 people, big city, best choice, welcome all!",
            34.0194,
            -118.4108));
    notes.add(new Note("Providence", "Brown University", 41.8393, -71.4162));
  }

  /**
   * Handle the notes for the map, if param has, add to notes, or return the original one.
   *
   * @param request request
   * @param response response
   * @return map of result
   * @throws Exception moshi exception
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    Map<String, Object> result = new HashMap<>();
    String note = request.queryParams("note");
    try {
      if (!StringUtils.isBlank(note)) {
        notes.add(JsonReader.readNoteFromString(note));
      }
      result.put("result", "success");
      result.put("data", notes);
    } catch (Exception e) {
      e.printStackTrace();
      result.put("result", "error_bad_request");
      result.put("data", e.getMessage());
    }
    return new GeneralResponse(result).serialize();
  }
}
