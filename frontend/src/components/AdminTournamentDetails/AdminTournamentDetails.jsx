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

const CustomSeed = ({ seed, handleEditWinner }) => {
    const winnerId = seed.winnerId;

    // Check if both player1Id and player2Id are null and winnerId exists (auto-advance case)
    const isAutoAdvance = !seed.teams[0]?.id && !seed.teams[1]?.id && winnerId !== null;

    return (
        <Seed style={{ fontSize: 20, justifyContent: 'center', alignItems: 'center' }}>
            <SeedItem>
                <div>
                    {isAutoAdvance ? (
                        <SeedTeam style={{  backgroundColor: 'green' }}>

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
                                
                                <Typography variant="playerProfile2" component="span" style={{color: winnerId == seed.teams[0]?.id ? 'white' : 'black' }}>
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

                            <IconButton onClick={() => handleEditWinner(seed.id, seed.teams)} aria-label="edit winner" sx={{ color: 'white' }}>
                                <EditIcon />
                            </IconButton>
                        </>
                    )}
                </div>
            </SeedItem>
        </Seed>
    );
};

function AdminTournamentDetails() {
    const { id } = useParams();
    const [tournament, setTournament] = useState({});
    const [rounds, setRounds] = useState([]);
    const [selectedMatchId, setSelectedMatchId] = useState(null);
    const [selectedTeams, setSelectedTeams] = useState([]); // Store selected match teams
    const [winner, setWinner] = useState('');
    const [open, setOpen] = useState(false);

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
                    { id: match.player1? match.player1.id : 0 , name: match.player1? match.player1.firstName : "Pending" },  // Provide fallback for firstName
                     {id: match.player2? match.player2.id : 0, name : match.player2? match.player2.firstName : "Pending" }  // Provide fallback for firstName
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

    const handleWinnerChange = (winnerId) => {
        setWinner(winnerId);  // Update the state with the new winner's ID
    };
    


    const handleEditWinner = (matchId, teams) => {
        handleOpenEdit(); 
        setSelectedMatchId(matchId); // Store match id
        console.log("the button is clicked ")
        setSelectedTeams(teams); // Store teams for the selected match

    };

    const handleSaveWinner = async () => {
        console.log("start button clicked");
        try {
            await axios.put(`${baseURL2}/${selectedMatchId}/winner/${winner}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            setOpen(false); // Close modal after submission
            window.location.reload(); // Reload to show updated data
        } catch (error) {
            console.error('Error updating the winner:', error);
        }
    };

    const handleStart = async () => {
        if (tournament.currentPlayers < 2) {
            alert("Not enough players to start the tournament. Minimum required: 2");
            return;
        }

        try {
            const response = await axios.post(`${baseURL}/start/${tournament.id}`, null, {
                headers: { Authorization: `Bearer ${token}` },
            });

            if (response.status === 200) {
                alert("Tournament started successfully!");
                window.location.reload();
            }
        } catch (error) {
            console.error("Error starting the tournament:", error);
            alert("Failed to start the tournament.");
        }
    };
    const handleCloseEdit = () => {
        setOpen(false);
    };

    const handleOpenEdit = () => {
        setOpen(true);
    };


    return (
        <Box sx={{ padding: 2 }}>
          
            <TournamentDescription tournament={tournament} handleStart={handleStart} />
   
            {/* Divider added here */}
            <Typography variant="header2" marginLeft={'20px'} >Tournament Bracket</Typography>
            <Button variant="contained" color="primary" sx={{ marginLeft: '10px' }} onClick={() => navigate(`/admin/tournaments/${tournament.id}/leaderboard`)}>
                       <Typography variant="body4" >Check Leaderboard</Typography>
                </Button>
            <Divider sx={{ width: '80%', margin: '20px 0' }} />
          


            {/* Tournament Bracket */}
            <Bracket
                rounds={rounds}
                renderSeedComponent={(props) => (
                    <CustomSeed
                        {...props}
                        handleEditWinner={handleEditWinner}
                    />
                )}
                roundTitleComponent={(title) => (
                    <Typography variant="header3" align="center" >
                        {title}
                    </Typography>
                )}
            />

            {/* Modal for editing winner */}
            <Dialog open={open} onClose={handleCloseEdit}>
                <DialogTitle>Edit Winner</DialogTitle>
                <DialogContent>
                    <Select value={winner} onChange={(e) => handleWinnerChange(e.target.value)}
                        sx={{
                            width: '300px',
                            height: '50px',
                            fontSize: '18px',
                            padding: '10px',
                        }}>
                        <MenuItem value={selectedTeams[0]?.id}>{selectedTeams[0]?.name}</MenuItem>
                        <MenuItem value={selectedTeams[1]?.id}>{selectedTeams[1]?.name}</MenuItem>
                    </Select>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseEdit}>Cancel</Button>
                    <Button onClick={handleSaveWinner} color="primary">Save</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}

export default AdminTournamentDetails;
