import React, { useState, useEffect } from 'react';
import { Bracket, Seed, SeedItem, SeedTeam } from 'react-brackets';
import { Typography, Box, Button, IconButton, Dialog, Select, MenuItem, DialogTitle, DialogContent, DialogActions, Chip } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import axios from 'axios';
import { useParams } from 'react-router-dom';

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
                        <SeedTeam style={{ color: 'green', backgroundColor: 'white' }}>
                            
                            <Typography variant="header3" component="span" style={{ color: winnerId === seed.teams[0]?.id ? 'green' : 'black' }}>
                            Auto Advance PLAYER {winnerId}
                                </Typography>
                        </SeedTeam>
                    ) : (
                        <>
                            <SeedTeam
                                style={{
                                    color: winnerId === seed.teams[0]?.id ? 'green' : 'black',
                                    backgroundColor: 'white'
                                }}
                            >
                                <Typography variant="header3" component="span" style={{ color: winnerId === seed.teams[0]?.id ? 'green' : 'black' }}>
                                    {seed.teams[0]?.name || 'Pending'}
                                </Typography>
                            </SeedTeam>
                            <SeedTeam
                                style={{
                                    color: winnerId === seed.teams[1]?.id ? 'green' : 'black',
                                    backgroundColor: 'white',
                                }}
                            >
                                <Typography variant="header3" component="span" style={{ color: winnerId === seed.teams[0]?.id ? 'green' : 'black' }}>
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

    const statusColorMap = {
        LIVE: 'success',
        UPCOMING: 'warning',
        EXPIRED: 'default',
    };

    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(`${baseURL}/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setTournament(response.data);

                const matchesResponse = await axios.get(`${baseURL2}/tournament/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                const formattedRounds = formatRounds(matchesResponse.data);
                setRounds(formattedRounds);

            } catch (error) {
                console.error('Error fetching tournament details:', error);
            }
        };

        fetchTournamentDetails();
    }, [id]);

    const formatRounds = (matches) => {
        const groupedMatches = matches.reduce((acc, match) => {
            const round = match.roundType.roundName;
            if (!acc[round]) acc[round] = [];
            acc[round].push({
                id: match.id,
                winnerId: match.winnerId,
                teams: [
                    { id: match.player1Id, name: match.player1Id ? `Player ${match.player1Id}` : null },
                    { id: match.player2Id, name: match.player2Id ? `Player ${match.player2Id}` : null },
                ],
            });
            return acc;
        }, {});

        return Object.keys(groupedMatches).map((round) => ({
            title: round,
            seeds: groupedMatches[round],
        }));
    };




    const handleEditWinner = (matchId, teams) => {
        setSelectedMatchId(matchId); // Store match id
        setSelectedTeams(teams); // Store teams for the selected match
        setWinner(''); // Reset winner selection
        setOpen(true);
    };

    const handleWinnerChange = (newWinnerId) => {
        setWinner(newWinnerId);
    };

    const handleClose = () => {
        setOpen(false); // Close modal without saving
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



    return (
        <Box sx={{ padding: 2 }}>
            {/* Tournament Header */}
            <Box sx={{ display: 'flex', alignItems: 'center', marginBottom: '30px' }}>
                <Typography variant="header1" sx={{ display: 'inline-flex' }}>{tournament.name}</Typography>
                <Chip label={tournament.status} color="success" sx={{ marginLeft: '10px' }} />
                <Button variant="contained" color="primary" onClick={handleStart} disabled={tournament.status !== 'UPCOMING'} sx={{ marginLeft: '10px' }}>
                    Start Tournament
                </Button>
            </Box>





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
            <Dialog open={open} onClose={handleClose}>
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
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleSaveWinner} color="primary">Save</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}

export default AdminTournamentDetails;
