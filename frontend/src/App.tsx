import React, { useState, useEffect, useRef, useMemo } from "react";
import "./style/App.css";
import Map, {
  Layer,
  MapLayerMouseEvent,
  Source,
  ViewStateChangeEvent,
  MapRef,
  PointLike,
  Marker,
  Popup,
} from "react-map-gl";
import "mapbox-gl/dist/mapbox-gl.css";
import { key } from "./private/key";
import { initialData, geoLayer } from "./utils/overlays";
import InputBox from "./components/InputBox";
import MessageBox from "./components/MessageBox";
import Pin from "./utils/pin";
import { getNotes, Note } from "./utils/note";

const latitude: number = 41.824;
const longitude: number = -71.4128;
const initzoom: number = 10;

function App() {
  const [viewState, setViewState] = useState({
    longitude: longitude,
    latitude: latitude,
    zoom: initzoom,
  });


  const [notes, setNotes] = useState<[Note] | []>([]);
  const [popupInfo, setPopupInfo] = useState<Note | null>(null);
  const pins = useMemo(
    () =>
      notes.map((note, index) => (
        <Marker
          key={`marker-${index}`}
          longitude={note.longitude}
          latitude={note.latitude}
          anchor="bottom"
          aria-label="pin"
          onClick={(e) => {
            e.originalEvent.stopPropagation();
            setPopupInfo(note);
          }}
        >
          <Pin />
        </Marker>
      )),
    [notes]
  );

  const [overlay, setOverlay] = useState<GeoJSON.FeatureCollection | undefined>(
    undefined
  );
  const [message, setMessage] = useState<string>("");


  const mapRef = useRef<MapRef>(null);

  function onMapClick(e: MapLayerMouseEvent) {
    const bbox: [PointLike, PointLike] = [
      [e.point.x, e.point.y],
      [e.point.x, e.point.y],
    ];

    if (mapRef.current != null) {
      const prop = mapRef.current.queryRenderedFeatures(bbox)[0];
      if (prop != null && prop.properties !== null) {
        let name =
          prop.properties.name === undefined
            ? ""
            : prop.properties.name + ", in ";
        let city =
          prop.properties.city === undefined ? "" : prop.properties.city + ", ";
        let state =
          prop.properties.state === undefined ? "" : prop.properties.state;
        setMessage(
          `Latitude: ${e.lngLat.lat}\nLongitude: ${e.lngLat.lng}\n ${name} ${city} ${state}`
        );
      }
    }
  }


  useEffect(() => {
    setOverlay(initialData());
    getNotes().then((r) => {
      if (typeof r === "string") {
        setMessage(r);
      } else {
        setNotes(r);
      }
    });
  }, []);

  return (
    <div className="App">
      <div className="info">
        <InputBox
          setOverlay={setOverlay}
          setMessage={setMessage}
          setNotes={setNotes}
        ></InputBox>
        <MessageBox message={message}></MessageBox>
      </div>

      <Map
        ref={mapRef}
        mapboxAccessToken={key}
        longitude={viewState.longitude}
        latitude={viewState.latitude}
        zoom={viewState.zoom}
        onMove={(ev: ViewStateChangeEvent) => setViewState(ev.viewState)}
        onClick={onMapClick}
        style={{ width: window.innerWidth, height: window.innerHeight }}
        mapStyle={"mapbox://styles/mapbox/dark-v10"}
      >
        <Source id="geo_dat" type="geojson" data={overlay}>
          <Layer {...geoLayer} />
        </Source>
        {pins}

        {popupInfo && (
          <Popup
            anchor="top"
            longitude={Number(popupInfo.longitude)}
            latitude={Number(popupInfo.latitude)}
            aria-lable="pop up"
            onClose={() => setPopupInfo(null)}
          >
            <div>
              <h4 aria-label={popupInfo.title}>{popupInfo.title}</h4>
              <h6 aria-label={popupInfo.note}>{popupInfo.note}</h6>
            </div>
          </Popup>
        )}
      </Map>
    </div>
  );
}

export default App;
