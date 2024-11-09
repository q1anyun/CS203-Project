import React, { useState, useEffect } from 'react';
import Profile from './Profile';
import LeaderboardHeader from './LeaderboardHeader';
import axios from 'axios';
import { Container, Grid2 } from '@mui/material';
import { Link } from "react-router-dom";
import { useNavigate } from 'react-router-dom';
import defaultProfilePic from '../../assets/default_user.png';

const baseURL = import.meta.env.VITE_PLAYER_SERVICE_URL;

function Leaderboard() {
  const [profiles, setProfiles] = useState([]);
  const [topThree, setTopThree] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get(`${baseURL}/top100Players`);
        // Separate top 3 players
        const top3 = response.data.slice(0, 3);
        const remainingPlayers = response.data.slice(3);
        
        setTopThree(await attachProfilePhotos(top3));
        setProfiles(await attachProfilePhotos(remainingPlayers));
      } catch (error) {
        if (error.response) {
          const statusCode = error.response.status;
          const errorMessage = error.response.data?.message || 'An unexpected error occurred';
          navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
        } else if (err.request) {
          navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
        } else {
          navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + err.message)}`);
        }
      }
    };

    fetchData();
  }, []);


  const attachProfilePhotos = async (players) => {
    const token = localStorage.getItem('token');
    return await Promise.all(
      players.map(async (player) => {
        try {
          const profilePictureResponse = await axios.get(`${baseURL}/photo/${player.playerId}`, {
            headers: { Authorization: `Bearer ${token}` },
            responseType: 'blob',
          });
          const imageUrl = URL.createObjectURL(profilePictureResponse.data);
          return { ...player, profilePicture: imageUrl };
        } catch {
          // If photo fetch fails, add a default image or handle as needed
          return { ...player, profilePicture: defaultProfilePic };
        }
      })
    );
  };


  return (
    <div>
      <LeaderboardHeader topPlayers={topThree} />
      <Container maxWidth="lg" sx={{ marginTop: 4, marginBottom: 10 }}>
        <Grid2 container spacing={2}>
          {profiles.map((profile, index) => (
            <Grid2 size={12} key={profile.playerId}>
              <Link
                to={`/profileview/${profile.playerId}`}
                style={{ textDecoration: 'none', color: 'inherit' }}
              >
                <Profile
                  rank={index + 4} // Add 4 to account for top 3 players
                  firstName={profile.firstName}
                  lastName={profile.lastName}
                  eloRating={profile.eloRating}
                  profilePhoto={profile.profilePicture}
                />
              </Link>
            </Grid2>
          ))}
        </Grid2>
      </Container>
    </div>
  );
}

export default Leaderboard;