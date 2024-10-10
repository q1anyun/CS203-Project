import React, { useState, useEffect, useMemo } from 'react';

import './PlayerProfile.css';
import { Card, CardContent, Typography, Avatar, Box, Divider, Grid, Button, Tabs, Tab, Dialog, DialogTitle, DialogContent, TextField, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { PieChart, LineChart } from '@mui/x-charts';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { styled } from '@mui/material/styles';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import countryList from 'react-select-country-list'
import useProfilePic from '../ProfilePicture/UseProfilePicture';
import defaultProfilePic from '../../assets/default_user.png'


const baseURL = import.meta.env.VITE_PLAYER_SERVICE_URL;
const baseURL2 = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const baseURL3 = import.meta.env.VITE_ELO_SERVICE_URL;

const VisuallyHiddenInput = styled('input')({
  clip: 'rect(0 0 0 0)',
  clipPath: 'inset(50%)',
  height: 1,
  overflow: 'hidden',
  position: 'absolute',
  bottom: 0,
  left: 0,
  whiteSpace: 'nowrap',
  width: 1,
});



function PlayerProfile({ profilePic }) {

  const [value, setValue] = useState(0); // State for managing tab selection
  const [openEdit, setOpenEdit] = useState(false);
  const [localProfilePic, setLocalProfilePic] = useState(defaultProfilePic);
  const [playerDetails, setPlayerDetails] = useState([]);
  const [uData, setUData] = useState([]);
  const [xLabels, setXLabels] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState(''); // Declare error state
  const [recentMatches, setRecentMatches] = useState([]);
  const [liveTournaments, setLiveTournaments] = useState([]);
  const options = useMemo(() => countryList().getData(), [])
  const navigate = useNavigate();
  profilePic = useProfilePic(); 




  useEffect(() => {
    if (profilePic) {
      setLocalProfilePic(profilePic);
      // Update localProfilePic when profilePic changes
    }
  }, [profilePic]);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  const handleOpenEdit = () => setOpenEdit(true);
  const handleCloseEdit = () => setOpenEdit(false);

  const handleDetailChange = (event) => {
    const { name, value } = event.target; // Destructure name and value from the event

    // Update the corresponding field in playerDetails
    setPlayerDetails((prevDetails) => ({
      ...prevDetails,            // Keep the existing playerDetails fields
      [name]: value              // Update only the field that triggered the change
    }));
  };

  useEffect(() => {
    const fetchPlayerAndMatchData = async () => {
      const token = localStorage.getItem('token');

      if (!token) {
        navigate('/login');  // Redirect to login if no token
        return;
      }

      try {
        // Fetch player details
        const playerResponse = await axios.get(`${baseURL}/currentPlayerById`, {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });
        console.log('player details:', playerResponse.data);
        setPlayerDetails(playerResponse.data || []);
 


        // Fetch chart data
        const chartResponse = await axios.get(`${baseURL3}/chart/current`, {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });
        console.log('chart data:', chartResponse.data);
        setUData(chartResponse.data.map((data) => data.elo));
        setXLabels(chartResponse.data.map((data) => data.date));



        // Fetch recent matches
        const matchResponse = await axios.get(`${baseURL}/recentMatches`, {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });

        console.log('past matches:', matchResponse.data);


        setRecentMatches(matchResponse.data || []);
        console.log(`${baseURL2}/live/current`);
        // Fetch live tournaments
        const tournamentResponse = await axios.get(`${baseURL2}/live/current`, {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });
        console.log('Live Tournaments:', tournamentResponse.data);

        setLiveTournaments(tournamentResponse.data || []);
      


      } catch (err) {
        // Handle errors
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
      }
    };

    fetchPlayerAndMatchData();
  }, [navigate]);



  const handleFileAndImageUpload = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      const imageUrl = URL.createObjectURL(file);
      setLocalProfilePic(imageUrl);
    }
  };



  const handleSave = async () => {
    const token = localStorage.getItem('token');

    // Update player details
    const playerData = {
      firstName: playerDetails.firstName,
      lastName: playerDetails.lastName,
      country: playerDetails.country,
    };

    // Update player details
    await axios.put(`${baseURL}/currentPlayerById`, playerData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });

    // Handle file upload
    if (selectedFile) {
      const formData = new FormData();
      formData.append("file", selectedFile);
      await axios.post(`${baseURL}/uploadProfile`, formData, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'multipart/form-data',
        },
      });
    }
    window.location.reload();
    console.log('Profile updated successfully');
    handleCloseEdit();
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

          <Button
            className="button"
            variant="contained"
            color="primary"
            onClick={handleOpenEdit}
          >
            Edit Profile
          </Button>
          <CardContent>
            <Typography variant="playerProfile">{playerDetails.firstName + " " + playerDetails.lastName}</Typography>
          </CardContent>
        </Box>

        {/* Divider to separate sections */}
        <Divider sx={{ my: 0.5 }} />

        {/* Three Boxes Section */}
        <Grid container spacing={2} justifyContent="center">
          {/* <Grid item xs={4}>
            <Box sx={{ backgroundColor: '#f5f5f5', padding: 2, textAlign: 'center', borderRadius: 2 }}>
              <Typography variant="body4">7</Typography>
              <Typography variant="header3">Rank</Typography>
            </Box>
          </Grid> */}
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
                    marginX: 'auto', // Add horizontal margin between tabs

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
                    marginX: 'auto', // Add horizontal margin between tabs

                  }}
                />

              </Tabs>
            </Box>
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
                    justifyContent='center'
                    alignItems='center'
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
                {/* Add more content for Tab 1 here */}
              </Box>
            )}


            {value === 1 && (
            <Box sx={{ p: 2, height: '100%', textAlign: 'center' }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {recentMatches.length > 0 ? (
                recentMatches.map((match, index) => (
                  <Grid container spacing={2} marginLeft="5px" key={match.id}>
                    <Grid item xs={12}>
                      <Card
                        sx={{
                          display: 'flex',
                          flexDirection: 'column',
                          p: 2,
                          backgroundColor: 'background.paper',
                          borderRadius: 2,
                          
                          flexGrow: 1,
                        }}
                      >

          
                        {/* Flexbox for Players and Winner */}
                        <CardContent sx={{ display: 'flex', alignItems: 'flex-start' }}>
                          {/* Left Column for Players */}
                          <Box sx={{ textAlign: 'left', alignItems: 'flex-start' }}>
                            {/* Player 1 */}
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
                              <Avatar
                                alt={`Player ${match.player1Id}`}
                                src={`../../../backend/player-service/profile-picture/player_${match.player1Id}.jpg`}
                                sx={{ mr: 1 }}
                              />
                              <Typography variant="header3">Player {match.player1Id}</Typography>
                            </Box>
          
                            {/* Player 2 */}
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                              <Avatar
                                alt={`Player ${match.player2Id}`}
                                src={`../../../backend/player-service/profile-picture/player_${match.player2Id}.jpg`}
                                sx={{ mr: 1 }}
                              />
                              <Typography variant="header3">Player {match.player2Id}</Typography>
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
                            <Typography variant="header3">
                              {match.winnerId ? `Player ${match.winnerId}` : 'Pending'}
                            </Typography>
                          </Box>
                        </CardContent>
                      </Card>
                    </Grid>
                  </Grid>
                ))
              ) : (
                <Typography variant="playerProfile2">No recent matches available.</Typography>
              )}
            </Box>
          </Box>
        )}
           

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
                        onClick={() => navigate(`/player/tournaments/${tournament.id}`)} // Navigate to tournament details
                      >
                        <CardContent>
                          <Typography variant="header2">{tournament.name}</Typography>
                          <Typography variant="body4" display = 'block'>Click here to view details</Typography>
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
      {/*dialog to edit the profile*/}
      <Dialog open={openEdit} onClose={handleCloseEdit}
        sx={{ '& .MuiDialog-paper': { width: '80%', maxWidth: '600px' } }} >
        <DialogTitle>Edit Profile</DialogTitle>
        <DialogContent
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',

          }}>

          {/* Display Existing Profile Picture */}

          <Avatar
            sx={{ width: 200, height: 200, marginTop: 1 }}
            alt={playerDetails.firstName}
            src={localProfilePic}  // Use current profile picture
          />




          {/* Label to Trigger File Input */}
          <Button
            component="label"
            variant="contained"
            tabIndex={-1}
            startIcon={<CloudUploadIcon />}
          >
            Upload files
            <VisuallyHiddenInput
              type="file"
              onChange={handleFileAndImageUpload}

            />
          </Button>


          <TextField
            label="First Name"
            name="firstName"
            value={playerDetails.firstName}
            onChange={handleDetailChange}
            variant="outlined"
            fullWidth
            margin="normal"
          />
          <TextField
            label="Last Name"
            name="lastName"
            value={playerDetails.lastName}
            onChange={handleDetailChange}
            variant="outlined"
            fullWidth
            margin="normal"
          />

          <FormControl fullWidth>
            <InputLabel>Country</InputLabel>
            <Select
              name="country"
              value={playerDetails.country}
              label="Country"
              onChange={handleDetailChange}
              sx={{ textAlign: 'left' }}

            >
              {options.map((country) => (
                <MenuItem key={country.value} value={country.value}>
                  {country.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <Button onClick={handleSave} color="primary">
            Save
          </Button>
        </DialogContent>
      </Dialog>

    </Box>



  );
}

export default PlayerProfile;