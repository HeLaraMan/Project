import React, { useState, useEffect, useRef } from "react";
import { DayPilotCalendar } from "@daypilot/daypilot-lite-react";
import { fetchSessions, signUpSession, removeSignup, getUserIdCookie } from "../services/api";
import Toastify from "toastify-js";
import "toastify-js/src/toastify.css";

function StudentView() {
  const [events, setEvents] = useState([]);
  const calendarRef = useRef();

  useEffect(() => {
    async function loadSessions() {
      try {
        const data = await fetchSessions();
        const favorites = JSON.parse(localStorage.getItem('favoriteCourses') || '[]');
         const events = data.map(event => ({ ...event,
           backColor: favorites.some(fav => event.text.includes(fav)) ? '#FFD700' : '#E3F2FD'
         }));
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

    return () => socket.close(); // Clean up connection when component unmounts
  }, []);


  useEffect(() => {
    // Force refresh calendar view after mount
    if (calendarRef.current && calendarRef.current.control) {
      calendarRef.current.control.update();
    }
  }, []);

 const onEventClick = async (args) => {
  const userId = getUserIdCookie();

  if (!userId) {
    alert("You must be logged in to sign up or remove sessions.");
    return;
  }

  // Check if user has already signed up (could also preload this info if stored in session data)
  const isSignedUp = window.confirm(`Have you already signed up for "${args.e.text()}"? Click OK to remove, Cancel to re-sign.`);

  try {
    if (isSignedUp) {
      await removeSignup(args.e.data.id);
      alert("Session removed from favorites!");
    } else {
      await signUpSession(args.e.data.id);
      alert("Successfully signed up!");
    }

    // Refresh events
    const updated = await fetchSessions();
    setEvents(updated);
  } catch (error) {
    console.error("Failed to update signup:", error);
  }
};

  return (
    <div>
      <h1>Student TA Session Selector</h1>
      <button
        onClick={() => window.location.href = "/finalproject/favorites"}
        style={{
          marginBottom: "10px",
          padding: "8px 16px",
          backgroundColor: "#1976d2",
          color: "white",
          border: "none",
          borderRadius: "4px",
          cursor: "pointer"
        }}
      >
        Set Favorite Courses
      </button>
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
