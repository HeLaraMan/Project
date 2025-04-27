import React, { useState, useEffect } from "react";
import { DayPilotCalendar } from "./DayPilotCalendar";
import { fetchSessions, signUpSession, removeSignup } from "../services/api";

const fakeEvents = [
  {
    id: "1",
    text: "CSCI 201 Office Hour - Prof. Smith",
    start: "2025-04-28T10:00:00",
    end: "2025-04-28T11:00:00",
  },
  {
    id: "2",
    text: "CSCI 270 Study Session - TA Lee",
    start: "2025-04-29T14:00:00",
    end: "2025-04-29T15:30:00",
  },
  {
    id: "3",
    text: "CSCI 356 Review - TA Johnson",
    start: "2025-04-30T09:30:00",
    end: "2025-04-30T10:30:00",
  },
];

function StudentView() {
  const [events, setEvents] = useState([]);
  const [selectedSession, setSelectedSession] = useState(null);

  useEffect(() => {
    setEvents(fakeEvents);
  }, []);

  const onEventClick = (args) => {
    setSelectedSession(args.e.data.id);
  };

  const handleConfirm = async () => {
    if (selectedSession) {
      await signUpSession(selectedSession);
      alert("Signed up!");
    }
  };

  const handleRemove = async () => {
    if (selectedSession) {
      await removeSignup(selectedSession);
      alert("Removed signup!");
    }
  };

  return (
    <div>
      <h1>Student TA Session Selector</h1>
      <DayPilotCalendar
        viewType="Week"
        onEventClick={onEventClick}
        events={{ list: events }}
      />
      <div>
        <button onClick={handleConfirm}>Confirm Signup</button>
        <button onClick={handleRemove}>Remove Signup</button>
      </div>
    </div>
  );
}

export default StudentView;
