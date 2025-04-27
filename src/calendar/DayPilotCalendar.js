import React, { useState, useEffect } from "react";
import "./DayPilotCalendar.css";

const getTopFromTime = (time) => {
  const startHour = 8; // calendar starts at 8am
  const date = new Date(time);
  const hours = date.getHours() + date.getMinutes() / 60;
  const top = (hours - startHour) * 50; // 50px per hour
  return top < 0 ? 0 : top;
};

const getHeightFromTimes = (start, end) => {
  const startDate = new Date(start);
  const endDate = new Date(end);
  const diffInHours = (endDate - startDate) / (1000 * 60 * 60);
  return diffInHours * 50; // 50px per hour
};

const DayPilotCalendar = ({ events = [], viewType = "Week", onTimeRangeSelected, onEventClick }) => {
  const [calendarEvents, setCalendarEvents] = useState(Array.isArray(events) ? events : []);

  useEffect(() => {
    setCalendarEvents(Array.isArray(events) ? events : []);
  }, [events]);

  const handleBackgroundClick = (e) => {
    const hourHeight = 50;
    const startHour = 8;
    const clickedHour = Math.floor(e.nativeEvent.offsetY / hourHeight) + startHour;
    const clickedStart = new Date();
    clickedStart.setHours(clickedHour, 0, 0, 0);
    const clickedEnd = new Date(clickedStart);
    clickedEnd.setHours(clickedStart.getHours() + 1);

    if (onTimeRangeSelected) {
      onTimeRangeSelected({ start: clickedStart.toISOString(), end: clickedEnd.toISOString() });
    }
  };

  return (
    <div className="daypilot-calendar">
      <div className="daypilot-calendar-header">{viewType} View</div>
      <div className="daypilot-calendar-body" onClick={handleBackgroundClick}>
        {/* Background times */}
        {Array.from({ length: 12 }).map((_, idx) => {
          const hour = 8 + idx;
          return (
            <div key={hour} className="daypilot-calendar-hour">
              {hour}:00
            </div>
          );
        })}

        {/* Event blocks */}
        {calendarEvents.map((event) => {
          const top = getTopFromTime(event.start);
          const height = getHeightFromTimes(event.start, event.end);
          return (
            <div
              key={event.id}
              className="daypilot-calendar-event"
              style={{ top: `${top}px`, height: `${height}px` }}
              onClick={(e) => {
                e.stopPropagation();
                if (onEventClick) onEventClick({ e: { data: event } });
              }}
            >
              {event.text}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export { DayPilotCalendar };