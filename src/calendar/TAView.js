import { useEffect, useRef, useState } from 'react';
import { DayPilotCalendar } from '@daypilot/daypilot-lite-react';
import { fetchSessions, createSession, deleteSession, getCookie } from '../services/api';
import './TAView.css';

function TAView() {
  const [events, setEvents] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newSessionName, setNewSessionName] = useState('');
  const [selectedTimeRange, setSelectedTimeRange] = useState(null);
  const calendarRef = useRef();

  useEffect(() => {
    async function loadSessions() {
      try {
        const data = await fetchSessions();
        setEvents(data);
      } catch (error) {
        console.error('Failed to fetch sessions:', error);
      }
    }
    loadSessions();
  }, []);

  useEffect(() => {
    // Force refresh calendar view after mount
    if (calendarRef.current && calendarRef.current.control) {
      calendarRef.current.control.update();
    }
  }, []);

  const onTimeRangeSelected = args => {
    setSelectedTimeRange(args);
    setIsModalOpen(true); // Open the modal
  };

  const handleCreateSession = async () => {
    if (!newSessionName) return;
    try {
      await createSession({
        text: newSessionName,
        email: getCookie(), // added this field to send the email to backend
        start: selectedTimeRange.start.toString(),
        end: selectedTimeRange.end.toString(),
      });
      const updated = await fetchSessions();
      setEvents(updated);
      setIsModalOpen(false); // Close the modal
      setNewSessionName(''); // Reset the input
      alert('Session created successfully!');
    } catch (error) {
      console.error('Failed to create session:', error);
    }
  };

  const onEventClick = async args => {
    if (window.confirm(`Delete session "${args.e.text()}"?`)) {
      try {
        await deleteSession(args.e.data.id);
        const updated = await fetchSessions();
        setEvents(updated);
        alert('Session deleted successfully!');
      } catch (error) {
        console.error('Failed to delete session:', error);
      }
    }
  };

  return (
    <div>
      <h1>TA Schedule Manager</h1>
      <DayPilotCalendar
        ref={calendarRef}
        viewType={'Week'}
        events={{ list: events }}
        onTimeRangeSelected={onTimeRangeSelected}
        onEventClick={onEventClick}
      />
      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal">
            <h2>Create New Session</h2>
            <input
              type="text"
              placeholder="Enter session class name"
              value={newSessionName}
              onChange={e => setNewSessionName(e.target.value)}
            />
            <div className="modal-buttons">
              <button onClick={handleCreateSession}>Create</button>
              <button onClick={() => setIsModalOpen(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default TAView;
