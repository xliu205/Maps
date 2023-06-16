package edu.brown.cs32.student.server.filter;

import edu.brown.cs32.student.server.GeneralResponse;
import edu.brown.cs32.student.server.MissingArgException;
import edu.brown.cs32.student.server.utils.Data;
import edu.brown.cs32.student.server.utils.Data.FilterRequest;
import edu.brown.cs32.student.server.utils.JsonReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.StringUtils;

/**
 * Handler class for the filtering redlining map data.
 *
 * <p>This endpoint is similar to the endpoint(s) you'll need to create for Sprint 2. It takes a
 * basic GET request with no Json body, and returns a Json object in reply. The responses are more
 * complex, but this should serve as a reference.
 */
public class FilterHandler implements Route {
  private Data.GeoData geoData;

  private final String PATH = "data/fullDownload.json";
  private GeoFilter converter;

  public FilterHandler() {
    try {
      this.geoData = JsonReader.readFromFileToGeo(PATH);
      this.converter =
          new CachedFilterRequestConverter(new FilterRequestConverter(geoData), 10, 60);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * filter
   *
   * @param request the request to handle
   * @param response use to modify properties of the response
   * @return response content
   * @throws Exception This is part of the interface; we don't have to throw anything.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    String filter = request.queryParams("filter");
    Map<String, Object> result = new HashMap<>();
    try {

      // check if params are given
      if (StringUtils.isBlank(filter)) throw new MissingArgException(List.of("filter"));
      FilterRequest filterRequest = JsonReader.readFilterFromString(filter);

      Data.GeoData filteredResult = converter.convertFilterRequest(filterRequest);
      result.put("result", "success");
      result.put("data", filteredResult);
      return new GeneralResponse(result).serialize();
    } catch (MissingArgException e) {
      result.put("result", "error_bad_request");
      result.put("data", e.getMessage());
    } catch (IllegalArgumentException e) {
      result.put("result", "error_bad_request");
      result.put("data", e.getMessage());
    } catch (Throwable e) {
      result.put("result", "error_bad_request");
      result.put("data", e.getMessage());
    }
    return new GeneralResponse(result).serialize();
  }
}

