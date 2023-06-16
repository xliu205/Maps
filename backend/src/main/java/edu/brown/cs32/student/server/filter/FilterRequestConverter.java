package edu.brown.cs32.student.server.filter;

import edu.brown.cs32.student.server.utils.Data;
import java.util.ArrayList;
import java.util.List;

public class FilterRequestConverter implements GeoFilter {
  private final Data.GeoData geoData;

  public FilterRequestConverter(Data.GeoData data) {
    this.geoData = data;
  }

  // get filer request transfer to geodata
  @Override
  public Data.GeoData convertFilterRequest(Data.FilterRequest request) {
    List<Data.GeoFeature> retFeatures = new ArrayList<>();
    Double minLat = request.minLat(),
        maxLat = request.maxLat(),
        minLon = request.minLon(),
        maxLon = request.maxLon();
    List<Data.GeoFeature> featureList = geoData.features();
    for (Data.GeoFeature feature : featureList) {
      Data.GeoGeometry geometry = feature.geometry();
      boolean isValid = true;
      if (geometry != null) {
        List<List<List<List<Double>>>> coordinates = geometry.coordinates();
        if (coordinates != null) {
          for (List<Double> coordinate : coordinates.get(0).get(0)) {
            double lon = coordinate.get(0);
            double lat = coordinate.get(1);
            if (lat < minLat || lat > maxLat || lon < minLon || lon > maxLon) {
              isValid = false;
              break;
            }
          }
        }
        if (isValid) {
          retFeatures.add(feature);
        }
      }
    }
    return new Data.GeoData("FeatureCollection", retFeatures);
  }
}
