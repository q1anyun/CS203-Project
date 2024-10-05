import React, { useState, useEffect } from 'react';
import { Card, CardContent, Typography, Box, Divider, Grid } from '@mui/material';
import { useParams } from 'react-router-dom';
const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const baseURL2 = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;
import axios from 'axios';


function TournamentDetails() {
  const {id} = useParams(); 
  const [tournament, setTournament] = useState({}); 
  const [matches, setMatches] = useState([]);

  const token = localStorage.getItem('token');
  console.log(id); 

  useEffect(() => {
    const fetchTournamentDetails = async () => {
        try {
            const response = await axios.get(`${baseURL}/${id}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            setTournament(response.data);
            console.log(response.data); 
            const matchesResponse = await axios.get(`${baseURL2}/tournament/${id}`, {
              headers: {
                  'Authorization': `Bearer ${token}`,
              },
          });
          console.log('Tournament Matches:', matchesResponse.data);
          setMatches(matchesResponse.data); // Set matches to state

        } catch (error) {
            console.error('Error fetching tournament details:', error);
        }
    };

    fetchTournamentDetails();
}, [id, token]);

const matchesByRound = matches.reduce((acc, match) => {
  const roundName = match.roundType.roundName;
  if (!acc[roundName]) {
    acc[roundName] = [];
  }
  acc[roundName].push(match);
  return acc;
}, {});

  // Hardcoded tournament details
  // const tournament = {
  //   id: 1,
  //   name: 'Singapore Open',
  //   matches: [
  //     { id: 1, player1: 'Player A', player2: 'Player B', result: 'Player A won' },
  //     { id: 2, player1: 'Player C', player2: 'Player D', result: 'Player D won' },
  //   ],
  //   bracket: [
  //     // Round 1
  //     [
  //       { id: 1, player1: 'Player A', player2: 'Player B', result: 'Player A won' },
  //       { id: 2, player1: 'Player C', player2: 'Player D', result: 'Player D won' },
  //     ],
  //     // Round 2
  //     [
  //       { id: 3, player1: 'Player A', player2: 'Player C', result: 'TBD' },
  //       { id: 4, player1: 'Player B', player2: 'Player D', result: 'TBD' },
  //     ],
  //     // Add more rounds as needed
  //   ],
  //   leaderboard: [
  //     { rank: 1, player: 'Player A', points: 1500 },
  //     { rank: 2, player: 'Player D', points: 1450 },
  //   ],
  // };

  return (
    <Box sx={{ padding: 2 }}>
    {/* Card for Recent Match Results */}
    <Card sx={{ marginBottom: 2 }}>
      <CardContent>
        <Typography variant="h4">{tournament.name} - Match Results</Typography>
        <Divider sx={{ my: 2 }} />
        
        {matches.length > 0 ? (
          matches.map((match) => (
            <Box key={match.id} sx={{ marginBottom: 2 }}>
              <Typography variant="body1">{match.player1Id} vs {match.player2Id}</Typography>
              <Typography variant="body2">Result: {match.winnerId}</Typography>
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
        
        {Object.entries(matchesByRound).map(([roundName, matches]) => (
            <Box key={roundName} sx={{ marginBottom: 3 }}>
              <Typography variant="h6">{roundName}</Typography>
              <Grid container spacing={2} justifyContent="center">
                {matches.map((match) => (
                  <Grid item xs={5} key={match.id}>
                    <Box
                      sx={{
                        padding: 1,
                        border: '1px solid #ccc',
                        borderRadius: 1,
                        textAlign: 'center',
                      }}
                    >
                      <Typography variant="body1">Player {match.player1Id}</Typography>
                      <Typography variant="body2">vs</Typography>
                      <Typography variant="body1">Player {match.player2Id}</Typography>
                      <Typography variant="body2">
                        Result: {match.winnerId ? `Player ${match.winnerId}` : 'Pending'}
                      </Typography>
                    </Box>
                  </Grid>
                ))}
              </Grid>
            </Box>
          ))}
      </CardContent>
    </Card>
           {/* Card for Tournament Rankings */}
      <Card>
        <CardContent>
          <Typography variant="h4">Tournament Rankings</Typography>
          <Divider sx={{ my: 2 }} />
{/*           
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
          </Grid> */}
        </CardContent>
      </Card>

  </Box>

  );
}

export default TournamentDetails;
