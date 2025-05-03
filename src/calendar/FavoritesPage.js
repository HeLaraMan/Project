import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import './FavoritesPage.css';

function FavoritesPage() {
  const [courses, setCourses] = useState([]);
  const [selectedCourses, setSelectedCourses] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    // Mock data - replace with Jakarda API call
    const mockCourses = [
      { id: 'CS101', name: 'Introduction to Computer Science' },
      { id: 'MATH201', name: 'Calculus II' },
      { id: 'PHYS101', name: 'Physics I' },
      { id: 'ENG202', name: 'Advanced Writing' },
      { id: 'BIO301', name: 'Molecular Biology' }
    ];
    setCourses(mockCourses);
    
    const saved = localStorage.getItem('favoriteCourses');
    if (saved) setSelectedCourses(JSON.parse(saved));
  }, []);

  const toggleCourse = (courseId) => {
    setSelectedCourses(prev => 
      prev.includes(courseId) 
        ? prev.filter(id => id !== courseId)
        : [...prev, courseId]
    );
  };

  const handleSave = () => {
    localStorage.setItem('favoriteCourses', JSON.stringify(selectedCourses));
    navigate('/student');
  };

  return (
    <div className="favorites-page">
      <h1>Select Favorite Courses</h1>
      <div className="courses-list">
        {courses.map(course => (
          <div 
            key={course.id}
            className={`course-item ${selectedCourses.includes(course.id) ? 'selected' : ''}`}
            onClick={() => toggleCourse(course.id)}
          >
            <span>{course.id}: {course.name}</span>
            {selectedCourses.includes(course.id) && <span className="star">★</span>}
          </div>
        ))}
      </div>
      <div className="actions">
        <button onClick={handleSave}>Save & Return</button>
        <button onClick={() => navigate('/student')}>Cancel</button>
      </div>
    </div>
  );
}

export default FavoritesPage;