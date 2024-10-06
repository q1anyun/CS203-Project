import React, { useState, useEffect } from 'react';
import { Card, CardContent, Typography, Box, Divider, Grid, Avatar } from '@mui/material';
import { useParams } from 'react-router-dom';
const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const baseURL2 = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;
import axios from 'axios';
import { useNavigate } from 'react-router-dom';


function TournamentDetails() {
  const { id } = useParams();
  const [tournament, setTournament] = useState({});
  const [matches, setMatches] = useState([]);
  const navigate = useNavigate();

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



  return (
    <Box sx={{ padding: 2 }}>
      {/* Tournament Title */}
      <Typography variant="h1" gutterBottom sx={{ textAlign: 'left', fontSize: '40px', marginBottom: '30px', fontWeight: 'bold', marginLeft: '20px' }}>
        {tournament.name}
      </Typography>
      <Typography variant="h1" gutterBottom sx={{ textAlign: 'left', fontSize: '20px', marginBottom: '30px', fontWeight: 'regular', marginLeft: '20px' }}>
        Description of the event
      </Typography>


      {/* Card for Tournament Rounds */}
      <Card sx={{ marginBottom: 2 }}>
        <CardContent>

          {Object.entries(matchesByRound).map(([roundName, matches]) => (
            <Box key={roundName} sx={{ marginBottom: 3 }}>
              <Typography variant="h1" gutterBottom sx={{ textAlign: 'left', fontSize: '30px', marginBottom: '30px', fontWeight: 'bold', marginLeft: '20px' }}>
                {roundName}
                <span style={{ fontSize: '15px', fontWeight: 'medium', marginLeft: '10px', letterSpacing: '0.5px' }}>Tournament Bracket</span></Typography>

              <Grid container spacing={2} marginLeft='5px'>
                {matches.map((match) => (
                  <Grid item xs={4} key={match.id}>
                    <Box
                      sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        p: 2, // padding: 20px, using theme spacing
                        backgroundColor: 'background.paper', // use theme color
                        border: '1px solid',
                        borderColor: 'divider', // use theme divider color
                        borderRadius: 2, // borderRadius: 5px, using theme spacing
                      }}
                    >
                      {/* Box for Date of the Event at the Top */}
                      <Box sx={{ mb: 2 }}> {/* marginBottom: 20px, using theme spacing */}
                        <Typography
                          variant="h1"
                          sx={{
                            textAlign: 'left',
                            fontSize: '10px',
                            fontWeight: 'light',
                          }}
                        >
                          Date of the Event
                        </Typography>
                      </Box>

                      {/* Flexbox for Players and Winner */}
                      <Box sx={{ display: 'flex', alignItems: 'flex-start' }}>
                        {/* Left Column for Players */}
                        <Box sx={{ textAlign: 'left', alignItems: 'flex-start' }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
                            <Avatar alt={`Player ${match.player1Id}`} src={`/path/to/avatar/${match.player1Id}.jpg`} sx={{ mr: 1 }} />
                            <Typography variant="h1" sx={{ fontWeight: 'bold', fontSize: '20px' }}>
                              Player {match.player1Id}
                            </Typography>
                          </Box>
                          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                            <Avatar alt={`Player ${match.player2Id}`} src={`/path/to/avatar/${match.player2Id}.jpg`} sx={{ mr: 1 }} />
                            <Typography variant="h1" sx={{ fontWeight: 'bold', fontSize: '20px' }}>
                              Player {match.player2Id}
                            </Typography>
                          </Box>
                        </Box>

                        {/* Divider */}
                        <Divider orientation="vertical" sx={{ height: '100px', mx: 5 }} /> {/* marginLeft: 80px, marginRight: 40px */}

                        {/* Right Column for Winner */}
                        <Box sx={{ flexShrink: 0, textAlign: 'center' }}>
                          <Typography variant="h1" sx={{ fontWeight: 'light', mb: 1, fontSize: '15px' }}>
                            Winner:
                          </Typography>
                          <Box sx={{ mb: 2 }}>
                            <Avatar
                              alt={`Winner ${match.winnerId}`}
                              src={match.winnerId ? `/path/to/avatar/${match.winnerId}.jpg` : '/path/to/default-avatar.jpg'} // Fallback avatar
                              sx={{ width: 56, height: 56, justifyContent: 'center' }} // Adjust size as needed
                            />
                          </Box>
                          <Typography variant="h1" sx={{ fontWeight: 'bold', mb: 3, fontSize: '20px' }}>
                            {match.winnerId ? `Player ${match.winnerId}` : 'Pending'}
                          </Typography>
                        </Box>
                      </Box>
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
