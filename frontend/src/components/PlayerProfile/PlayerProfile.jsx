import React, { useState, useEffect, useMemo } from 'react';
import { Card, CardContent, Typography, Avatar, Box, Divider, Grid, Button, Tabs, Tab, Dialog, DialogTitle, DialogContent, TextField, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import { styled } from '@mui/material/styles';
import axios from 'axios';
import countryList from 'react-select-country-list'
import defaultProfilePic from '../../assets/default_user.png'
import EditProfileDialog from './EditProfileDialog';
import { useNavigate } from 'react-router-dom';


const baseURL = import.meta.env.VITE_PLAYER_SERVICE_URL;
const baseURL2 = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const baseURL3 = import.meta.env.VITE_ELO_SERVICE_URL;
const baseURL4 = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;

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
  const [playerDetails, setPlayerDetails] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState(''); // Declare error state
  const navigate = useNavigate();



  const options = useMemo(() => countryList().getData(), []);


  const handleChange = (event, newValue) => setValue(newValue);
  const handleOpenEdit = () => setOpenEdit(true);
  const handleCloseEdit = () => setOpenEdit(false);

  const handleDetailChange = (event) => {
    const { name, value } = event.target;

    // Update the corresponding field in playerDetails
    setPlayerDetails((prevDetails) => ({
      ...prevDetails,
      [name]: value
    }));
  };


  useEffect(() => {
    const fetchPlayerAndMatchData = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login'); // Redirect to login if no token
        return;
      }

      try {
        // Fetch player details
        const playerResponse = await axios.get(`${baseURL}/currentPlayerById`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setPlayerDetails(playerResponse.data || {});

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
        backgroundColor: '#f0f0f0',
        height: '100vh',
        justifyItems: 'center',

      }}
    >

      <Card sx={{ width: '80%', height: '500px', padding: 2, marginTop: '5%' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          {/* Profile Card Section */}
          <Avatar
            sx={{ width: 200, height: 200, marginTop: 2 }}
            alt={playerDetails.firstName}
            src={profilePic}
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
      <EditProfileDialog
        open={openEdit}
        handleClose={handleCloseEdit}
        playerDetails={playerDetails}
        handleDetailChange={handleDetailChange}
        handleFileAndImageUpload={handleFileAndImageUpload}
        handleSave={handleSave}
        options={options}
        profilePic={profilePic}
      />



    </Box>



  );
}

export default PlayerProfile;