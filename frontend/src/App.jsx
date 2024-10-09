import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import NavBar from './components/NavBar/NavBar';
import Home from './components/Home/Home';
import LoginPage from './components/LoginPage/LoginPage';
import SignUpPage from './components/SignUpPage/SignUpPage';
import PlayerProfile from './components/PlayerProfile/PlayerProfile';
import AdminProfile from './components/AdminProfile/AdminProfile';
import PlayerTournamentView from './components/PlayerTournamentView/PlayerTournamentView';
import AdminTournamentView from './components/AdminTournamentView/AdminTournamentView';
import AdminMatchesView from './components/AdminMatchesView/AdminMatchesView';
import Leaderboard from './components/Leaderboard/Leaderboard';
import defaultProfilePic from './assets/default_user.png';
import DefaultErrorPage from './components/DefaultErrorPage/DefaultErrorPage';
import TournamentDetails from './components/TournamentDetails/TournamentDetails';
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute';
import useProfilePic from './components/ProfilePicture/UseProfilePicture';

function AppContent() {
  const location = useLocation();
  const hideNavBarPaths = ['/login', '/signup', '/error'];
  // State to store the current profile picture, initialized with the default image
  const profilePic = useProfilePic(); 

 

  return (
    <>
      {!hideNavBarPaths.includes(location.pathname) && <NavBar profilePic={profilePic} />}
      <Routes>
        {/*COMMENTED CODE, CORRECT CODE IMPLEMENTATION*/}
        {/* <Route path="/" element={<Navigate to="/login" />} />

        <Route path="/player/tournaments"
          element={<ProtectedRoute>
            <PlayerTournamentView />
          </ProtectedRoute>} />

        <Route path="/admin/tournaments"
          element={<ProtectedRoute>
            <AdminTournamentView />
          </ProtectedRoute>} />

        <Route path="/player/profile"
          element={<ProtectedRoute>
            <PlayerProfile profilePic={profilePic} onProfilePicUpdate={handleProfilePicUpdate} />
          </ProtectedRoute>} />

        <Route path="/admin/profile"
          element={<ProtectedRoute>
            <AdminProfile profilePic={profilePic} onProfilePicUpdate={handleProfilePicUpdate} />
          </ProtectedRoute>} />

        <Route path="/admin/matches"
          element={<ProtectedRoute>
            <AdminMatchesView />
          </ProtectedRoute>} /> */}

        {/* Routes to be removed, replace with commented code above */}
        <Route path="/" element={<Navigate to="/home" />} />
        <Route path="/player/tournaments" element={<PlayerTournamentView />} />
        <Route path="/admin/tournaments" element={<AdminTournamentView />} />
        <Route path="/admin/matches" element={<AdminMatchesView />} />
        <Route path="/player/profile" element={<PlayerProfile profilePic={profilePic} />} />
        <Route path="/admin/profile" element={<AdminProfile profilePic={profilePic}  />} />
        <Route path="/player/tournaments/:id" element={<TournamentDetails />} />

        {/* General Routes */}
        <Route path="/home" element={<Home />} />
        <Route path="/leaderboard" element={<Leaderboard />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/error" element={<DefaultErrorPage />} />

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
