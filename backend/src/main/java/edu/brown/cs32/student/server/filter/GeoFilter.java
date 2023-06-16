package edu.brown.cs32.student.server.filter;

import edu.brown.cs32.student.server.utils.Data;

public interface GeoFilter {
  Data.GeoData convertFilterRequest(Data.FilterRequest request);
}
