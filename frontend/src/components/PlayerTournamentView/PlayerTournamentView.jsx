import * as React from 'react';
import { useState, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { Button, Chip, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Checkbox, FormControlLabel, Typography, Box, Grid } from '@mui/material';
import styles from './PlayerTournamentView.module.css';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const baseURL2 = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;


const statusColorMap = {
    Live: 'success',
    Upcoming: 'warning',
    Expired: 'default',
};

const StyledTableCell = styled(TableCell)(({ theme }) => ({
    [`&.${tableCellClasses.head}`]: {
        backgroundColor: theme.palette.common.black,
        color: theme.palette.common.white,
        textAlign: 'center',
        variant: 'header1'
    },
    [`&.${tableCellClasses.body}`]: {
        fontSize: 14,
        textAlign: 'center',
    },
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
    '&:nth-of-type(odd)': {
        backgroundColor: theme.palette.action.hover,
    },
    '&:last-child td, &:last-child th': {
        border: 0,
    },
}));

function PlayerTournamentView() {
    const [tournaments, setTournaments] = useState([]);
    const [joinedTournaments, setJoinedTournaments] = useState([]);
    const [openDialog, setOpenDialog] = useState(false);
    const [selectedTournament, setSelectedTournament] = useState({});
    const [agreedToTerms, setAgreedToTerms] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const token = localStorage.getItem('token');
    const navigate = useNavigate();

    useEffect(() => {
        const fetchTournaments = async () => {
            try {
                // Fetch all tournaments
                const tournamentResponse = await axios.get(baseURL, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                setTournaments(tournamentResponse.data);
    
                // Fetch tournaments that the user has registered for
                const registeredResponse = await axios.get(`${baseURL}/registered/current`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                setJoinedTournaments(registeredResponse.data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
    
        fetchTournaments();
    }, []);
    
    const isJoined = (tournamentId) => joinedTournaments.some(tournament => tournament.id === tournamentId);
    
    const handleJoin = (tournament) => {
        setSelectedTournament(tournament);
        setOpenDialog(true);
    };
    
    const handleDialogClose = () => {
        setOpenDialog(false);
        setAgreedToTerms(false);
    };
    
    const handleAgreeChange = (event) => {
        setAgreedToTerms(event.target.checked);
    };
    
    const handleRegister = async () => {
        try {
            const response = await axios.post(`${baseURL2}/register/current/${selectedTournament.id}`, null, {
                headers: { 'Authorization': `Bearer ${token}` },
            });
    
            if (response.status !== 200) {
                throw new Error('Failed to enroll in the tournament');
            }
    
            setJoinedTournaments(prev => [...prev, selectedTournament]);
            setOpenDialog(false);
        } catch (err) {
            console.error(err);
        }
    };
    
    const handleViewDetails = (tournamentId) => {
        navigate(`${tournamentId}`);
    };
    
    if (loading) return <Typography>Loading tournaments...</Typography>;
    if (error) return <Typography>Error: {error}</Typography>;
    

    return (
        <div>
            <Typography variant="header1" component="h2" gutterBottom className={styles.title}>
                All Tournaments
            </Typography>
            <TableContainer component={Paper} className={styles.table}>
                <Table sx={{ minWidth: 700 }} aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell> <Typography variant="header4">ID</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Name</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Start Date</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">End Date</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Time Control</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Min ELO</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Max ELO</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Players</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Status</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Actions</Typography></StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tournaments.map((tournament) => (
                            <StyledTableRow key={tournament.id}>
                                <StyledTableCell><Typography variant="body4">{tournament.id}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.name}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.startDate}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.endDate}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.timeControl.timeControlMinutes}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.minElo}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.maxElo}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.maxPlayers}</Typography></StyledTableCell>
                                <StyledTableCell>
                                    <Chip label={tournament.status} variant="outlined" color={statusColorMap[tournament.status]} />
                                </StyledTableCell>
                                <StyledTableCell>
                                    {tournament.status === "Live" || tournament.status === "Expired" ? (
                                        <></>
                                    ) : (
                                        <div style={{ display: 'flex', alignItems: 'center' }}>
                                            <Button
                                                variant="contained"
                                                color={isJoined(tournament.id) ? 'secondary' : 'success'}
                                                disabled={isJoined(tournament.id) || tournament.status !== "UPCOMING" || tournament.currentPlayers == tournament.maxPlayers}
                                                onClick={() => handleJoin(tournament)}
                                            >
                                                {isJoined(tournament.id)
                                                    ? 'Joined'  // Show "Joined" if the player has joined
                                                    : tournament.currentPlayers >= tournament.maxPlayers
                                                        ? 'Full'    // Show "Full" if the tournament is full
                                                        : 'Join'}
                                            </Button>
                                            <Button
                                                variant="outlined"
                                                color="primary"
                                                onClick={() => handleViewDetails(tournament.id)} // Navigate to tournament details
                                                style={{ marginLeft: '8px' }}
                                            >
                                                <VisibilityIcon /> {/* Visibility Icon */}
                                            </Button>
                                        </div>
                                    )}

                                </StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* Dialog for Terms of Agreement */}
            <Dialog open={openDialog} onClose={handleDialogClose}>
                <DialogTitle variant='header4' align="center">Registration for Chess Tournament</DialogTitle>
                <DialogContent>
                    <DialogContentText align="center" variant='body4'>
                        Please agree to the following terms to join the tournament:
                    </DialogContentText >
                    <Box sx={{ margin: '16px 0' }}>
                        <Typography variant="body4" gutterBottom display='block'>
                            • No use of chess bots or external assistance during matches.
                        </Typography>
                        <Typography variant="body4" gutterBottom display='block'>
                            • All participants must maintain good sportsmanship.
                        </Typography>
                        <Typography variant="body4" gutterBottom display='block'>
                            • Adherence to tournament rules is mandatory.
                        </Typography>
                        <Typography variant="body4" gutterBottom display='block'>
                            • Failure to comply may result in disqualification.
                        </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'center' }}>

                        <FormControlLabel
                            control={<Checkbox checked={agreedToTerms} onChange={handleAgreeChange} />}
                            label={
                                <Typography variant="body4">
                                    I agree to the terms and conditions
                                </Typography>
                            }
                        />
                    </Box>
                </DialogContent>
                <DialogActions sx={{ justifyContent: 'center', pb: 2 }}>
                    <Grid container justifyContent="center" spacing={2}>
                        <Grid item>
                            <Button onClick={handleDialogClose} variant="outlined">
                                Cancel
                            </Button>
                        </Grid>
                        <Grid item>
                            <Button
                                onClick={handleRegister}
                                variant="contained"
                                disabled={!agreedToTerms}
                            >
                                Register
                            </Button>
                        </Grid>
                    </Grid>
                </DialogActions>
            </Dialog>
        </div>
    );
}

export default PlayerTournamentView;

