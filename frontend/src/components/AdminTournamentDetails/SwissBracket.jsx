import React, { useState, useEffect } from 'react';
import { CardContent, Typography, Box, Divider, Grid, Avatar, Tab, Tabs, IconButton, MenuItem, Select, Dialog, DialogContent, DialogActions, DialogTitle, Button } from '@mui/material';
import axios from 'axios';
import EditIcon from '@mui/icons-material/Edit';
import Knockout from './Knockout';
import { useNavigate } from 'react-router-dom';
import defaultProfilePic from '../../assets/default_user.png';
import SwissStandings from './SwissStandings';

const swissBracketURL = import.meta.env.VITE_TOURNAMENT_SWISSBRACKET_URL;
const matchmakingURL = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;
const playerURL = import.meta.env.VITE_PLAYER_SERVICE_URL;
const swissStandingURL = import.meta.env.VITE_TOURNAMENT_SWISSSTANDING_URL; 

function SwissBracket({ matches, SwissBracketID }) {
    const [swissRoundDetails, setSwissRoundDetails] = useState([]);
    const [tabValue, setTabValue] = useState(localStorage.getItem('lastTab') || 'swiss');
    const [editMatchId, setEditMatchId] = useState(null);
    const [selectedWinner, setSelectedWinner] = useState({});
    const [swissMatches, setSwissMatches] = useState([]);
    const [knockoutMatches, setKnockoutMatches] = useState([]);
    const [groupedRounds, setGroupedRounds] = useState([]);
    const [playersWithPhotos, setPlayersWithPhotos] = useState({});
    const [swissStandings, setSwissStandings] = useState([{}]); 

    const navigate = useNavigate();

    useEffect(() => {
        const fetchSwissBracket = async () => {
            const response = await axios.get(`${swissBracketURL}/${SwissBracketID}`);
            setSwissRoundDetails(response.data);
        };
        fetchSwissBracket();
    }, [SwissBracketID]);

    
    useEffect(() => {
        const fetchSwissStandings = async () => {
            console.log(`${swissStandingURL}/${SwissBracketID}`); 
            const response = await axios.get(`${swissStandingURL}/${SwissBracketID}`);
            console.log(response.data);
            setSwissStandings(response.data);
            console.log( swissStandings);
           
        };
        fetchSwissStandings();
    }, [SwissBracketID]);

    useEffect(() => {
        const swiss = matches.filter(match => match.swissRoundNumber !== null);
        const knockout = matches.filter(match => match.swissRoundNumber === null);
        setSwissMatches(swiss);
        setKnockoutMatches(knockout);
    }, [matches]);


    useEffect(() => {
        const fetchProfilePhotos = async (matches) => {
            const allPlayers = matches.reduce((acc, match) => {
                if (match.player1) acc.push(match.player1);
                if (match.player2) acc.push(match.player2);
                return acc;
            }, []);

            const playersWithPhotos = await attachProfilePhotos(allPlayers);
            setPlayersWithPhotos(playersWithPhotos.reduce((acc, player) => {
                acc[player.id] = player.profilePhoto;
                return acc;
            }, {}));
        };

        fetchProfilePhotos([...swissMatches, ...knockoutMatches]);
    }, [swissMatches, knockoutMatches]);

    const attachProfilePhotos = async (players) => {
        const token = localStorage.getItem('token');
        return await Promise.all(
            players.map(async (player) => {
                try {
                    const profilePictureResponse = await axios.get(`${playerURL}/photo/${player.id}`, {
                        headers: { Authorization: `Bearer ${token}` },
                        responseType: 'blob',
                    });
                    const imageUrl = URL.createObjectURL(profilePictureResponse.data);
                    return { ...player, profilePhoto: imageUrl };
                } catch {
                    // If photo fetch fails, add a default image or handle as needed
                    return { ...player, profilePhoto: defaultProfilePic };
                }
            })
        );
    };

    useEffect(() => {
        const groupMatchesByRound = () => {
            const grouped = knockoutMatches.reduce((acc, match) => {
                if (!match || !match.roundType) return acc;
                const round = match.roundType.roundName;
                if (!acc[round]) acc[round] = [];
                acc[round].push({
                    id: match.id,
                    winnerId: match.winnerId,
                    teams: [
                        { id: match.player1?.id || 0, name: match.player1 ? match.player1.firstName + " " + match.player1.lastName : "Pending" },
                        { id: match.player2?.id || 0, name: match.player2 ? match.player2.firstName + " " + match.player2.lastName : "Pending" }
                    ],
                });
                return acc;
            }, {});

            return Object.keys(grouped).map(round => ({
                title: round,
                seeds: grouped[round],
            }));
        };

        setGroupedRounds(groupMatchesByRound());
    }, [knockoutMatches]);

    const handleEditClick = (matchId) => {
        setEditMatchId(matchId);
    };

    const handleCloseEdit = () => {
        setEditMatchId(null);
    };

    const handleWinnerChange = (matchId, value) => {
        setSelectedWinner({ ...selectedWinner, [matchId]: value });
    };

    const handleSaveWinner = async () => {
        const winnerId = selectedWinner[editMatchId];
        try {
            await axios.put(`${matchmakingURL}/${editMatchId}/winner/${winnerId}`, {
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            });
            handleCloseEdit();
            window.location.reload();
        } catch (error) {
            if (error.response) {
                const statusCode = error.response.status;
                const errorMessage = error.response.data?.message || 'An unexpected error occurred';
                navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
            } else if (err.request) {
                navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
            } else {
                navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + err.message)}`);
            }
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
            <Tabs
                value={tabValue}
                onChange={(e, newValue) => {
                    setTabValue(newValue);
                    localStorage.setItem('lastTab', newValue);
                }}
                textColor="primary"
                indicatorColor="primary"
            >
                <Tab label="Swiss" value="swiss" />
                <Tab label="Swiss Standings" value="swiss standing" />
                <Tab label="Knockout" value="knockout" />
                
            </Tabs>

            <Divider sx={{ my: 1 }} />

            {tabValue === 'swiss' && Object.keys(matchesByRound).length > 0 && (
                <>
                    <Typography variant='header2'>Total Rounds: {swissRoundDetails.numberOfRounds}</Typography>
                    {Object.entries(matchesByRound).slice().reverse().map(([roundName, roundMatches]) => (
                        <Box
                            key={roundName}
                            sx={{
                                marginBottom: 2,
                                border: roundName === `Round ${swissRoundDetails.currentRound}` ? '4px solid black' : '1px solid white',
                                borderRadius: '4px',
                                backgroundColor: roundName === `Round ${swissRoundDetails.currentRound}` ? '#f0f0f0' : 'white'
                            }}
                        >

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
                                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 4, backgroundColor: match.winnerId === match.player1.id ? 'green' : 'background.paper', p: 1, borderRadius: 1 }}>
                                                    <Avatar
                                                        alt={match.player1?.firstName}
                                                        src={playersWithPhotos[match.player1?.id] || defaultProfilePic}
                                                        sx={{ mr: 1 }}
                                                    />
                                                    <Typography variant="body1" style={{ color: match.winnerId === match.player1.id ? 'white' : 'black' }}>
                                                        {`${match.player1?.firstName} ${match.player1?.lastName}`}
                                                    </Typography>
                                                </Box>

                                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, backgroundColor: match.winnerId === match.player2.id ? 'green' : 'background.paper', p: 1, borderRadius: 1 }}>
                                                    <Avatar
                                                        alt={match.player2?.firstName}
                                                        src={playersWithPhotos[match.player2?.id] || defaultProfilePic}
                                                        sx={{ mr: 1 }}
                                                    />
                                                    <Typography variant="body1" style={{ color: match.winnerId === match.player2.id ? 'white' : 'black' }}>
                                                        {`${match.player2?.firstName} ${match.player2?.lastName}`}
                                                    </Typography>
                                                </Box>

                                                <IconButton onClick={() => handleEditClick(match.id)} size="small" sx={{ ml: 'auto' }}
                                                    disabled={match.winnerId !== null}>
                                                    <EditIcon />
                                                </IconButton>
                                            </Box>

                                            <Dialog open={editMatchId === match.id} onClose={handleCloseEdit} >
                                                <DialogTitle>Edit Winner</DialogTitle>
                                                <DialogContent>
                                                    <Select
                                                        value={selectedWinner[match.id] || ''}
                                                        onChange={(e) => handleWinnerChange(match.id, e.target.value)}
                                                        sx={{ width: '300px', height: '50px', fontSize: '18px', padding: '10px' }}
                                                    >
                                                        <MenuItem value={match.player1?.id}>{match.player1.firstName + " " + match.player1.lastName}</MenuItem>
                                                        <MenuItem value={match.player2?.id}>{match.player2.firstName + " " + match.player2.lastName}</MenuItem>
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
                    ))}
                </>
            )}

            {tabValue === 'knockout' && (
                knockoutMatches.length > 0 ? (
                    <Knockout rounds={groupedRounds} />
                ) : (
                    <Typography variant="playerProfile2" align="center">Knockout rounds coming soon.</Typography>
                )
            )}

            {tabValue === 'swiss standing' && (
                 <SwissStandings 
                 swissStandings={swissStandings}
                 playersWithPhotos={playersWithPhotos}
               />
            )}
        </Box>
    );
}

export default SwissBracket;
