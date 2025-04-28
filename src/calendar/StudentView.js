import React, { useState, useEffect } from "react";
import { DayPilotCalendar } from "@daypilot/daypilot-lite-react";
import { fetchSessions, signUpSession } from "../services/api";

function StudentView() {
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

  const onEventClick = async (args) => {
    if (window.confirm(`Sign up for session "${args.e.text()}"?`)) {
      try {
        await signUpSession(args.e.data.id);
        alert("Successfully signed up!");
      } catch (error) {
        console.error("Failed to sign up:", error);
      }
    }
  };

  return (
    <div>
      <h1>Student TA Session Selector</h1>
      <DayPilotCalendar
        viewType={"Week"}
        events={{ list: events }}
        onEventClick={onEventClick}
      />
    </div>
  );
}

export default StudentView;
