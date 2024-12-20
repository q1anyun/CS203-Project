import React from 'react';
import { Stack, Typography, Avatar, Paper } from '@mui/material';

function Profile({ rank, firstName, lastName, eloRating, profilePhoto }) {
  const fullName = `${firstName} ${lastName.toUpperCase()}`;

  return (
    <Paper variant="outlined" sx={{ padding: 2 }}>
      <Stack direction="row" alignItems="center" spacing={5} justifyContent="space-between">
        <Stack direction="row" alignItems="center" spacing={3}>
          <Typography variant="header2" sx={{width: "4rem", textAlign: "center"}}>{rank}</Typography>
          <Avatar alt="Profile" src={profilePhoto} sx={{ width: 56, height: 56 , border: '1px solid'}} />
          <Stack>
            <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
              {fullName}
            </Typography>
          </Stack>
        </Stack>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }}>
          {eloRating}
        </Typography>
      </Stack>
    </Paper>
  );
}

export default Profile;
