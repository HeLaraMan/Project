const BACKEND_URL = "http://localhost:8080/finalproject"; // CHANGE this to match your Eclipse project backend

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

