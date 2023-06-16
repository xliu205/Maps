import React, { useState } from "react";
import { Note, saveNotes } from "../utils/note";
import { overlayData } from "../utils/overlays";

interface InputBoxProps {
  setOverlay: (o: GeoJSON.FeatureCollection) => any;
  setMessage: (o: string) => any;
  setNotes: (o: [Note]) => any;
}

function isValidLat(str: string): boolean {
  let num = parseFloat(str);
  return num >= -90 && num <= 90;
}

function isValidLon(str: string): boolean {
  let num = parseFloat(str);
  return num >= -180 && num <= 180;
}

function InputBox(props: InputBoxProps) {
  const [minLat, setminLat] = useState<string>("");
  const [maxLat, setmaxLat] = useState<string>("");
  const [minLon, setminLon] = useState<string>("");
  const [maxLon, setmaxLon] = useState<string>("");
  const [lat, setLat] = useState<string>("");
  const [lon, setLon] = useState<string>("");
  const [title, setTitle] = useState<string>("");
  const [note, setNote] = useState<string>("");


  function handleSubmit() {
    let filter: {
      minLat: string | undefined;
      maxLat: string | undefined;
      minLon: string | undefined;
      maxLon: string | undefined;
    } = {
      minLat: minLat,
      maxLat: maxLat,
      minLon: minLon,
      maxLon: maxLon,
    };
    if (!isValidLat(minLat)) {
      if (minLat.trim() !== "") {
        props.setMessage(`unvalid input`);
        return;
      }
      delete filter.minLat;
    }
    if (!isValidLat(maxLat)) {
      if (maxLat.trim() !== "") {
        props.setMessage(`unvalid input`);
        return;
      }
      delete filter.maxLat;
    }
    if (!isValidLon(minLon)) {
      if (minLon.trim() !== "") {
        props.setMessage(`unvalid input`);
        return;
      }
      delete filter.minLon;
    }
    if (!isValidLat(maxLon)) {
      if (maxLon.trim() !== "") {
        props.setMessage(`unvalid input`);
        return;
      }
      delete filter.maxLon;
    }

    overlayData(filter).then((r) => {
      if (typeof r === "string") {
        props.setMessage(r);
      } else {
        props.setOverlay(r);
      }
    });
  }

  function handleConfirm() {
    if (isValidLat(lat) && isValidLon(lon)) {
      saveNotes({
        title: title,
        note: note,
        latitude: Number(lat),
        longitude: Number(lon),
      }).then((r) => {
        if (typeof r === "string") {
          props.setMessage(r);
        } else {
          props.setNotes(r);
        }
      });
    } else {
      props.setMessage(`unvalid input`);
    }
  }

  function handleReset() {
    setminLat("");
    setmaxLat("");
    setminLon("");
    setmaxLon("");
  }

  return (
    <div className="inputBox">
      <div className="input-group mb-3 mt-3 ms-3">
        <span className="input-group-text">Min Latitude</span>
        <input
          aria-label={"Min Latitude input box"}
          placeholder=""
          type="number"
          min={-90}
          max={90}
          className="form-control"
          value={minLat}
          onChange={(e) => setminLat(e.target.value)}
          onKeyUp={(e) => {
            if (e.key == "Enter") handleSubmit();
          }}
        ></input>
        <span className="input-group-text">Max Latitude</span>
        <input
          aria-label={"Max Latitude input box"}
          placeholder=""
          type="number"
          min={-90}
          max={90}
          className="form-control"
          value={maxLat}
          onChange={(e) => setmaxLat(e.target.value)}
          onKeyUp={(e) => {
            if (e.key == "Enter") handleSubmit();
          }}
        ></input>

        <span className="input-group-text">Min Longitude</span>
        <input
          aria-label={"Min Longitude input box"}
          placeholder=""
          type="number"
          min={-180}
          max={180}
          className="form-control"
          value={minLon}
          onChange={(e) => setminLon(e.target.value)}
          onKeyUp={(e) => {
            if (e.key == "Enter") handleSubmit();
          }}
        ></input>
        <span className="input-group-text">Max Longitude</span>
        <input
          aria-label={"Max Longitude input box"}
          placeholder=""
          type="number"
          min={-180}
          max={180}
          className="form-control"
          value={maxLon}
          onChange={(e) => setmaxLon(e.target.value)}
          onKeyUp={(e) => {
            if (e.key == "Enter") handleSubmit();
          }}
        ></input>

        <button
          aria-label={"Reset button"}
          className="btn btn-secondary"
          onClick={() => handleReset()}
        >
          Reset
        </button>
        <button
          aria-label={"Submit button"}
          className="btn btn-primary"
          onClick={() => handleSubmit()}
        >
          Submit
        </button>
      </div>
      <div className="input-group mb-3 mt-3 ms-3">
        <span className="input-group-text">Latitude</span>
        <input
          aria-label={"Latitude input box"}
          placeholder=""
          type="number"
          min={-90}
          max={90}
          className="form-control"
          value={lat}
          onChange={(e) => setLat(e.target.value)}
          onKeyUp={(e) => {
            if (e.key == "Enter") handleConfirm();
          }}
        ></input>
        <span className="input-group-text">Longitude</span>
        <input
          aria-label={"Longitude input box"}
          placeholder=""
          type="number"
          min={-180}
          max={180}
          className="form-control"
          value={lon}
          onChange={(e) => setLon(e.target.value)}
          onKeyUp={(e) => {
            if (e.key == "Enter") handleConfirm();
          }}
        ></input>
        <span className="input-group-text">Title</span>
        <input
          aria-label={"Title input box"}
          placeholder=""
          type="text"
          className="form-control"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          onKeyUp={(e) => {
            if (e.key == "Enter") handleConfirm();
          }}
        ></input>
        <span className="input-group-text">Note</span>
        <input
          aria-label={"Note input box"}
          placeholder=""
          type="text"
          className="form-control"
          value={note}
          onChange={(e) => setNote(e.target.value)}
          onKeyUp={(e) => {
            if (e.key == "Enter") handleConfirm();
          }}
        ></input>
        <button
          aria-label={"Submit button"}
          className="btn btn-success"
          onClick={() => handleConfirm()}
        >
          Confrim
        </button>
      </div>
    </div>
  );
}

export default InputBox;
