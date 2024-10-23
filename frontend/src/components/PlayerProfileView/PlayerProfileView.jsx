import React, { useState, useEffect, useMemo } from 'react';
import { Card, CardContent, Typography, Avatar, Box, Divider, Grid, Button, Tabs, Tab, Dialog, DialogTitle, DialogContent, TextField, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { PieChart, LineChart } from '@mui/x-charts';
import { useNavigate , useParams } from 'react-router-dom';
import axios from 'axios';
import countryList from 'react-select-country-list'
import useProfilePic from '../ProfilePicture/UseProfilePicture';
import defaultProfilePic from '../../assets/default_user.png'

const playerURL = import.meta.env.VITE_PLAYER_SERVICE_URL;
const tournamentURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const eloURL = import.meta.env.VITE_ELO_SERVICE_URL;
const matchmakingURL = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;

function PlayerProfileView({ profilePic }) {

  const [value, setValue] = useState(0); // State for managing tab selection
  const [localProfilePic, setLocalProfilePic] = useState(defaultProfilePic);
  const [playerDetails, setPlayerDetails] = useState([]);
  const [uData, setUData] = useState([]);
  const [xLabels, setXLabels] = useState([]);
  const [error, setError] = useState(''); // Declare error state
  const [recentMatches, setRecentMatches] = useState([]);
  const [liveTournaments, setLiveTournaments] = useState([]);
  const { id } = useParams(); 

  const navigate = useNavigate();
  profilePic = useProfilePic();

  const handleChange = (event, newValue) => setValue(newValue);

  useEffect(() => {
    const fetchPlayerAndMatchData = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login'); // Redirect to login if no token
        return;
      }

      try {
        // Fetch player details
        const playerResponse = await axios.get(`${playerURL}/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setPlayerDetails(playerResponse.data || {});

        // Fetch chart data
        const chartResponse = await axios.get(`${eloURL}/chart/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUData(chartResponse.data.map((data) => data.elo));
        setXLabels(chartResponse.data.map((data) => data.date));

        // Fetch recent matches
        const matchResponse = await axios.get(`${matchmakingURL}/player/${id}/recent`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setRecentMatches(matchResponse.data || []);

        // Fetch live tournaments
        const tournamentResponse = await axios.get(`${tournamentURL}/live/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setLiveTournaments(tournamentResponse.data || []);
      } catch (err) {
        handleFetchError(err);
      }
    };

    fetchPlayerAndMatchData();
  }, [navigate]);

  //handle the errors 
  const handleFetchError = (err) => {
    if (err.response) {
      const statusCode = err.response.status;
      const errorMessage = err.response.data.message || 'An error occurred';
      if (statusCode === 404 || statusCode === 403) {
        setError('Player details not found or access denied');
      } else {
        navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
      }
    } else if (err.request) {
      navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
    } else {
      navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + err.message)}`);
    }
  };

  return (
    <Box
      sx={{

        display: 'grid',
        gridTemplateRows: '1fr 1fr',
        height: '100%', // Full viewport height
        backgroundColor: '#f0f0f0',// Optional: background color for the page
        justifyItems: 'center',

      }}
    >

      <Card sx={{ width: '80%', height: '500px', padding: 2, marginTop: '5%' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          {/* Profile Card Section */}
          <Avatar
            sx={{ width: 200, height: 200, marginTop: 2 }}
            alt={playerDetails.firstName}
            src={localProfilePic}
          />

          <CardContent>
            <Typography variant="playerProfile">{playerDetails.firstName + " " + playerDetails.lastName}</Typography>
          </CardContent>
        </Box>

        {/* Divider to separate sections */}
        <Divider sx={{ my: 0.5 }} />

        {/* Three Boxes Section */}
        <Grid container spacing={2} justifyContent="center">
          <Grid item xs={6}>
            <Box sx={{ backgroundColor: '#f5f5f5', padding: 2, textAlign: 'center', borderRadius: 2 }}>
              <Typography variant="header3" display='block'>Country</Typography>
              <Typography variant="playerProfile2" display='block'>{playerDetails.country}</Typography>
            </Box>
          </Grid>
          <Grid item xs={6}>
            <Box sx={{ backgroundColor: '#f5f5f5', padding: 2, textAlign: 'center', borderRadius: 2 }}>
              <Typography variant="header3" display='block'>Rating</Typography>
              <Typography variant="playerProfile2" display='block'>{playerDetails.eloRating}</Typography>

            </Box>
          </Grid>
        </Grid>
      </Card>
      <Box sx={{ width: '80%', marginTop: '0px', marginBottom: '5%' }}>
        <Card sx={{ padding: 2, height: '600px', overflowY: 'auto', }}>
          <CardContent>
            <Box sx={{ position: 'sticky', top: 0, backgroundColor: '#fff', zIndex: 1 }}>
              <Tabs
                value={value}
                onChange={handleChange}
                aria-label="tabs"
                sx={{
                  '& .MuiTabs-flexContainer': {
                    justifyContent: 'center', // Center the tabs
                  }
                }}

              >
                <Tab
                  label={
                    <Typography variant="playerProfile2" sx={{ fontSize: '1.25rem' }}>
                      Results Statistics
                    </Typography>
                  }
                  sx={{
                    padding: '12px 24px',
                    marginX: 'auto',

                  }}
                />
                <Tab
                  label={
                    <Typography variant="playerProfile2" sx={{ fontSize: '1.25rem' }}>
                      Past Matches
                    </Typography>
                  }
                  sx={{

                    padding: '12px 24px',
                    marginX: 'auto', 

                  }}
                />

                <Tab
                  label={
                    <Typography variant="playerProfile2" sx={{ fontSize: '1.25rem' }}>
                      Ongoing Tournaments
                    </Typography>
                  }

                  sx={{

                    padding: '12px 24px',
                    marginX: 'auto', 

                  }}
                />

              </Tabs>
            </Box>

            {/* tab for results statistics*/}
            {value === 0 && (
              <Box sx={{ p: 2 }}>

                <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', justifyContent: 'center', alignItems: 'center', height: '400px', marginTop: '-50px' }}>
                  <PieChart
                    series={[
                      {
                        data: [
                          { id: 0, value: playerDetails.totalWins, label: 'Wins', color: 'orange' },
                          { id: 1, value: playerDetails.totalLosses, label: 'Losses', color: 'grey' },
                        ]
                      },
                    ]}
                    width={400}
                    height={200}
                  />
                  <LineChart
                    width={500}
                    height={300}
                    series={[

                      { data: uData, label: 'Elo Rating' },
                    ]}
                    xAxis={[{ scaleType: 'point', data: xLabels, ticks: false }]}
                  />
                </Box>
              </Box>
            )}

            {/* tab for recent matches*/}
            {value === 1 && (
              <Box sx={{ display: 'flex', flexDirection: 'row', flexWrap: 'wrap', gap: 2, p: 2, justifyContent: 'center' }}>
                {recentMatches.length > 0 ? (
                  recentMatches.map((match, index) => (
                    <Card
                      key={match.id}
                      sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        p: 2,
                        backgroundColor: 'background.paper',
                        borderRadius: 2,
                        flexGrow: 1,
                        alignItems: 'center'

                      }}
                    >
                      <Typography variant="header3">{match.tournament.name}</Typography>

                      {/* Flexbox for Players and Winner */}
                      <CardContent sx={{ display: 'flex', alignItems: 'flex-start' }}>
                        {/* Left Column for Players */}
                        <Box sx={{ textAlign: 'left', alignItems: 'flex-start' }}>
                          {/* Player 1 */}
                          <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
                            <Avatar
                              alt={`Player ${match.winnerId}`}
                              src={`../../../backend/player-service/profile-picture/player_${match.winnerId}.jpg`}
                              sx={{ mr: 1 }}
                            />
                            <Typography variant="body4">Player {match.winnerId}</Typography>
                          </Box>

                          {/* Player 2 */}
                          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                            <Avatar
                              alt={`Player ${match.loserId}`}
                              src={`../../../backend/player-service/profile-picture/player_${match.loserId}.jpg`}
                              sx={{ mr: 1 }}
                            />
                            <Typography variant="body4">Player {match.loserId}</Typography>
                          </Box>
                        </Box>

                        {/* Divider */}
                        <Divider orientation="vertical" sx={{ height: '100px', ml: 5, mr: 8 }} />

                        {/* Right Column for Winner */}
                        <Box sx={{ flexShrink: 0, alignItems: 'center' }}>
                          <Typography variant="body4">Winner:</Typography>
                          <Box sx={{ mb: 2 }}>
                            <Avatar
                              alt={`Winner ${match.winnerId}`}
                              src={
                                match.winnerId
                                  ? `../../../backend/player-service/profile-picture/player_${match.winnerId}.jpg`
                                  : '/path/to/default-avatar.jpg'
                              }
                              sx={{
                                width: 56,
                                height: 56,
                                justifyContent: 'center',
                                alignContent: 'center',
                              }}
                            />
                          </Box>
                          <Typography variant="body4">
                            {match.winnerId ? `Player ${match.winnerId}` : 'Pending'}
                          </Typography>
                        </Box>
                      </CardContent>
                    </Card>
                  ))
                ) : (
                  <Typography variant="playerProfile2" align="center">
                    No recent matches available.
                  </Typography>
                )}
              </Box>
            )}

            {/* tab for ongoing tournaments */}
            {value === 2 && (
              <Box sx={{ p: 2, height: '100%' }}>

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {liveTournaments.length > 0 ? (
                    liveTournaments.map((tournament, index) => (
                      <Box
                        key={index}
                        sx={{
                          display: 'flex',
                          flexDirection: 'row',
                          alignItems: 'center',
                          padding: 2,
                          border: '1px solid #ddd',
                          borderRadius: 2,
                        }}
                        onClick={() => navigate(`/admin/tournaments/${tournament.id}`)} // Navigate to tournament details
                      >
                        <CardContent>
                          <Typography variant="header2">{tournament.name}</Typography>
                          <Typography variant="body4" display='block'>Click here to view details</Typography>
                        </CardContent>
                      </Box>
                    ))
                  ) : (
                    <Typography variant="playerProfile2">No ongoing tournaments found.</Typography>
                  )}
                </Box>
              </Box>
            )}
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
}

export default PlayerProfileView;