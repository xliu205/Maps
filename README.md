# Documentation
## Project Details
- **Project Name**:
    - Map
- **Team members and contributions**:
  - *xliu205*:
    - user story 2
    - part of user story 3
    - part of user story 5
    - testing
  - *wfu16*:
    - part of user story 3
    - part of user story 5
    - documentation
- **Total estimated time**:
    - 20 hours
- A link to [our repo](https://github.com/cs0320-s2023/sprint-5-wfu16-xliu205)


## Relationship between classes and interfaces
- FilterRequestConverter and Cached FilterRequestConverter implement the interface GeoFilter
- isGeoJsonSuccessResponse, isGeoJsonErrorResponse, isNoteSuccessResponse, isNoteErrorResponse check if any class belong to the interfaces GeoJsonSuccessResponse, GeoJsonErrorResponse, NoteSuccessResponse, and NoteErrorResponse, respectively

## Errors/Bugs
- We currently have no remaining errors. 

## Tests
- **Unit Testing**
  - *FilterRequestConverterTest*: make sures that the FilterRequestConverter successfully filters out the right number of areas inside the region bounded by given minLat, maxLat, minLon, maxLon
  - *JsonReaderTest*: mocked tests
    - TestReadNested: checks that the different fields from a json string are successfully parsed
    - TestReadNonExist: checks when null is returned when a specific field is absent from the json string
    - TestReadFromFile: checks that the correct number of elements are returned by reading through file path
        

## How To
- **Run the Tests**
    - Run "npm test" in the terminal
- **Build our program**
    - start from react-map-gl, make sure that the map can successfully be shown in the localhost
    - be able to extract the latitude and longitude information from json file
    - create different colors for overlaying redlining data
    - backend: filter the json data within the latitude and longitude boundary box, and create caching
    - frontend: build inputbox and messagebox to allow user input and popout output information
    - allow stakeholder homeowner to add annotations
- **Run our program**
    - run "npm start" to start the server
    - run "npm install" and then "npm run dev" to run the map localhost
    - in the input boxes of the first row, the user can input maximum latitude, minimum latitude, maximum longitude, and minimum longitude to set the bounding box
    - when the user clicks on an area on the map, the latitude, longitude, state, city, and name of the region will be shown in the message box
    - the homeowner stakeholder will be able to annotate different areas of the map

## Reflection
- we rely on mostly xliu205's labor when running the demo
- *packages and tools we used*:
  - 1. useState and useEffect hooks from react
  - 2. react-map-gl library
  - 3. mapbox-gl/dist/mapbox-gl.css
  - 4. Map and HashMap from Java.util
  - 5. ArrayList and List from Java.util
  - 6. CacheBuilder, CacheLoader, and LoadingCache from google
  - 7. Moshi
  - 8. Request, Response, and Route from Spark for server
  - 9. TimeUnit package in Java, used for caching 
  - 10. RoundingMode and DecimalFormat packages in Java, used to check if latitudes and longitudes are valid
  - 11. Operating system macOs, or Linux, or Windows Server, to run the server
  - 12. IntelliJ, Java Development Kit, and Git, which we use to build and run java backend and manipulate version control
