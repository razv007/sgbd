import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './components/LoginPage';
import SignupPage from './components/SignupPage';
import HomePage from './components/HomePage';
import TimelinePage from './components/TimelinePage'; // Importă noua pagină // Import HomePage
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/" element={<HomePage />} /> {/* Add HomePage route */}
        <Route path="/timeline" element={<TimelinePage />} /> {/* Adaugă ruta pentru timeline */}
        <Route path="*" element={<Navigate to="/" replace />} /> {/* Redirect any other path to HomePage (or login if not auth) */}
      </Routes>
    </Router>
  );
}

export default App
