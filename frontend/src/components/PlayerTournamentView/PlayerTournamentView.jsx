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
import { Button, Chip, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Checkbox, FormControlLabel, Typography, Box, Grid } from '@mui/material';
import styles from './PlayerTournamentView.module.css';
import axios from 'axios';

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
    const [selectedTournament, setSelectedTournament] = useState([]);
    const [agreedToTerms, setAgreedToTerms] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const token = localStorage.getItem('token');

    useEffect(() => {
        const fetchTournaments = async () => {
            try {
                // Fetch all tournaments
                const response = await axios.get(`${baseURL}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });
                setTournaments(response.data);

                // Fetch the tournaments that the user has registered for
                const tournamentJoinedResponse = await axios.get(`${baseURL}/registered/current`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });
                setJoinedTournaments(tournamentJoinedResponse.data);
            } catch (error) {
                setError(error.message);
            } finally {
                setLoading(false);
            }
        };

        fetchTournaments();
    }, []);

    const isJoined = (tournamentId) => {
        return joinedTournaments.includes(tournamentId);
    };

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
        console.log(token);
        if (selectedTournament.id != null) {
            try {
                const response = await axios.post(`${baseURL2}/register/current/${selectedTournament.id}`, null, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        
                    },
                });
                if (response.status !== 200) {
                    throw new Error('Failed to enroll in the tournament');
                }

                setJoinedTournaments((prevJoined) => [...prevJoined, selectedTournament.id]);
                setOpenDialog(false);
            } catch (error) {
                console.error(error);
            }
        }
    };

    if (loading) {
        return <Typography>Loading tournaments...</Typography>;
    }

    if (error) {
        return <Typography>Error: {error}</Typography>;
    }

    return (
        <div>
            <Typography variant="h4" component="h2" gutterBottom className={styles.title}>
                All Tournaments
            </Typography>
            <TableContainer component={Paper} className={styles.table}>
                <Table sx={{ minWidth: 700 }} aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell>ID</StyledTableCell>
                            <StyledTableCell>Name</StyledTableCell>
                            <StyledTableCell>Start Date</StyledTableCell>
                            <StyledTableCell>End Date</StyledTableCell>
                            <StyledTableCell>Time Control</StyledTableCell>
                            <StyledTableCell>Min ELO</StyledTableCell>
                            <StyledTableCell>Max ELO</StyledTableCell>
                            <StyledTableCell>Players</StyledTableCell>
                            <StyledTableCell>Status</StyledTableCell>
                            <StyledTableCell>Actions</StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tournaments.map((tournament) => (
                            <StyledTableRow key={tournament.id}>
                                <StyledTableCell>{tournament.id}</StyledTableCell>
                                <StyledTableCell>{tournament.name}</StyledTableCell>
                                <StyledTableCell>{tournament.startDate}</StyledTableCell>
                                <StyledTableCell>{tournament.endDate}</StyledTableCell>
                                <StyledTableCell>{tournament.timeControl.timeControlMinutes}</StyledTableCell>
                                <StyledTableCell>{tournament.minElo}</StyledTableCell>
                                <StyledTableCell>{tournament.maxElo}</StyledTableCell>
                                <StyledTableCell>{tournament.maxPlayers}</StyledTableCell>
                                <StyledTableCell>
                                    <Chip label={tournament.status} variant="outlined" color={statusColorMap[tournament.status]} />
                                </StyledTableCell>
                                <StyledTableCell>
                                    {tournament.status === "Live" || tournament.status === "Expired" ? (
                                        <></>
                                    ) : (
                                        <Button
                                            variant="contained"
                                            color={isJoined(tournament.id) ? 'secondary' : 'success'}
                                            disabled={isJoined(tournament.id)}
                                            onClick={() => handleJoin(tournament)}
                                        >
                                            {isJoined(tournament.id) ? 'Joined' : 'Join'}
                                        </Button>
                                    )}
                                </StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* Dialog for Terms of Agreement */}
            <Dialog open={openDialog} onClose={handleDialogClose}>
                <DialogTitle align="center">Registration for Chess Tournament</DialogTitle>
                <DialogContent>
                    <DialogContentText align="center">
                        Please agree to the following terms to join the tournament:
                    </DialogContentText>
                    <Box sx={{ margin: '16px 0' }}>
                        <Typography variant="body2" gutterBottom>
                            • No use of chess bots or external assistance during matches.
                        </Typography>
                        <Typography variant="body2" gutterBottom>
                            • All participants must maintain good sportsmanship.
                        </Typography>
                        <Typography variant="body2" gutterBottom>
                            • Adherence to tournament rules is mandatory.
                        </Typography>
                        <Typography variant="body2" gutterBottom>
                            • Failure to comply may result in disqualification.
                        </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                        <FormControlLabel
                            control={<Checkbox checked={agreedToTerms} onChange={handleAgreeChange} />}
                            label="I agree to the terms and conditions"
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

