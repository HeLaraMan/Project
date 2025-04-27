import React, { useState, useEffect } from "react";
import { DayPilotCalendar } from "./DayPilotCalendar";
import { fetchSessions, createSession, deleteSession } from "../services/api";

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

function TAView() {
  const [events, setEvents] = useState([]);

  useEffect(() => {
    setEvents(fakeEvents);
  }, []);

  const onTimeRangeSelected = async (args) => {
    const sessionName = prompt("Enter session class name:");
    if (!sessionName) return;
    await createSession({
      text: sessionName,
      start: args.start.toString(),
      end: args.end.toString(),
    });
    const updated = await fetchSessions();
    setEvents(updated);
  };

  const onEventClick = async (args) => {
    if (window.confirm("Delete this session?")) {
      await deleteSession(args.e.data.id);
      const updated = await fetchSessions();
      setEvents(updated);
    }
  };

  return (
    <div>
      <h1>TA Schedule Helper</h1>
      <DayPilotCalendar
        viewType="Week"
        onTimeRangeSelected={onTimeRangeSelected}
        onEventClick={onEventClick}
        events={{ list: events }}
      />
    </div>
  );
}

export default TAView;
