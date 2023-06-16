import { useState } from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import InputBox from "../src/components/InputBox";
import MessageBox from "../src/components/MessageBox";
import userEvent from "@testing-library/user-event";
import { Note } from "../src/utils/note";


  test("displays reset", async () => {
    async () => {
      const [overlay, setOverlay] = useState<
        GeoJSON.FeatureCollection | undefined
      >(undefined);
      const [message, setMessage] = useState<string>("");
      const [notes, setNotes] = useState<[Note] | []>([]);
      render(
        <div>
          <InputBox
            setOverlay={setOverlay}
            setMessage={setMessage}
            setNotes={setNotes}
          />
          <MessageBox message={message}></MessageBox>
        </div>
      );
      const long = screen.getByRole("numberbox", {
        name: "Max Longitude input box",
      });
      const submit = screen.getByRole("button", { name: "Submit button" });
      const reset = screen.getByRole("button", { name: "Reset button" });
      userEvent.type(long, "asdqwas");
      reset.click();
      expect(long.textContent).toBe("");
      userEvent.type(long, "300"); // exceed range
      submit.click();
      expect(screen.getByText(/unvalid input/i)).toNotBeInTheDocument();
    };
  });

  test("displays confirm", async () => {
    async () => {
      const [overlay, setOverlay] = useState<
        GeoJSON.FeatureCollection | undefined
      >(undefined);
      const [message, setMessage] = useState<string>("");
      const [notes, setNotes] = useState<[Note] | []>([]);
      render(
        <div>
          <InputBox
            setOverlay={setOverlay}
            setMessage={setMessage}
            setNotes={setNotes}
          />
          <MessageBox message={message}></MessageBox>
        </div>
      );
      const long = screen.getByRole("numberbox", {
        name: "Longitude input box",
      });
      const confirm = screen.getByRole("button", { name: "Confirm button" });
      userEvent.type(long, "300"); // exceed range
      confirm.click();
      expect(
        screen.getByText(/unvalid input/i)
      ).toNotBeInTheDocument();
    };
  });

  test("displays message add", async () => {
    async () => {
      const [overlay, setOverlay] = useState<
        GeoJSON.FeatureCollection | undefined
      >(undefined);
      const [message, setMessage] = useState<string>("");
      const [notes, setNotes] = useState<[Note] | []>([]);
      render(
        <div>
          <InputBox
            setOverlay={setOverlay}
            setMessage={setMessage}
            setNotes={setNotes}
          />
          <MessageBox message={message}></MessageBox>
        </div>
      );
      const long = screen.getByRole("numberbox", {
        name: "Max Longitude input box",
      });
      const reset = screen.getByRole("button", { name: "Reset button" });
      const submit = screen.getByRole("button", { name: "Submit button" });
      const keyword = screen.getByRole("textbox", {
        name: "Keyword input box",
      });
      const confirm = screen.getByRole("button", { name: "Confrim button" });

      userEvent.type(long, "hello");
      userEvent.click(reset);
      long.innerText = "";
      submit.click();
      userEvent.type(keyword, "world");
      confirm.click();
      expect(screen.getByText(/!/i)).toNotBeInTheDocument();
    };
  });
