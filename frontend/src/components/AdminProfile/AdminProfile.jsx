import React, { useState, useEffect } from 'react';
import { Card, CardContent, Typography, Avatar, Box } from '@mui/material';
import axios from 'axios';
import defaultProfilePic from '../../assets/default_user.png';
import { useNavigate } from 'react-router-dom';
import useHandleError from '../Hooks/useHandleError';

const baseURL = import.meta.env.VITE_USER_SERVICE_URL;

function AdminProfile() {

  const [adminDetails, setAdminDetails] = useState([]);
  const navigate = useNavigate();
  const handleError = useHandleError(); 

  useEffect(() => {
    const fetchUserData = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }
      try {
        const adminResponse = await axios.get(`${baseURL}/current`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setAdminDetails(adminResponse.data || {});
      } catch (error) {
        handleError(error);
      }
    };
    fetchUserData();
  },);

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
