import React, { useState, useEffect } from 'react';
import { Bracket, Seed, SeedItem, SeedTeam } from 'react-brackets';
import { Typography, Box, Button, IconButton, Dialog, Select, MenuItem, DialogTitle, DialogContent, DialogActions, Chip, Divider } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import TournamentDescription from './TournamentDescription';
import { useNavigate } from 'react-router-dom';

const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const baseURL2 = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;

const CustomSeed = ({ seed }) => {
    const winnerId = seed.winnerId;

    // Check if both player1Id and player2Id are null and winnerId exists (auto-advance case)
    const isAutoAdvance = !seed.teams[0]?.id && !seed.teams[1]?.id && winnerId !== null;

    return (
        <Seed style={{ fontSize: 20, justifyContent: 'center', alignItems: 'center', color:'white'}}>
            <SeedItem>
                <div>
                    {isAutoAdvance ? (
                        <SeedTeam style={{ backgroundColor: 'green' }}>

                            <Typography variant="header3" component="span" style={{ color: 'white' }}>
                                Auto Advance PLAYER {winnerId}
                            </Typography>
                        </SeedTeam>
                    ) : (
                        <>
                            <SeedTeam
                                style={{

                                    backgroundColor: winnerId == seed.teams[0]?.id ? 'green' : 'white'
                                }}
                            >

                                <Typography variant="playerProfile2" component="span" style={{ color: winnerId == seed.teams[0]?.id ? 'white' : 'black' }}>
                                    {seed.teams[0]?.name || 'Pending'}
                                </Typography>
                            </SeedTeam>
                            <SeedTeam
                                style={{

                                    backgroundColor: winnerId == seed.teams[1]?.id ? 'green' : 'white'
                                }}
                            >
                                <Typography variant="playerProfile2" component="span" style={{ color: winnerId == seed.teams[1]?.id ? 'white' : 'black' }}>
                                    {seed.teams[1]?.name || 'Pending'}
                                </Typography>
                            </SeedTeam>

                           
                        </>
                    )}
                </div>
            </SeedItem>
        </Seed>
    );
};

function TournamentDetails() {
    const { id } = useParams();
    const [tournament, setTournament] = useState({});
    const [rounds, setRounds] = useState([]);
    const [selectedTeams, setSelectedTeams] = useState([]); // Store selected match teams


    

    const statusColorMap = {
        LIVE: 'success',
        UPCOMING: 'warning',
        EXPIRED: 'default',
    };

    const token = localStorage.getItem('token');
    const navigate = useNavigate();

    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(`${baseURL}/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setTournament(response.data);
                console.log(response.data);

                const matchesResponse = await axios.get(`${baseURL2}/tournament/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                console.log(matchesResponse.data);

                const formattedRounds = formatRounds(matchesResponse.data);
                setRounds(formattedRounds);

            } catch (error) {
                console.error('Error fetching tournament details:', error);
            }
        };

        fetchTournamentDetails();
    }, [id]);


    const formatRounds = (matches) => {

        const groupedMatches = matches.reduce((acc, match, index) => {

            // Check if match and roundType are not null before accessing roundName
            if (!match || !match.roundType) {
                return acc;  // Skip this match if roundType is undefined or null
            }

            const round = match.roundType.roundName;
            if (!acc[round]) {
                acc[round] = [];
            }

            acc[round].push({
                id: match.id,
                winnerId: match.winnerId,
                teams: [
                    { id: match.player1 ? match.player1.id : 0, name: match.player1 ? match.player1.firstName : "Pending" },  // Provide fallback for firstName
                    { id: match.player2 ? match.player2.id : 0, name: match.player2 ? match.player2.firstName : "Pending" }  // Provide fallback for firstName
                ],
            });


            return acc;
        }, {});


        const formattedRounds = Object.keys(groupedMatches).map((round) => ({
            title: round,
            seeds: groupedMatches[round],
        }));



        return formattedRounds;
    };

   


    return (
        <Box sx={{ padding: 2 }}>
          
            <TournamentDescription tournament={tournament} />
   
            {/* Divider added here */}
            <Typography variant="header2" marginLeft={'20px'} >Tournament Bracket</Typography>
            <Button variant="contained" color="primary" sx={{ marginLeft: '10px' }} onClick={() => navigate(`/player/tournaments/leaderboard/${tournament.id}`)}>
                       <Typography variant="body4" >Check Leaderboard</Typography>
                </Button>

                       
                <Divider sx={{ width: '80%', margin: '20px 0' }} />

                {/* Tournament Bracket */}
                <Bracket
                    rounds={rounds}
                    renderSeedComponent={(props) => (
                        <CustomSeed
                            {...props}
                        />
                    )}
                    roundTitleComponent={(title) => (
                        <Typography variant="header3" align="center">
                            {title}
                        </Typography>
                    )}
                />

              
            </Box>
            
        
    );

}
    export default TournamentDetails;
