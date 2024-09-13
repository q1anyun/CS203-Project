import React from 'react';
import { Stack, Typography, Avatar, Paper } from '@mui/material';

function Profile({ rank, firstName, lastName, username, eloPoints, profilePhoto }) {
  const fullName = `${firstName} ${lastName.toUpperCase()}`;

  return (
    <Paper variant="outlined" sx={{ padding: 2 }}>
      <Stack direction="row" alignItems="center" spacing={5} justifyContent="space-between">
        <Stack direction="row" alignItems="center" spacing={3}>
          <Typography variant="h4" sx={{width: "4rem", textAlign: "center"}}>{rank}</Typography>
          <Avatar alt="Profile" src={profilePhoto} sx={{ width: 56, height: 56 , border: '1px solid'}} />
          <Stack>
            <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
              {fullName}
            </Typography>
            <Typography variant="body1">@{username}</Typography>
          </Stack>
        </Stack>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }}>
          {eloPoints}
        </Typography>
      </Stack>
    </Paper>
  );
}

export default Profile;
