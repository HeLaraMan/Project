import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import TAView from "./calendar/TAView";
import StudentView from "./calendar/StudentView";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/ta" element={<TAView />} />
        <Route path="/student" element={<StudentView />} />
      </Routes>
    </Router>
  );
}

export default App;
