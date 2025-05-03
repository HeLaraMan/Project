import React, { useState, useEffect, useRef } from "react";
import { DayPilotCalendar } from "@daypilot/daypilot-lite-react";
import { fetchSessions, signUpSession, getUserIdCookie } from "../services/api";

function StudentView() {
  const [events, setEvents] = useState([]);
  const calendarRef = useRef();

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

  //this is for notifications
  useEffect(() => {
    const userId = getUserIdCookie();
    if (!userId) return;

    const socket = new WebSocket(`ws://localhost:8080/finalproject/notifications/${userId}`);

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      if (data.type === "notification") {
        alert(`${data.title}\n${data.message}`);
      }
    };

    socket.onerror = (err) => {
      console.error("WebSocket error:", err);
    };

    return () => socket.close(); // Clean up connection when component unmounts
  }, []);


  useEffect(() => {
    // Force refresh calendar view after mount
    if (calendarRef.current && calendarRef.current.control) {
      calendarRef.current.control.update();
    }
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
        ref={calendarRef}
        viewType={"Week"}
        events={events}
        onEventClick={onEventClick}
      />
    </div>
  );
}

export default StudentView;
