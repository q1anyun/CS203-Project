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
import Leaderboard from './components/Leaderboard/Leaderboard';
import defaultProfilePic from './assets/default_user.png';
import DefaultErrorPage from './components/DefaultErrorPage/DefaultErrorPage';
import TournamentDetails from './components/TournamentDetails/TournamentDetails';
import AdminTournamentDetails from './components/AdminTournamentDetails/AdminTournamentDetails';
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute';
import useProfilePic from './components/ProfilePicture/UseProfilePicture';
import { ThemeProvider } from '@mui/material/styles';
import theme from './styles/theme.js';

function AppContent() {
  const location = useLocation();
  const hideNavBarPaths = ['/login', '/signup', '/error'];
  // State to store the current profile picture, initialized with the default image
  const profilePic = useProfilePic(); 

 

  return (
    <>
      {!hideNavBarPaths.includes(location.pathname) && <NavBar profilePic={profilePic} />}
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />

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
            <PlayerProfile profilePic={profilePic} />
          </ProtectedRoute>} />

        <Route path="/admin/profile"
          element={<ProtectedRoute>
            <AdminProfile profilePic={profilePic} />
          </ProtectedRoute>} />

        <Route path="/admin/tournaments/:id"
          element={<ProtectedRoute>
            <AdminTournamentDetails />
          </ProtectedRoute>} />

          <Route path="/player/tournaments/:id"
          element={<ProtectedRoute>
            <TournamentDetails />
          </ProtectedRoute>} />
       
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
    <ThemeProvider theme={theme}>

    <Router>
      <AppContent />
    </Router>
    </ThemeProvider>
  );
}

export default App;
