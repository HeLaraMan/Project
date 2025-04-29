import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate, Link } from "react-router-dom";
import TAView from "./calendar/TAView";
import StudentView from "./calendar/StudentView";

function App() {
  return (
    <Router basename="/finalproject">
      <div>
        {/* Simple Nav Bar */}
          <nav style={{ padding: "10px", backgroundColor: "#84a9ac" }}>
            <Link to="/student" style={{ marginRight: "20px", color: "white", textDecoration: "none" }}>Student View</Link>
            <Link to="/ta" style={{ color: "white", textDecoration: "none" }}>TA View</Link>
          </nav>
        {/* Routes */}
        <Routes>
          <Route path="/" element={<Navigate to="/student" />} /> {/* Auto redirect */}
          <Route path="/ta" element={<TAView />} />
          <Route path="/student" element={<StudentView />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;