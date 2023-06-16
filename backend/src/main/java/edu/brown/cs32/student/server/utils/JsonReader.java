package edu.brown.cs32.student.server.utils;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class JsonReader {
  // input: json string
  // output: hash map
  public static Map<String, Object> readFromString(String jsonString) throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
    Map<String, Object> ret = (Map<String, Object>) moshi.adapter(type).fromJson(jsonString);
    return ret;
  }

  public static Map<String, Object> readFromFile(String path) throws IOException {
    String jsonString = new String(Files.readAllBytes(Paths.get(path)));
    return readFromString(jsonString);
  }

  // read from a file to geodata
  public static Data.GeoData readFromFileToGeo(String path) throws IOException {
    String jsonString = new String(Files.readAllBytes(Paths.get(path)));
    Moshi moshi = new Moshi.Builder().build();
    Data.GeoData data = moshi.adapter(Data.GeoData.class).fromJson(jsonString);
    return data;
  }

  // json string to note
  public static Data.Note readNoteFromString(String jsonString) throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    Data.Note data = moshi.adapter(Data.Note.class).fromJson(jsonString);
    return data;
  }

  // json string into FilterRequest
  public static Data.FilterRequest readFilterFromString(String jsonString) throws Throwable {
    Moshi moshi = new Moshi.Builder().build();
    try {
      return moshi.adapter(Data.FilterRequest.class).fromJson(jsonString);
    } catch (AssertionError ae) {
      throw ae.getCause();
    }
  }
}
