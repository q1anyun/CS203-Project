import * as React from 'react'; 
import { useState } from 'react';
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

const tournamentsData = [
    {
        tournamentId: 100823,
        tournamentName: "Chess Masters",
        startDate: "2024-09-10",
        endDate: "2024-09-15",
        timeControl: "Rapid",
        minElo: 1200,
        maxElo: 1800,
        numberOfPlayers: 10,
        status: "Expired",
    },
    {
        tournamentId: 200564,
        tournamentName: "Junior Championship",
        startDate: "2024-09-12",
        endDate: "2024-09-18",
        timeControl: "Blitz",
        minElo: 800,
        maxElo: 1400,
        numberOfPlayers: 8,
        status: "Upcoming",
    },
    {
        tournamentId: 200789,
        tournamentName: "Grand Slam",
        startDate: "2024-09-20",
        endDate: "2024-09-30",
        timeControl: "Classic",
        minElo: 1500,
        maxElo: 2200,
        numberOfPlayers: 16,
        status: "Live",
    },
];


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
    const [tournaments, setTournaments] = useState(tournamentsData);
    const [joinedTournaments, setJoinedTournaments] = useState([]);
    const [openDialog, setOpenDialog] = useState(false);
    const [selectedTournament, setSelectedTournament] = useState(null);
    const [agreedToTerms, setAgreedToTerms] = useState(false);

    const handleJoin = (tournament) => {
        setSelectedTournament(tournament);
        setOpenDialog(true);  // Open the registration pop-up
    };

    const handleDialogClose = () => {
        setOpenDialog(false);
        setAgreedToTerms(false);
    };

    const handleAgreeChange = (event) => {
        setAgreedToTerms(event.target.checked);
    };

    const handleRegister = () => {
        if (selectedTournament) {
            setJoinedTournaments((prevJoined) => [...prevJoined, selectedTournament.tournamentId]);
            setOpenDialog(false);
        }
    };

    const isJoined = (tournamentId) => joinedTournaments.includes(tournamentId);

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
                            <StyledTableRow key={tournament.tournamentId}>
                                <StyledTableCell>{tournament.tournamentId}</StyledTableCell>
                                <StyledTableCell>{tournament.tournamentName}</StyledTableCell>
                                <StyledTableCell>{tournament.startDate}</StyledTableCell>
                                <StyledTableCell>{tournament.endDate}</StyledTableCell>
                                <StyledTableCell>{tournament.timeControl}</StyledTableCell>
                                <StyledTableCell>{tournament.minElo}</StyledTableCell>
                                <StyledTableCell>{tournament.maxElo}</StyledTableCell>
                                <StyledTableCell>{tournament.numberOfPlayers}</StyledTableCell>
                                <StyledTableCell>
                                    <Chip label={tournament.status} variant="outlined" color={statusColorMap[tournament.status]} />
                                </StyledTableCell>
                                <StyledTableCell>
                                    {tournament.status === "Live" || tournament.status === "Expired" ? ( 
                                                <> {} </>
                                    ) : (
                                        <Button
                                            variant="contained"
                                            color={isJoined(tournament.tournamentId) ? 'secondary' : 'success'}
                                            disabled={isJoined(tournament.tournamentId)}
                                            onClick={() => handleJoin(tournament)}
                                        >
                                            {isJoined(tournament.tournamentId) ? 'Joined' : 'Join'}
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
                            <Button onClick={handleRegister} color="success" variant="contained" disabled={!agreedToTerms}>
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
