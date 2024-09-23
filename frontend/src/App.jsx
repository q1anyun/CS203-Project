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
import TournamentDetails from './components/PlayerProfile/TournamentDetails';

function AppContent() {
  const location = useLocation();
  const hideNavBarPaths = ['/login', '/signup', '/error'];
    // State to store the current profile picture, initialized with the default image
    const [profilePic, setProfilePic] = useState(defaultProfilePic);

    // Callback to update the profile image from PlayerProfile
    const handleProfilePicUpdate = (newImage) => {
      setProfilePic(newImage); // Update profile pic state
    };

  return (
    <>
      {!hideNavBarPaths.includes(location.pathname) && <NavBar profilePic={profilePic} />}
      <Routes>
        <Route path="/" element={<Navigate to="/home" />} />
        <Route path="/home" element={<Home />} />
        <Route path="/player/tournaments" element={<PlayerTournamentView />} />
        <Route path="/admin/tournaments" element={<AdminTournamentView />} />
        <Route path="/admin/matches" element={<AdminMatchesView />} />
        <Route path="/player/profile" element={<PlayerProfile profilePic={profilePic} onProfilePicUpdate={handleProfilePicUpdate}/>} />
        <Route path="/admin/profile" element={<AdminProfile  profilePic={profilePic} onProfilePicUpdate={handleProfilePicUpdate}/>} />
        <Route path="/leaderboard" element={<Leaderboard />} />
        <Route path="/login" element={<LoginPage />} /> 
        <Route path="/signup" element={<SignUpPage />} /> 
        <Route path="/error" element={<DefaultErrorPage />} /> 
        <Route path='/tournamentview' element= {<TournamentDetails />}/>
        

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
