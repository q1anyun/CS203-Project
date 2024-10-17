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

function TournamentTable(token, baseURL, handleJoin, handleWithdraw, handleViewDetails, isJoined) {

    const [joinedTournaments, setJoinedTournaments] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [tournaments, setTournaments] = useState([]);
    const statusColorMap = {
        LIVE: 'success',
        UPCOMING: 'warning',
        COMPLETED: 'default',
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

    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 7;  
    const totalPages = Math.ceil(tournaments.length / itemsPerPage);
    const handleNextPage = () => {
        if (currentPage < totalPages) {
            setCurrentPage(prevPage => prevPage + 1);
        }
    };

    const handlePrevPage = () => {
        if (currentPage > 1) {
            setCurrentPage(prevPage => prevPage - 1);
        }
    };

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

    if (loading) return <Typography>Loading tournaments...</Typography>;
    if (error) return <Typography>Error: {error}</Typography>;

    console.log(tournaments);
    return (
        <div className={styles.container}>
            <Typography variant="header1" component="h2" gutterBottom className={styles.title}>
                All Tournaments
            </Typography>
            <TableContainer component={Paper} className={styles.table}>
                <Table sx={{ minWidth: 700 }} aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell> <Typography variant="header4">ID</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Name</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Start DateTime</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">End DateTime</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Time Control</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Min ELO</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Max ELO</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Players</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Status</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Actions</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">View</Typography></StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tournaments.map((tournament) => (
                            <StyledTableRow key={tournament.id}>
                                <StyledTableCell><Typography variant="body4">{tournament.id}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.name}</Typography></StyledTableCell>
                                <StyledTableCell>
                                    <Typography variant="body4">
                                        {new Date(tournament.startDate + "Z").toLocaleString('en-GB', {
                                            timeZone: 'Asia/Singapore',
                                            year: 'numeric',
                                            month: '2-digit',
                                            day: '2-digit',
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        })}
                                    </Typography>
                                </StyledTableCell>

                                <StyledTableCell>
                                    <Typography variant="body4">
                                        {new Date(tournament.endDate + "Z").toLocaleString('en-GB', {
                                            timeZone: 'Asia/Singapore',
                                            year: 'numeric',
                                            month: '2-digit',
                                            day: '2-digit',
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        })}
                                    </Typography>
                                </StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.timeControl.timeControlMinutes}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.minElo}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.maxElo}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body4">{tournament.maxPlayers}</Typography></StyledTableCell>
                                <StyledTableCell>
                                    <Chip label={tournament.status} color={statusColorMap[tournament.status]} />
                                </StyledTableCell>
                                <StyledTableCell>
                                    {tournament.status === "Live" || tournament.status === "Expired" ? (
                                        <></>
                                    ) : (
                                        <Button
                                            variant="contained"
                                            color={isJoined(tournament.id) ? 'secondary' : 'success'}
                                            disabled={tournament.status !== "UPCOMING" || tournament.currentPlayers == tournament.maxPlayers}
                                            onClick={() => {
                                                if (isJoined(tournament.id)) {
                                                    handleWithdraw(tournament);  // Call handleWithdraw if the user has joined
                                                } else {
                                                    handleJoin(tournament);      // Call handleJoin if the user has not joined
                                                }
                                            }}
                                            style={{ width: '120px' }}
                                        >
                                            {isJoined(tournament.id)
                                                ? 'Withdraw'  // Show "Joined" if the player has joined
                                                : tournament.currentPlayers >= tournament.maxPlayers
                                                    ? 'Full'    // Show "Full" if the tournament is full
                                                    : 'Join'}
                                        </Button>

                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    <Button
                                        variant="outlined"
                                        color="primary"
                                        onClick={() => handleViewDetails(tournament.id)} // Navigate to tournament details
                                        style={{ marginLeft: '8px' }}
                                    >
                                        <VisibilityIcon /> {/* Visibility Icon */}
                                    </Button>
                                </StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', mt: 2 }}>
                <Button onClick={handlePrevPage} disabled={currentPage === 1} variant="contained" sx={{ mr: 2 }}>
                    Previous
                </Button>
                <Typography variant="body1">
                    Page {currentPage} of {totalPages}
                </Typography>
                <Button onClick={handleNextPage} disabled={currentPage === totalPages} variant="contained" sx={{ ml: 2 }}>
                    Next
                </Button>
            </Box>
        </div>
    );
}

export default TournamentTable;
