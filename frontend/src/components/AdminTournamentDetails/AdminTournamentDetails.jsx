import React, { useState, useEffect } from 'react';
import { Card, CardContent, Typography, Box, Divider, Grid, Avatar, Button, IconButton, Dialog, Select, MenuItem, DialogTitle, DialogContent, DialogActions, Chip, Snackbar } from '@mui/material';
import { useParams } from 'react-router-dom';
const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const baseURL2 = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import EditIcon from '@mui/icons-material/Edit';

function AdminTournamentDetails() {
    const { id } = useParams();
    const [tournament, setTournament] = useState({});
    const [matches, setMatches] = useState([]);
    const [selectedMatch, setSelectedMatch] = useState(null);
    const [winner, setWinner] = useState(''); // Holds the selected winner
    const [open, setOpen] = useState(false); // For controlling modal open state
    const [errorMessage, setErrorMessage] = useState('');
    const [showError, setShowError] = useState(false);

    const navigate = useNavigate();


    const statusColorMap = {
        LIVE: 'success',
        UPCOMING: 'warning',
        EXPIRED: 'default',
    };


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

    const handleClick = async () => {
        console.log('Start button clicked');

        if (tournament.currentPlayers < 2) {
            setErrorMessage(`Not enough players to start the tournament.\nMinimum required: 2\nCurrent players: ${tournament.currentPlayers}`);
            setShowError(true); // Open the Snackbar
            return; // Prevent the function from proceeding
        }
        try {
            const response = await axios.post(`${baseURL}/start/${tournament.id}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            window.location.reload();
        } catch (error) {
            console.error('Error making the API request:', error);
        }
    };

    const handleEditWinner = (match) => {
        setSelectedMatch(match);
        setWinner(''); // Reset winner selection
        setOpen(true);
    };

    const handleWinnerChange = (winnerId) => {
        setWinner(winnerId);
    };

    const handleSaveWinner = async () => {
        try {
            await axios.put(`${baseURL2}/${selectedMatch.id}/winner/${winner}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            setOpen(false); // Close modal after submission
            window.location.reload(); // Reload to show updated data
        } catch (error) {
            console.error('Error updating the winner:', error);
        }
    };

    const handleClose = () => {
        setOpen(false); // Close modal without saving
    };

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
            <Box display="flex" alignItems="center" justifyContent="space-between">
                {/* Box to stack name and description */}
                <Box display="flex" flexDirection="column">
                    <Box display="flex" alignItems="center" gap={2}>
                        <Typography variant="header1" gutterBottom>
                            {tournament.name}
                        </Typography>
                        <Chip label={tournament.status} color={statusColorMap[tournament.status]} />
                    </Box>
                    <Dialog open={showError} onClose={() => setShowError(false)}>
                        <DialogTitle variant= 'header3'>Error</DialogTitle>
                        <DialogContent variant ='body4' style={{ whiteSpace: 'pre-line' }}>
                            {errorMessage}
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={() => setShowError(false)}>Close</Button>
                        </DialogActions>
                    </Dialog>

                    {/* <Typography variant="body4" gutterBottom marginLeft={'20px'}>
                        Description of the event
                    </Typography> */}
                </Box>

                {/* Start button on the right */}
                <Button variant="contained" color="primary" size='medium' onClick={handleClick} disabled={tournament.status !== "UPCOMING"}>
                    Start
                </Button>
            </Box>

            {/* Card for Tournament Rounds */}
            {Object.keys(matchesByRound).length > 0 ? (

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
                                                <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                    <Typography
                                                        variant="body4"
                                                        textAlign={'left'}
                                                    >
                                                        Date of the Event
                                                    </Typography>
                                                    <IconButton
                                                        onClick={() => handleEditWinner(match)}
                                                        disabled={match.player1Id == null || match.player2Id == null}  // Disable if match.winnerId exists
                                                        aria-label="edit winner"
                                                    >
                                                        <EditIcon />
                                                    </IconButton>
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
                                                            <Avatar alt={`Player ${match.player2Id}`} src={`../../../backend/player-service/profile-picture/player_${match.player2Id}.jpg`} sx={{ mr: 1 }} />
                                                            <Typography variant="header3" >
                                                                Player {match.player2Id}
                                                            </Typography>
                                                        </Box>
                                                    </Box>

                                                    {/* Divider */}
                                                    <Divider orientation="vertical" sx={{ height: '100px', ml: 5, mr: 8 }} />

                                                    {/* Right Column for Winner */}
                                                    <Box sx={{ flexShrink: 0, alignItems: 'center' }}>
                                                        <Typography variant="body4">
                                                            Winner:
                                                        </Typography>
                                                        <Box sx={{ mb: 2 }}>
                                                            <Avatar
                                                                alt={`Winner ${match.winnerId}`}
                                                                src={match.winnerId ? `../../../backend/player-service/profile-picture/player_${match.winnerId}.jpg` : '/path/to/default-avatar.jpg'}
                                                                sx={{ width: 56, height: 56, justifyContent: 'center', alignContent: 'center' }}
                                                            />
                                                        </Box>
                                                        <Typography variant="header3">
                                                            {match.winnerId ? `Player ${match.winnerId}` : 'Pending'}
                                                        </Typography>
                                                    </Box>
                                                </Box>

                                                {/* Modal for editing winner */}
                                                <Dialog open={open && selectedMatch === match} onClose={handleClose}>
                                                    <DialogTitle>Edit Winner</DialogTitle>
                                                    <DialogContent>
                                                        <Select value={winner} onChange={(e) => handleWinnerChange(e.target.value)}
                                                            sx={{
                                                                width: '300px',
                                                                height: '50px',
                                                                fontSize: '18px',
                                                                padding: '10px',
                                                            }}>
                                                            <MenuItem value={match.player1Id}>Player {match.player1Id}</MenuItem>
                                                            <MenuItem value={match.player2Id}>Player {match.player2Id}</MenuItem>
                                                        </Select>
                                                    </DialogContent>
                                                    <DialogActions>
                                                        <Button onClick={handleClose}>Cancel</Button>
                                                        <Button onClick={handleSaveWinner} color="primary">Save</Button>
                                                    </DialogActions>
                                                </Dialog>

                                            </Box>
                                        </Grid>
                                    ))}
                                </Grid>
                            </Box>
                        ))}
                    </CardContent>
                </Card>
            ) : (
                <Typography variant="homePage2" marginLeft={'20px'}>
                    Matches not available yet as the tournament has not started.
                </Typography>
            )}

        </Box>
    );
}

export default AdminTournamentDetails;
