package edu.brown.cs32.student.server.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class Data {

  public record FilterRequest(Double minLat, Double maxLat, Double minLon, Double maxLon) {
    public FilterRequest {
      // default max number
      if (minLat == null) minLat = -90.0;
      if (maxLat == null) maxLat = 90.0;
      if (minLon == null) minLon = -180.0;
      if (maxLon == null) maxLon = 180.0;

      if (minLat < -90 || minLat > 90) {
        throw new IllegalArgumentException("get minLat=" + minLat + ", check the range");
      }
      if (maxLat < -90 || maxLat > 90) {
        throw new IllegalArgumentException("get maxLat=" + maxLat + ", check the range");
      }
      if (minLon < -180 || minLon > 180) {
        throw new IllegalArgumentException("get minLon=" + minLon + ", check the range");
      }
      if (maxLon < -180 || maxLon > 180) {
        throw new IllegalArgumentException("get maxLon=" + maxLon + ", check the range");
      }

      // check minLat <= maxLat and minLon <= maxLon
      if (minLat > maxLat) {
        throw new IllegalArgumentException(
            "get minLat=" + minLat + ", maxLat=" + maxLat + ",check the range");
      }
      if (minLon > maxLon) {
        throw new IllegalArgumentException(
            "get minLon=" + minLon + ", maxLon=" + maxLon + ", check the range");
      }

      // truncate the rest
      DecimalFormat df = new DecimalFormat();
      df.setMaximumFractionDigits(3);
      df.setRoundingMode(RoundingMode.FLOOR);
      // get the minimum.
      minLat = Double.parseDouble(df.format(minLat));
      minLon = Double.parseDouble(df.format(minLon));
      // add 0.001 to get the maximum.
      if (maxLat != 90) maxLat = Double.parseDouble(df.format(maxLat)) + 0.001;
      if (maxLon != 180) maxLon = Double.parseDouble(df.format(maxLon)) + 0.001;
    }
  }

  public record FilterResponse(String result, GeoData data) {}

  public record GeoData(String type, List<GeoFeature> features) {}

  public record GeoFeature(String type, GeoGeometry geometry, Map<String, Object> properties) {}

  public record GeoGeometry(String type, List<List<List<List<Double>>>> coordinates) {}

  public record Note(String title, String note, Double latitude, Double longitude) {}

  public record NoteResponse(String result, List<Note> data) {}
}
