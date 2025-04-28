import React, { useState, useEffect } from "react";
import { DayPilotCalendar } from "@daypilot/daypilot-lite-react";
import { fetchSessions, createSession, deleteSession } from "../services/api";

function TAView() {
  const [events, setEvents] = useState([]);

  useEffect(() => {
    async function loadSessions() {
      try {
        const data = await fetchSessions();
        setEvents(data);
      } catch (error) {
        console.error("Failed to fetch sessions:", error);
      }
    }
    loadSessions();
  }, []);

  const onTimeRangeSelected = async (args) => {
    const sessionName = prompt("Enter session class name:");
    if (!sessionName) return;
    try {
      await createSession({
        text: sessionName,
        start: args.start.toString(),
        end: args.end.toString(),
      });
      const updated = await fetchSessions();
      setEvents(updated);
      alert("Session created successfully!");
    } catch (error) {
      console.error("Failed to create session:", error);
    }
  };

  const onEventClick = async (args) => {
    if (window.confirm(`Delete session "${args.e.text()}"?`)) {
      try {
        await deleteSession(args.e.data.id);
        const updated = await fetchSessions();
        setEvents(updated);
        alert("Session deleted successfully!");
      } catch (error) {
        console.error("Failed to delete session:", error);
      }
    }
  };

  return (
    <div>
      <h1>TA Schedule Manager</h1>
      <DayPilotCalendar
        viewType={"Week"}
        events={{ list: events }}
        onTimeRangeSelected={onTimeRangeSelected}
        onEventClick={onEventClick}
      />
    </div>
  );
}

export default TAView;