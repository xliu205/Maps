interface MessageBoxProps {
  message: string;
}

function InputBox(props: MessageBoxProps) {
  return (
    <div
      className="messageBox alert alert-primary ms-3"
      role="alert"
      aria-label={props.message}
    >
      <p className="mb-0">
        <small>
          help: Latitude is between -90 to 90, Longitude is between -180 to 180.{" "}
        </small>
      </p>
      <hr className="my-1" />
      <p className="mb-0">
        <b>Message:</b>
      </p>
      <div className="text-center">{props.message}</div>
    </div>
  );
}

export default InputBox;
