import React, { useState, useEffect, useRef } from "react";
import { DayPilotCalendar } from "@daypilot/daypilot-lite-react";
import { fetchSessions, signUpSession } from "../services/api";
import Toastify from "toastify-js";
import "toastify-js/src/toastify.css";

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

  useEffect(() => {
    const userID = null; // To be Implemented: Retrieve user ID
    if (!userID) return;

    const socket = new WebSocket(
      `ws://localhost:8080/finalproject/notifications/${userId}`,
    );

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);

      if (data.type === "notification") {
        Toastify({
          text: `${data.title}: ${data.message}`,
          duration: 5000,
          close: true,
          gravity: "top",
          position: "right",
          backgroundColor: "#82A8AA",
        }).showToast();
      }
    };

    socket.onerror = (err) => {
      console.error("WebSocket error:", err);
    };

    return () => socket.close();
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
        events={{ list: events }}
        onEventClick={onEventClick}
      />
    </div>
  );
}

export default StudentView;
