import React from 'react';
import { Card, CardContent, Typography, Box, Divider, Grid } from '@mui/material';


function TournamentDetails() {
  // Hardcoded tournament details
  const tournament = {
    id: 1,
    name: 'Singapore Open',
    matches: [
      { id: 1, player1: 'Player A', player2: 'Player B', result: 'Player A won' },
      { id: 2, player1: 'Player C', player2: 'Player D', result: 'Player D won' },
    ],
    bracket: [
      // Round 1
      [
        { id: 1, player1: 'Player A', player2: 'Player B', result: 'Player A won' },
        { id: 2, player1: 'Player C', player2: 'Player D', result: 'Player D won' },
      ],
      // Round 2
      [
        { id: 3, player1: 'Player A', player2: 'Player C', result: 'TBD' },
        { id: 4, player1: 'Player B', player2: 'Player D', result: 'TBD' },
      ],
      // Add more rounds as needed
    ],
    leaderboard: [
      { rank: 1, player: 'Player A', points: 1500 },
      { rank: 2, player: 'Player D', points: 1450 },
    ],
  };

  return (
    <Box sx={{ padding: 2 }}>
    {/* Card for Recent Match Results */}
    <Card sx={{ marginBottom: 2 }}>
      <CardContent>
        <Typography variant="h4">{tournament.name} - Recent Match Results</Typography>
        <Divider sx={{ my: 2 }} />
        
        {tournament.matches.length > 0 ? (
          tournament.matches.map((match) => (
            <Box key={match.id} sx={{ marginBottom: 2 }}>
              <Typography variant="body1">{match.player1} vs {match.player2}</Typography>
              <Typography variant="body2">Result: {match.result}</Typography>
            </Box>
          ))
        ) : (
          <Typography variant="body2">No recent matches available.</Typography>
        )}
      </CardContent>
    </Card>

    {/* Card for Tournament Rounds */}
    <Card  sx={{ marginBottom: 2 }}>
      <CardContent>
        <Typography variant="h4">Tournament Bracket</Typography>
        <Divider sx={{ my: 2 }} />
        
        <Grid container spacing={2} sx={{ marginTop: 2 }}>
          {tournament.bracket.map((round, index) => (
            <Grid item xs={12} key={index}>
              <Typography variant="h6">Round {index + 1}</Typography>
              <Grid container spacing={2} justifyContent="center">
                {round.map((match) => (
                  <Grid item xs={2} key={match.id}>
                    <Box
                      sx={{
                        padding: 1,
                        border: '1px solid #ccc',
                        borderRadius: 1,
                        textAlign: 'center',
                      }}
                    >
                      <Typography variant="body1">{match.player1}</Typography>
                      <Typography variant="body2">vs</Typography>
                      <Typography variant="body1">{match.player2}</Typography>
                      <Typography variant="body2">Result: {match.result}</Typography>
                    </Box>
                  </Grid>
                ))}
              </Grid>
            </Grid>
          ))}
        </Grid>
      </CardContent>
    </Card>
           {/* Card for Tournament Rankings */}
      <Card>
        <CardContent>
          <Typography variant="h4">Tournament Rankings</Typography>
          <Divider sx={{ my: 2 }} />
          
          <Grid container spacing={2} >
            {tournament.leaderboard.map((entry) => (
              <Grid item xs={15} key={entry.rank}>
                <Box
                  sx={{
                    padding: 1,
                    backgroundColor: '#f5f5f5',
                    borderRadius: 1,
                    textAlign: 'center',
                  }}
                >
                  <Typography variant="h6">#{entry.rank}</Typography>
                  <Typography variant="body1">{entry.player}</Typography>
                  <Typography variant="body2">Points: {entry.points}</Typography>
                </Box>
              </Grid>
            ))}
          </Grid>
        </CardContent>
      </Card>

  </Box>

  );
}

export default TournamentDetails;
