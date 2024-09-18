import React, { useState } from 'react';
import './PlayerProfile.css';
import { Card, CardContent, Typography, Avatar, Box, Divider, Grid, Button, Tabs, Tab } from '@mui/material';
import { PieChart, LineChart } from '@mui/x-charts';

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



function PlayerProfile({ profilePic }) {
  const [localProfilePic] = useState(profilePic);
  const [playerName] = useState('Magnus Carlsen');
  const wins = 8;
  const losses = 8;
  const totalGames = wins + losses;
  const winrate = (wins / totalGames) * 100;
  const [value, setValue] = useState(0); // State for managing tab selection
  const handleChange = (event, newValue) => {
    setValue(newValue);
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
                <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', justifyContent: 'center', alignItems: 'center', height: '400px', marginTop: '-50px', marginleft: '10%' }}>
                  <PieChart
                    series={[
                      { data: data },
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
                    xAxis={[{ scaleType: 'point', data: xLabels, ticks: false}]}
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


    </Box>



  );
}

export default PlayerProfile;