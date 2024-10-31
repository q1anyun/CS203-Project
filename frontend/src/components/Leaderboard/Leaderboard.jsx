import React, { useState, useEffect } from 'react';
import Profile from './Profile';
import LeaderboardHeader from './LeaderboardHeader';
import axios from 'axios';
import { Container, Grid2 } from '@mui/material';
import { Link } from "react-router-dom";

const baseURL = import.meta.env.VITE_PLAYER_SERVICE_URL;

function Leaderboard() {
  const [profiles, setProfiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get(`${baseURL}/getTop100Players`);
        setProfiles(response.data);
        setLoading(false);
      } catch (err) {
        setError('Failed to load data');
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) return <p>Loading...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div>
      <LeaderboardHeader />
      <Container maxWidth="lg" sx={{ marginTop: 4, marginBottom: 10 }}>
        <Grid2 container spacing={2}>
          {profiles.map((profile, index) => (
            <Grid2 size={12} key={profile.userId}>
              <Link
                to={`/profileview/${profile.userId}`}
                style={{ textDecoration: 'none', color: 'inherit' }} >
                <Profile
                  rank={index + 1}
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