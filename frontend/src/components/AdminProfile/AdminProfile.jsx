import React, { useState, useEffect}  from 'react';
import { styled } from '@mui/material/styles';
import { Card, CardContent, Typography, Avatar, Box,  Button, Tabs, Tab, Dialog, DialogTitle, DialogContent, TextField } from '@mui/material';
import axios from 'axios';
import defaultProfilePic from '../../assets/default_user.png'; 

const baseURL = import.meta.env.VITE_USER_SERVICE_URL; 

function AdminProfile() {

  const [adminDetails, setAdminDetails] = useState([]); 


  useEffect(() => {
    const fetchUserData = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login'); // Redirect to login if no token
        return;
      }
       
        const adminResponse = await axios.get(`${baseURL}/current`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setAdminDetails(adminResponse.data || {});

      };

      fetchUserData();
    }, );

 

  return (
    <Box
      sx={{

        display: 'grid',
        gridTemplateRows: '1fr 1fr',
        height: '100vh',
        backgroundColor: '#f0f0f0',
        justifyItems: 'center',

      }}
    >

      <Card sx={{ width: '80%', height: '500px', padding: 2, marginTop: '5%' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          {/* Profile Card Section */}
          <Avatar
            sx={{ width: 200, height: 200, marginTop: 2 }}
            alt={adminDetails.username}
            src={defaultProfilePic}
          />
          <CardContent>
            <Typography variant="playerProfile2"><strong>Username : </strong>{adminDetails.username}</Typography>
            <Typography variant="playerProfile2" display={'block'}><strong>Email : </strong>{adminDetails.email}</Typography>
          </CardContent>
        </Box>
      </Card>
     
      
      

    </Box>
  );
}

export default AdminProfile;
