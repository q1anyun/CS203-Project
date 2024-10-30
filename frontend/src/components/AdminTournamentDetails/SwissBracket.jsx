import React, { useState, useEffect } from 'react';
import { Card, CardContent, Typography, Box, Divider, Grid, Avatar, Tab, Tabs, IconButton, MenuItem, Select, Dialog, DialogContent, DialogActions, DialogTitle, Button } from '@mui/material';
import axios from 'axios';
import EditIcon from '@mui/icons-material/Edit';
import Knockout from './Knockout';

const baseURL = import.meta.env.VITE_TOURNAMENT_SWISSBRACKET_URL;
const baseURL2 = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;

function SwissBracket({ matches, SwissBracketID }) {
    const [swissRoundDetails, setSwissRoundDetails] = useState([]);
    const [tabValue, setTabValue] = useState('swiss');
    const [editMatchId, setEditMatchId] = useState(null);
    const [selectedWinner, setSelectedWinner] = useState({});
    const [swissMatches, setSwissMatches] = useState([]);
    const [knockoutMatches, setKnockoutMatches] = useState([]);
    const [groupedRounds, setGroupedRounds] = useState([]);
    const [selectedTeams, setSelectedTeams] = useState([]); // Store selected match teams
    const [winner, setWinner] = useState('');
    const [open, setOpen] = useState(false);

    useEffect(() => {
        const fetchSwissBracket = async () => {
            const response = await axios.get(`${baseURL}/${SwissBracketID}`);
            setSwissRoundDetails(response.data);
        };

        // Check for a saved tab state in local storage
        const lastTab = localStorage.getItem('lastTab');
        if (lastTab) {
            setTabValue(lastTab);
            localStorage.removeItem('lastTab'); // Clear the saved state so it defaults on future loads
        }

        fetchSwissBracket();
    }, [SwissBracketID]);

    useEffect(() => {
        // Separate the matches into Swiss and Knockout based on a distinguishing property
        const swiss = matches.filter(match => match.swissRoundNumber !== null);
        const knockout = matches.filter(match => match.swissRoundNumber === null);
        setSwissMatches(swiss);
        setKnockoutMatches(knockout);
    }, [matches]);


    useEffect(() => {
        const groupMatchesByRound = () => {
            const grouped = knockoutMatches.reduce((acc, match) => {
                if (!match || !match.roundType) return acc;
                const round = match.roundType.roundName;
                if (!acc[round]) {
                    acc[round] = [];
                }
                acc[round].push({
                    id: match.id,
                    winnerId: match.winnerId,
                    teams: [
                        { id: match.player1 ? match.player1.id : 0, name: match.player1 ? match.player1.firstName : "Pending" },
                        { id: match.player2 ? match.player2.id : 0, name: match.player2 ? match.player2.firstName : "Pending" }
                    ],
                });
                return acc;
            }, {});

            return Object.keys(grouped).map(round => ({
                title: round,
                seeds: grouped[round],
            }));
        };

        const formattedRounds = groupMatchesByRound();
        setGroupedRounds(formattedRounds);
    }, [matches]);

    const handleEditClick = (matchId) => {
        setEditMatchId(matchId);
    };
    const handleEditWinner2 = (matchId, teams) => {
        setEditMatchId(matchId);
        setSelectedTeams(teams);
        setOpen(true);
    };

    const handleCloseEdit = () => {
        setEditMatchId(null);
        setOpen(false);
    };

    const handleWinnerChange = (matchId, value) => {
        setSelectedWinner({ ...selectedWinner, [matchId]: value });
    };

    const handleSaveWinner = async () => {
        const winnerId = selectedWinner[editMatchId];
        try {
            await axios.put(`${baseURL2}/${editMatchId}/winner/${winner}`, {
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            });
            const updatedMatches = matches.map(match => {
                if (match.id === editMatchId) {
                    return { ...match, winnerId: winnerId };
                }
                return match;
            });
            matches = updatedMatches; // Update your state or context here accordingly
            handleCloseEdit();
            window.location.reload();

        } catch (error) {
            console.error('Error updating the winner:', error);
        }
    };
    const handleSaveWinner2 = async () => {
        try {
            await axios.put(`${baseURL2}/${editMatchId}/winner/${winner}`, {
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            });
            handleCloseEdit();
            localStorage.setItem('lastTab', 'knockout');  // Save the tab state
            window.location.reload();

        } catch (error) {
            console.error('Error updating the winner:', error);
        }
    };

    const matchesByRound = swissMatches.reduce((acc, match) => {
        const roundNumber = `Round ${match.swissRoundNumber || 'Unknown'}`;
        acc[roundNumber] = acc[roundNumber] || [];
        acc[roundNumber].push(match);
        return acc;
    }, {});

    return (
        <Box sx={{ padding: 2 }}>

            <Tabs value={tabValue} onChange={(e, newValue) => setTabValue(newValue)} textColor="primary" indicatorColor="primary">
                <Tab label="Swiss" value="swiss" />
                <Tab label="Knockout" value="knockout" />
            </Tabs>

            <Divider sx={{ my: 1 }} />

            {tabValue === 'swiss' && Object.keys(matchesByRound).length > 0 ? (
                Object.entries(matchesByRound).slice().reverse().map(([roundName, roundMatches]) => (
                    <Box key={roundName} sx={{
                        marginBottom: 2,
                        border: (roundName === `Round ${swissRoundDetails.currentRound}`) ? '4px solid black' : '1px solid white',
                        borderRadius: '4px',
                        backgroundColor: (roundName === `Round ${swissRoundDetails.currentRound}`) ? '#f0f0f0' : 'white'
                    }}>
                        <CardContent>
                            <Typography variant="header3">
                                {roundName === `Round ${swissRoundDetails.currentRound}`
                                    ? `${roundName} - Current Round`
                                    : roundName}
                            </Typography>

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
                                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 4,  backgroundColor: match.winnerId === match.player1.id ? 'green' : 'background.paper', // Conditional background
                                                    p: 1, borderRadius: 1 }}>
                                                <Avatar
                                                    alt={`${match.player1?.firstName}`}
                                                    src={`/path/to/profile_picture/player_${match.player1.id}.jpg`}
                                                    sx={{ mr: 1 }}
                                                />
                                                
                                                    <Typography variant="playerProfile2" style={{ color: match.winnerId === match.player1.id ? 'white' : 'black' }}>{match.player1?.firstName}</Typography>
                                                
                                            </Box>

                                            <Box sx={{ 
                                                display: 'flex', 
                                                alignItems: 'center', 
                                                mb: 2,
                                                backgroundColor: match.winnerId === match.player2.id ? 'lightgreen' : 'background.paper', // Conditional background
                                                p: 1,
                                                borderRadius: 1, 
                                            }}>
                                                <Avatar
                                                    alt={`${match.player2.firstName}`}
                                                    src={`/path/to/profile_picture/player_${match.player2Id}.jpg`}
                                                    sx={{ mr: 1 }}
                                                />
                                               
                                                    <Typography variant="playerProfile2" style={{ color: match.winnerId === match.player2.id ? 'white' : 'black' }}>{match.player2.firstName}</Typography>
                                                
                                            </Box>
                                           
                                            <IconButton onClick={() => handleEditClick(match.id)} size="small" sx={{ ml: 'auto' }}>
                                                <EditIcon />
                                            </IconButton>
                                        </Box>
                                        <Dialog open={editMatchId === match.id} onClose={handleCloseEdit} >
                                            <DialogTitle>Edit Winner</DialogTitle>
                                            <DialogContent>
                                                <Select
                                                    value={selectedWinner[match.id] || ''}
                                                    onChange={(e) => handleWinnerChange(match.id, e.target.value)}
                                                    fullWidth
                                                >
                                                    <MenuItem value={match.player1.id}>{match.player1.firstName}</MenuItem>
                                                    <MenuItem value={match.player2.id}>{match.player2.firstName}</MenuItem>
                                                </Select>
                                            </DialogContent>
                                            <DialogActions>
                                                <Button onClick={handleCloseEdit}>Cancel</Button>
                                                <Button onClick={handleSaveWinner} color="primary">Save</Button>
                                            </DialogActions>
                                        </Dialog>
                                    </Grid>
                                ))}
                            </Grid>
                        </CardContent>
                    </Box>
                ))
            ) : null
            }

            {tabValue === 'knockout' && (
                <>
                    {knockoutMatches.length > 0 ? (
                        <Knockout
                            rounds={groupedRounds}
                            handleEditWinner={handleEditWinner2}
                            winner={winner}
                            setWinner={setWinner}
                            selectedTeams={selectedTeams}
                            open={open}
                            handleCloseEdit={handleCloseEdit}
                            handleSaveWinner={handleSaveWinner2}
                        />
                    ) : (
                        <Typography variant="header3" align="center">Knockout rounds coming soon.</Typography>
                    )}
                </>
            )}
        </Box>
    );
}

export default SwissBracket;
