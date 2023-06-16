import {
  isNoteSuccessResponse,
  isNoteErrorResponse,
} from "./ResponseInterface";

export interface Note {
  title: string;
  note: string;
  latitude: number;
  longitude: number;
}


export function isNote(json: any): json is Note {
  return (
    "title" in json &&
    "note" in json &&
    "latitude" in json &&
    "longitude" in json
  );
}

async function fetchData(url: string): Promise<[Note] | string> {
  const response = await fetch(url);
  if (response.ok) {
    const json = await response.json();
    if (!isNoteSuccessResponse(json) && !isNoteErrorResponse(json)) {
      return "Error: server not response correctly.";
    }
    return json.data;
  } else {
    return "Error: server not response.";
  }
}


export function getNotes() {
  const url = "http://localhost:3232/note";
  return fetchData(url);
}


export function saveNotes(note: Note) {
  const url = "http://localhost:3232/note?note=" + JSON.stringify(note);
  return fetchData(url);
}
