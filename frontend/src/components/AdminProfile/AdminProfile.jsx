import React, { useState, useEffect}  from 'react';
import { styled } from '@mui/material/styles';
import { Card, CardContent, Typography, Avatar, Box,  Button, Tabs, Tab, Dialog, DialogTitle, DialogContent, TextField } from '@mui/material';
import useProfilePic from '../ProfilePicture/UseProfilePicture';
import defaultProfilePic from '../../assets/default_user.png'
import axios from 'axios';
const baseURL = import.meta.env.VITE_USER_SERVICE_URL; // Base URL for API calls

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

function AdminProfile({ profilePic }) {

  const [localProfilePic, setLocalProfilePic] = useState(defaultProfilePic);
  const [adminDetails, setAdminDetails] = useState([]); 
  profilePic = useProfilePic();


  useEffect(() => {
    if (profilePic) {
      setLocalProfilePic(profilePic);
      // Update localProfilePic when profilePic changes
    }
  }, [profilePic]);

  useEffect(() => {
    const fetchUserData = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login'); // Redirect to login if no token
        return;
      }

    
        // Fetch player details
        const adminResponse = await axios.get(`${baseURL}/current`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setAdminDetails(adminResponse.data || {});

      };

      fetchUserData();
    }, );

    //in progress will add the profile pic upload when i set up bucket 3 
  
 


  return (
    <Box
      sx={{

        display: 'grid',
        gridTemplateRows: '1fr 1fr',
        height: 'auto', // Full viewport height
        backgroundColor: '#f0f0f0',// Optional: background color for the page
        justifyItems: 'center',

      }}
    >

      <Card sx={{ width: '80%', height: '500px', padding: 2, marginTop: '5%' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          {/* Profile Card Section */}
          <Avatar
            sx={{ width: 200, height: 200, marginTop: 2 }}
            alt={adminDetails.username}
            src={localProfilePic}
          />

          <Button
            className="button"
            variant="contained"
            color="primary"
            // onClick={handleOpenEdit}
          >
            Edit Profile
          </Button>
          <CardContent>
            <Typography variant="h4">{adminDetails.username}</Typography>
          </CardContent>
        </Box>
      </Card>
     
      
      

    </Box>
  );
}

export default AdminProfile;
