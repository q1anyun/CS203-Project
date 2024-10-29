import React, { useState, useEffect } from 'react';
import { Card, CardContent, Typography, Box, Divider, Grid, Avatar,Tab,Tabs } from '@mui/material';
import axios from 'axios'
const baseURL = import.meta.env.VITE_TOURNAMENT_SWISSBRACKET_URL;
function SwissBracketDetails({ matches }) {
    const [currentRound, setCurrentRound] = useState(0); 
    const [numberOfRounds, setNumberOfRounds] = useState(0);
    const [tabValue, setTabValue] = useState('swiss');  // Default tab to show Swiss rounds

    const handleChangeTab = (event, newValue) => {
        setTabValue(newValue);
    };

    // useEffect(() => {
    //     const fetchSwissBracket = async () => {
    //         const response = await axios.get(`${baseURL}/choices`);
    //         setRoundTypeOptions(response.data);
    //     };

    //     fetchRoundType();
    // }, []);

 

  // Grouping the matches by the swissRoundNumber
  const matchesByRound = matches.reduce((acc, match) => {
    const roundNumber = `Round ${match.swissRoundNumber || 'Unknown'}`;
    acc[roundNumber] = acc[roundNumber] || [];
    acc[roundNumber].push(match);
    return acc;
  }, {});

  return (
    <Box sx={{ padding: 2 }}>
    <Tabs value={tabValue} onChange={handleChangeTab} textColor="primary" indicatorColor="primary">
        <Tab label="Swiss" value="swiss" />
        <Tab label="Knockout" value="knockout" />
    </Tabs>

    <Divider sx={{ my: 2 }} />

    {tabValue === 'swiss' && Object.keys(matchesByRound).length > 0 ? (
        Object.entries(matchesByRound).map(([roundName, roundMatches]) => (
            <Card key={roundName} sx={{ marginBottom: 2 }}>
                <CardContent>
                    <Typography variant="header3">{roundName}</Typography>
                    <Grid container spacing={2}>
                        {roundMatches.map((match) => (
                            <Grid item xs={12} sm={6} md={3} key={match.id}>
                                <Box
                                    sx={{
                                        display: 'flex',
                                        flexDirection: 'column',
                                        p: 2,
                                        backgroundColor: 'background.paper',
                                        border: '1px solid',
                                        borderColor: 'divider',
                                        borderRadius: 2,
                                    }}
                                >
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
                                        <Avatar alt={`Player ${match.player1Id}`} src={`/path/to/profile_picture/player_${match.player1Id}.jpg`} sx={{ mr: 1 }} />
                                        <Typography variant="body1">Player {match.player1Id}</Typography>
                                    </Box>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                        <Avatar alt={`Player ${match.player2Id}`} src={`/path/to/profile_picture/player_${match.player2Id}.jpg`} sx={{ mr: 1 }} />
                                        <Typography variant="body1">Player {match.player2Id}</Typography>
                                    </Box>
                                    <Typography variant="body2" mt={1}>
                                        Winner: {match.winnerId ? `Player ${match.winnerId}` : 'Pending'}
                                    </Typography>
                                </Box>
                            </Grid>
                        ))}
                    </Grid>
                </CardContent>
            </Card>
        ))
    ) : (
        <Typography variant="body1">
            No matches available yet.
        </Typography>
    )}

    {tabValue === 'knockout' && (
        <Typography variant="h6" align="center"></Typography>
    )}
</Box>
);
}

export default SwissBracketDetails;