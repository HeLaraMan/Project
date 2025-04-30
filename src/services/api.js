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

// uses the GET method by default
// Returns all sessions to be displayed on the calendar
export async function fetchSessions() {
  const res = await fetch(`${BACKEND_URL}/sessions`);
  return res.json();
}

export async function createSession(session) {
  await fetch(`${BACKEND_URL}/sessions`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(session),
  });
}

export async function deleteSession(id) {
  await fetch(`${BACKEND_URL}/sessions/${id}`, {
    method: "DELETE",
  });
}

export async function signUpSession(sessionId) {
  await fetch(`${BACKEND_URL}/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ sessionId }),
  });
}

export async function removeSignup(sessionId) {
  await fetch(`${BACKEND_URL}/signup/${sessionId}`, {
    method: "DELETE",
  });
}