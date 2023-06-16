package edu.brown.cs32.student.server;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.util.Map;
import okio.BufferedSource;

public record GeneralResponse(Map<String, Object> responseMap) {

  public GeneralResponse() {
    this(null);
  }
  /**
   * @return this response, serialized as Json
   */
  public String serialize() throws Exception {
    try {
      Moshi moshi = new Moshi.Builder().build();
      Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
      return moshi.adapter(type).toJson(responseMap);
    } catch (Exception e) {

      e.printStackTrace();
      throw e;
    }
  }

  /**
   * deserialize buffered json string to GeneralResponse class
   *
   * @param buffer
   * @return
   * @throws Exception
   */
  public GeneralResponse deserialize(BufferedSource buffer) throws Exception {
    try {
      Moshi moshi = new Moshi.Builder().build();
      Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
      Map<String, Object> response = (Map<String, Object>) moshi.adapter(type).fromJson(buffer);
      return new GeneralResponse(response);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}
