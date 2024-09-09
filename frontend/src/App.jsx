import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import NavBar from './components/NavBar/NavBar';
import Home from './components/Home/Home';
import LoginPage from './components/LoginPage/LoginPage';
import SignUpPage from './components/SignUpPage/SignUpPage';
import PlayerProfile from './components/PlayerProfile/PlayerProfile'; 
import AdminProfile from './components/AdminProfile/AdminProfile'; 
import PlayerTournamentView from './components/PlayerTournamentView/PlayerTournamentView';
import AdminTournamentView from './components/AdminTournamentView/AdminTournamentView';
import Leaderboard from './components/Leaderboard/Leaderboard';

function AppContent() {
  const location = useLocation();
  const hideNavBarPaths = ['/login', '/signup'];

  return (
    <>
      {!hideNavBarPaths.includes(location.pathname) && <NavBar />}
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/home" element={<Home />} />
        <Route path="/player/tournaments" element={<PlayerTournamentView />} />
        <Route path="/admin/tournaments" element={<AdminTournamentView />} />
        <Route path="/player/profile" element={<PlayerProfile />} />
        <Route path="/admin/profile" element={<AdminProfile />} />
        <Route path="/leaderboard" element={<Leaderboard />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
      </Routes>
    </>
  );
}

function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

export default App;
