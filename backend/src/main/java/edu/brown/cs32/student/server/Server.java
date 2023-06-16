package edu.brown.cs32.student.server;

import static spark.Spark.after;

import edu.brown.cs32.student.server.filter.FilterHandler;
import edu.brown.cs32.student.server.note.NoteHandler;
import spark.Spark;

public class Server {
  public static void main(String[] args) {
    Spark.port(3232);
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    // Setting up the handler for the GET /order endpoint
    Spark.get("filter", new FilterHandler());
    Spark.get("note", new NoteHandler());
    Spark.init();
    Spark.awaitInitialization();
    System.out.println("Server started on localhost:3232.");
  }
}
