import React, { useState } from 'react';
import './PlayerProfile.css';
import { Card, CardContent, Typography, Avatar, Box, Divider, Grid, Button, Tabs, Tab, Dialog, DialogTitle, DialogContent, TextField } from '@mui/material';
import { PieChart, LineChart } from '@mui/x-charts';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { styled } from '@mui/material/styles';

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



function PlayerProfile({ profilePic , onProfilePicUpdate}) {

  const [value, setValue] = useState(0); // State for managing tab selection
  const [openEdit, setOpenEdit] = useState(false);
  const [localProfilePic, setLocalProfilePic] = useState(profilePic);
  const [playerName, setPlayerName] = useState('Magnus Carlsen');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  



  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  const handleOpenEdit = () => setOpenEdit(true);
  const handleCloseEdit = () => setOpenEdit(false);

  const handleNameChange = (event) => setPlayerName(event.target.value);
  const handleFirstNameChange = (event) => setFirstName(event.target.value);
  const handleLastNameChange = (event) => setLastName(event.target.value);
  const handleEmailChange = (event) => setEmail(event.target.value);
  const handlePasswordChange = (event) => setPassword(event.target.value);

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
    const formData = new FormData();
    formData.append('playerName', playerName);
    formData.append('firstName', firstName);
    formData.append('lastName', lastName);
    formData.append('email', email);
    formData.append('password', password);

    if (selectedFile) {
      formData.append('profilePic', selectedFile);
    }

    try {
      const response = await fetch('/api/update-profile', {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        console.log('Profile updated successfully');
        // Optionally, refresh the UI with the new data
      } else {
        console.error('Error updating profile');
      }
    } catch (error) {
      console.error('Error:', error);
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
              <Typography variant="h6">Singapore</Typography>
              <Typography variant="body2">Country</Typography>
            </Box>
          </Grid>
          <Grid item xs={4}>
            <Box sx={{ backgroundColor: '#f5f5f5', padding: 2, textAlign: 'center', borderRadius: 2 }}>
              <Typography variant="h6">1800</Typography>
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
                    marginX: 20, // Add horizontal margin between tabs

                  }}
                />
                <Tab
                  label="Past Matches"
                  sx={{
                    fontSize: '1.25rem',
                    padding: '12px 24px',
                    marginX: 20, // Add horizontal margin between tabs

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

          <TextField
            margin="dense"
            label="Username"
            fullWidth
            value={playerName}
            onChange={handleNameChange}
          />
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
          <TextField
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