package edu.brown.cs32.student.server;

import java.util.List;

public class MissingArgException extends Exception {

  public MissingArgException(List<String> Args) {
    super("Missing Args: " + Args.toString());
  }
}
