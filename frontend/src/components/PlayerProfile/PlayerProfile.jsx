import React, { useState, useEffect } from 'react';
import './PlayerProfile.css';
import { Card, CardContent, Typography, Avatar, Box, Divider, Grid, Button, Tabs, Tab, Dialog, DialogTitle, DialogContent, TextField } from '@mui/material';
import { PieChart, LineChart } from '@mui/x-charts';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { styled } from '@mui/material/styles';
import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';
import axios from 'axios';

const baseURL = import.meta.env.VITE_PLAYER_SERVICE_URL;

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

const data = [
  { id: 0, value: 8, label: 'Wins', color: 'orange' },
  { id: 1, value: 9, label: 'Losses', color: 'grey' },
];
const uData = [1500, 1528, 1560, 1600, 1670, 1800, 1900];
const xLabels = [
  '1',
  '2',
  '3',
  '4',
  '5',
  '6',
  '7',
];





function PlayerProfile({ profilePic, onProfilePicUpdate }) {

  const [value, setValue] = useState(0); // State for managing tab selection
  const [openEdit, setOpenEdit] = useState(false);
  const [localProfilePic, setLocalProfilePic] = useState(profilePic);
  const [playerName, setPlayerName] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [country, setCountry] = useState('-');
  // const [email, setEmail] = useState('');
  // const [password, setPassword] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [rating, setRating] = useState(0);
  const [error, setError] = useState(''); // Declare error state
  const navigate = useNavigate();

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  const handleOpenEdit = () => setOpenEdit(true);
  const handleCloseEdit = () => setOpenEdit(false);

  // const handleNameChange = (event) => setPlayerName(event.target.value);
  const handleFirstNameChange = (event) => setFirstName(event.target.value);
  const handleLastNameChange = (event) => setLastName(event.target.value);
  const handleCountryChange = (event) => setCountry(event.target.value);
  // const handleEmailChange = (event) => setEmail(event.target.value);
  // const handlePasswordChange = (event) => setPassword(event.target.value);


    useEffect(() => {
      const fetchPlayerDetails = async () => {
          try {
              const token = localStorage.getItem('token');

              if (!token) {
                  navigate('/login'); // Redirect to login if no token found
                  return;
              }

              const response = await axios.get(`${baseURL}/currentPlayerById`, {
                  headers: {
                      'Authorization': `Bearer ${token}`
                  }
              });

              console.log('Player Data:', response.data);
              const { firstName, lastName, eloRating, profilePic, country} = response.data;

              // Store the player details in local state or localStorage as needed
              setPlayerName(firstName + " " +lastName || '');
              setFirstName(firstName || '');
              setLastName(lastName || '');
              setCountry(country ||'-'); 
              setRating(eloRating || '-'); 
              // setEmail(email || '');
              setLocalProfilePic(profilePic || '');


          } catch (err) {
              if (err.response) {
                  const statusCode = err.response.status;
                  const errorMessage = err.response.data.message || 'An error occurred';

                  // Handle specific error statuses
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

      fetchPlayerDetails();
  }, [navigate]); 

  


  const handleFileAndImageUpload = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      const imageUrl = URL.createObjectURL(file);
      setLocalProfilePic(imageUrl);
      onProfilePicUpdate(imageUrl);
    }
  };


  const handleSave = async () => {
    const token = localStorage.getItem('token'); // Use the token for authentication

    const playerData = {
      firstName: firstName,
      lastName: lastName,
      country: country,
    };

    if (selectedFile) {
      // You may need to handle file uploads separately
      const reader = new FileReader();
      reader.readAsDataURL(selectedFile); // Convert file to base64
      reader.onloadend = async () => {
        playerData.profilePic = reader.result; // Assign base64 string to profilePic
        await sendUpdate(playerData, token);
      };
    } else {
      await sendUpdate(playerData, token);
    }
  };

  const sendUpdate = async (playerData, token) => {
    try {
      const response = await axios.put(`${baseURL}/currentPlayerById`, playerData, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json', // Sending as JSON
        },
      });
      setPlayerName(firstName + " " +lastName || '');
      setCountry(country);

      if (response.status === 200) {
        console.log('Profile updated successfully');
      } else {
        console.error('Error updating profile');
      }
    } catch (error) {
      if (error.response) {
        console.error('Error Status:', error.response.status);
        console.error('Error Data:', error.response.data);
      } else if (error.request) {
        console.error('No response received:', error.request);
      } else {
        console.error('Error Message:', error.message);
      }
    }

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
            alt={playerName}
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
            <Typography variant="h4">{playerName}</Typography>
          </CardContent>
        </Box>

        {/* Divider to separate sections */}
        <Divider sx={{ my: 2 }} />

        {/* Three Boxes Section */}
        <Grid container spacing={2} justifyContent="center">
          <Grid item xs={4}>
            <Box sx={{ backgroundColor: '#f5f5f5', padding: 2, textAlign: 'center', borderRadius: 2 }}>
              <Typography variant="h6">7</Typography>
              <Typography variant="body2">Rank</Typography>
            </Box>
          </Grid>
          <Grid item xs={4}>
            <Box sx={{ backgroundColor: '#f5f5f5', padding: 2, textAlign: 'center', borderRadius: 2 }}>
              <Typography variant="h6">{country}</Typography>
              <Typography variant="body2">Country</Typography>
            </Box>
          </Grid>
          <Grid item xs={4}>
            <Box sx={{ backgroundColor: '#f5f5f5', padding: 2, textAlign: 'center', borderRadius: 2 }}>
              <Typography variant="h6">{rating}</Typography>
              <Typography variant="body2">Rating</Typography>
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
                aria-label="tabs example"
                sx={{
                  '& .MuiTabs-flexContainer': {
                    justifyContent: 'center', // Center the tabs
                  }
                }}

              >
                <Tab
                  label="Results Statistics"
                  sx={{
                    fontSize: '1.25rem',
                    padding: '12px 24px',
                    marginX: 'auto', // Add horizontal margin between tabs

                  }}
                />
                <Tab
                  label="Past Matches"
                  sx={{
                    fontSize: '1.25rem',
                    padding: '12px 24px',
                    marginX: 'auto', // Add horizontal margin between tabs

                  }}
                />

                <Tab
                  label="Ongoing Tournaments"

                  sx={{
                    fontSize: '1.25rem',
                    padding: '12px 24px',
                    marginX: 'auto', // Add horizontal margin between tabs

                  }}
                />

              </Tabs>
            </Box>
            {value === 0 && (
              <Box sx={{ p: 2 }}>
                <Typography variant="body1">Content for Tab 1</Typography>
                {/* Add more content for Tab 1 here */}
                {/* Pie Chart Section */}
                <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', justifyContent: 'center', alignItems: 'center', height: '400px', marginTop: '-50px' }}>
                  <PieChart
                    series={[
                      { data: data },
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
              <Box sx={{ p: 2, height: '100%' }}>
                <Typography variant="h6" sx={{ mb: 2 }}>Tab 1 Content</Typography>

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, }}>
                  <Box sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: 2, border: '1px solid #ddd', borderRadius: 2 }}>

                    <CardContent>
                      <Typography variant="h6">Item 1</Typography>
                      <Typography variant="body2">Details about Item 1</Typography>
                    </CardContent>

                  </Box>
                  <Box sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: 2, border: '1px solid #ddd', borderRadius: 2 }}>

                    <CardContent>
                      <Typography variant="h6">Item 2</Typography>
                      <Typography variant="body2">Details about Item 2</Typography>
                    </CardContent>

                  </Box>
                  <Box sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: 2, border: '1px solid #ddd', borderRadius: 2 }}>

                    <CardContent>
                      <Typography variant="h6">Item 3</Typography>
                      <Typography variant="body2">Details about Item 3</Typography>
                    </CardContent>

                  </Box>
                  <Box sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: 2, border: '1px solid #ddd', borderRadius: 2 }}>

                    <CardContent>
                      <Typography variant="h6">Item 1</Typography>
                      <Typography variant="body2">Details about Item 1</Typography>
                    </CardContent>

                  </Box>
                </Box>
                {/* Add more content for Tab 2 here */}
              </Box>
            )}

            {value === 2 && (
              <Box sx={{ p: 2, height: '100%' }}>
                <Typography variant="h6" sx={{ mb: 2 }}>Tab 3 Content</Typography>

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, }}>
                  <Box sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: 2, border: '1px solid #ddd', borderRadius: 2 }}
                    onClick={() => navigate("/player/tournaments")}>

                    <CardContent>
                      <Typography variant="h6">Singapore Open</Typography>
                      <Typography variant="body2">Click here to view details</Typography>
                    </CardContent>

                  </Box>
                  <Box sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: 2, border: '1px solid #ddd', borderRadius: 2, }}>


                    <CardContent>
                      <Typography variant="h6">Item 2</Typography>
                      <Typography variant="body2">Details about Item 2</Typography>
                    </CardContent>

                  </Box>
                  <Box sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: 2, border: '1px solid #ddd', borderRadius: 2 }}>

                    <CardContent>
                      <Typography variant="h6">Item 3</Typography>
                      <Typography variant="body2">Details about Item 3</Typography>
                    </CardContent>

                  </Box>
                  <Box sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', padding: 2, border: '1px solid #ddd', borderRadius: 2 }}>

                    <CardContent>
                      <Typography variant="h6">Item 1</Typography>
                      <Typography variant="body2">Details about Item 1</Typography>
                    </CardContent>

                  </Box>
                </Box>
                {/* Add more content for Tab 2 here */}
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
            alt={playerName}
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

          {/* <TextField
            margin="dense"
            label="Username"
            fullWidth
            value={playerName}
            onChange={handleNameChange}
          /> */}
          <TextField
            margin="dense"
            label="First Name"
            fullWidth
            value={firstName}
            onChange={handleFirstNameChange}
          />
          <TextField
            margin="dense"
            label="Last Name"
            fullWidth
            value={lastName}
            onChange={handleLastNameChange}
          />
          {/* <TextField
            margin="dense"
            label="Email"
            fullWidth
            value={email}
            onChange={handleEmailChange}
          />
          <TextField
            margin="dense"
            label="Password"
            fullWidth
            type="password"
            value={password}
            onChange={handlePasswordChange}
          /> */}
          <TextField
            margin="dense"
            label="Country"
            fullWidth
            type="country"
            value={country}
            onChange={handleCountryChange}
          />


          <Button onClick={handleSave} color="primary">
            Save
          </Button>
        </DialogContent>
      </Dialog>

    </Box>



  );
}

export default PlayerProfile;