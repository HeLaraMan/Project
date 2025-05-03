import React, { useEffect, useState } from "react";
import "./guestView.css";

function GuestView() {
  const [sessions, setSessions] = useState([]);
  const [filteredSessions, setFilteredSessions] = useState([]);
  const [expandedSessionId, setExpandedSessionId] = useState(null);
  const [query, setQuery] = useState("");

  useEffect(() => {
    fetch("http://localhost:8080/finalproject/guestCourses")
      .then((response) => response.json())
      .then((data) => {
        const loadedSessions = [];
        Object.entries(data).forEach(([courseName, sessionList]) => {
          sessionList.forEach((session) => {
            loadedSessions.push({
              id: session.sessionID,
              text: `${courseName}`, 
              courseName,
              start: session.startTime,
              end: session.endTime,
            });
          });
        });
        setSessions(loadedSessions);
        console.log("Loaded sessions:", loadedSessions);
      })
      .catch((error) => console.error("Error fetching sessions:", error));
  }, []);

  useEffect(() => {
    const lowercaseQuery = query.toLowerCase();
    const results = sessions.filter((session) =>
      session.text.toLowerCase().includes(lowercaseQuery)
    );
    setFilteredSessions(results);
  }, [query, sessions]);


  const handleToggle = (id) => {
    setExpandedSessionId((prevId) => (prevId === id ? null : id));
  };

  const displayedSessions = query ? filteredSessions : sessions;

  return (
    <div>
      <header className="navbar">
        <a href="/" className="navbar-name">
          Scheduler
        </a>
        <div>
          <a href="/finalproject/login.html" className="nav-button">
            Log in
          </a>
          <a href="/finalproject/register.html" className="nav-button">
            Sign up
          </a>
        </div>
      </header>

      <div className="guest-view-container">
        <h2>Office Hours</h2>
        <input
          type="text"
          placeholder="Search for a session..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />

        {query && displayedSessions.length === 0 && (
          <p className="error-message">No sessions found.</p>
        )}

        {displayedSessions.length > 0 && (
          <ul className="session-list">
            {displayedSessions.map((session) => (
              <li key={session.id}>
                <p>
                  <strong onClick={() => handleToggle(session.id)}>
                    {session.text}
                  </strong>
                </p>
                {expandedSessionId === session.id && (
                  <div className="session-details">
                    <p>Start: {session.start}</p>
                    <p>End: {session.end}</p>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

export default GuestView;