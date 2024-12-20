import React, { useState, useEffect } from 'react';
import { Card, CardContent, Typography, Avatar, Box, Divider, Grid, Tabs, Tab } from '@mui/material';
import { PieChart, LineChart } from '@mui/x-charts';
import { useNavigate, useParams } from 'react-router-dom';
import defaultProfilePic from '../../assets/default_user.png'; 
import axios from 'axios';
import ReactCountryFlag from 'react-country-flag';
import useHandleError from '../Hooks/useHandleError';

const playerURL = import.meta.env.VITE_PLAYER_SERVICE_URL;
const tournamentURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const eloURL = import.meta.env.VITE_ELO_SERVICE_URL;
const matchmakingURL = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;

function PlayerProfileView() {

  const [value, setValue] = useState(0);
  const [playerDetails, setPlayerDetails] = useState([]);
  const [uData, setUData] = useState([]);
  const [xLabels, setXLabels] = useState([]);
  const [recentMatches, setRecentMatches] = useState([]);
  const [liveTournaments, setLiveTournaments] = useState([]);
  const { id } = useParams();
  const [profilePicture, setProfilePicture] = useState(defaultProfilePic)

  const navigate = useNavigate();
  const handleError = useHandleError();

  const handleChange = (event, newValue) => setValue(newValue);

  useEffect(() => {
    const fetchPlayerAndMatchData = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }

      try {
        // Fetch player details
        const playerResponse = await axios.get(`${playerURL}/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setPlayerDetails(playerResponse.data || {});

        // Fetch chart data
        try {
          const chartResponse = await axios.get(`${eloURL}/chart/${id}`, {
            headers: { Authorization: `Bearer ${token}` },
          });
          setUData(chartResponse.data.map((data) => data.elo));
          setXLabels(chartResponse.data.map((data) => data.date));
        } catch (error) {
          if (error.response && error.response.status === 404) {
            console.warn('Chart data not found (404)');
          } else {
            throw error; // Rethrow if it's not a 404 error
          }
        }

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

      } catch (error) {
        handleError(error);
      }
    };

    fetchPlayerAndMatchData();
  }, [navigate]);



  useEffect(() => {
    const getProfilePicture = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }

      try {
        const profilePictureResponse = await axios.get(`${playerURL}/photo/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
          responseType: 'blob',
        });
        const imageUrl = URL.createObjectURL(profilePictureResponse.data);
        setProfilePicture(imageUrl);

      } catch (error) {
        if (error.response) {
          const statusCode = error.response.status;
          const errorMessage = error.response.data?.message || 'An unexpected error occurred';

          if (statusCode === 404) {
            setProfilePicture(defaultProfilePic);
          } else {
            navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
          }
        } else if (error.request) {
          navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
        } else {
          navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + error.message)}`);
        }
      }
    };

    getProfilePicture();
  }, [navigate]);

  return (
    <Box
      sx={{
        display: 'grid',
        gridTemplateRows: '1fr 1fr',
        height: '100%',
        backgroundColor: '#f0f0f0',
        justifyItems: 'center',

      }}
    >
      <Card sx={{ width: '80%', height: '500px', padding: 2, marginTop: '5%' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          {/* Profile Card Section */}
          <Avatar
            sx={{ width: 200, height: 200, marginTop: 2 }}
            alt={playerDetails.firstName}
            src={profilePicture}
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
              <ReactCountryFlag
                countryCode={playerDetails.country}// Assuming you have a countryCode field
                svg
                style={{
                  width: '2em',
                  height: '2em',
                  marginLeft: '10px'
                }}
                title={playerDetails.country}
              />
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
                        onClick={() => {
                          const userRole = localStorage.getItem('role');
                          const basePath = userRole === 'ADMIN' ? '/admin' : '/player';
                          navigate(`${basePath}/tournaments/${tournament.id}`);
                        }}
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