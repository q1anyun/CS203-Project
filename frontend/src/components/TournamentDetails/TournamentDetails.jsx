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
      <Typography variant="header1" gutterBottom >
        {tournament.name}
      </Typography>
      <Typography variant="body4" gutterBottom textAlign={'left'} marginLeft = '40px'  display="block">
        Description of the event
      </Typography>


      {/* Card for Tournament Rounds */}
      <Card sx={{ marginBottom: 2 }}>
        <CardContent>

          {Object.entries(matchesByRound).map(([roundName, matches]) => (
            <Box key={roundName} sx={{ marginBottom: 3 }}>
              <Typography variant="header2" gutterBottom >
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
                          variant="body4"
                          textAlign={'left'}
                        >
                          Date of the Event
                        </Typography>
                      </Box>

                      {/* Flexbox for Players and Winner */}
                      <Box sx={{ display: 'flex', alignItems: 'flex-start' }}>
                        {/* Left Column for Players */}
                        <Box sx={{ textAlign: 'left', alignItems: 'flex-start' }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
                            <Avatar alt={`Player ${match.player1Id}`} src={`../../../backend/player-service/profile-picture/player_${match.player1Id}.jpg`} sx={{ mr: 1 }} />
                            <Typography variant="header3" >
                              Player {match.player1Id}
                            </Typography>
                          </Box>
                          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                            <Avatar alt={`Player ${match.player2Id}`} src={`../../../backend/player-service/profile-picture/player_${match.player1Id}.jpg`} sx={{ mr: 1 }} />
                            <Typography variant="header3" >
                              Player {match.player2Id}
                            </Typography>
                          </Box>
                        </Box>

                        {/* Divider */}
                        <Divider orientation="vertical" sx={{ height: '100px', ml: 5, mr:8 }} /> {/* marginLeft: 80px, marginRight: 40px */}

                        {/* Right Column for Winner */}
                        <Box sx={{ flexShrink: 0, alignItems:'center' }}>
                          <Typography variant="body4">
                            Winner:
                          </Typography>
                          <Box sx={{ mb: 2 }}>
                            <Avatar
                              alt={`Winner ${match.winnerId}`}
                              src={match.winnerId ? `../../../backend/player-service/profile-picture/player_${match.player1Id}.jpg` : '/path/to/default-avatar.jpg'} // Fallback avatar
                              sx={{ width: 56, height: 56, justifyContent: 'center' , alignContent: 'center'}} // Adjust size as needed
                            />
                          </Box>
                          <Typography variant="header3">
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

    </Box>

  );
}

export default TournamentDetails;
