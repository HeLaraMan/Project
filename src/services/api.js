const BACKEND_URL = "http://localhost:8080/finalproject";

export function getCookie() {
  const cookie = document.cookie; // returns "loginemail=email; exp..."

  // search for the content of loginemail
  const prefix = 'loginemail=';
  // starting position of 'loginemail='
  const start = cookie.indexOf(prefix);
  if (start === -1) return null;
  // starting positiong of ';' after 'loginemail=' 
  let end = cookie.indexOf(';', start);
  if (end === -1) end = cookie.length;
  // create return value with the actual loginemail
  const value = cookie.substring(start + prefix.length, end).trim();

  return decodeURIComponent(value); // account for encoded special character
};

// Returns all sessions to be displayed on the calendar
export async function fetchSessions() {
  const res = await fetch(`${BACKEND_URL}/sessions`);
  const data = await res.json();

  return data.map((session) => ({
    ...session,
    id: session.sessionId,
    text: `${session.courseName} (${session.instructorName})`, // formats the block like "CSCI 201 (TA Name)"
    start: session.start,
    end: session.end,
  }));
}

export async function createSession(session) {
  await fetch(`${BACKEND_URL}/sessions`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(session),
  });
}

export async function deleteSession(sessionId) {
  const email = getCookie("loginemail");

  const res = await fetch(`${BACKEND_URL}/sessions?sessionId=${sessionId}&email=${email}`, {
    method: "DELETE",
  });

  if (!res.ok) {
    const message = await res.text();

    if (res.status === 401 || res.status === 403) {
      alert("You are not authorized to delete this session.\n\n" + message);
    } else {
      alert("Something went wrong while deleting the session:\n\n" + message);
    }

    throw new Error("Delete failed: " + message);
  }
}

export async function signUpSession(sessionId) {
  const email = getCookie("loginemail");

  const res = await fetch(`${BACKEND_URL}/signup`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, sessionId }),
  });

  if (!res.ok) {
    const message = await res.text();
    throw new Error(message || "Failed to sign up.");
  }
}

export async function removeSignup(sessionId) {
  await fetch(`${BACKEND_URL}/signup/${sessionId}`, {
    method: "DELETE",
  });
}

export async function fetchCourses() {
  const res = await fetch(`${BACKEND_URL}/courses`);
  return res.json();
}