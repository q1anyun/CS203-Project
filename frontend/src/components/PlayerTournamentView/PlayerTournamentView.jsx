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
import { Button, Chip, TextField, FormControl, InputLabel, Select, MenuItem, Box, Typography } from '@mui/material';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import styles from './PlayerTournamentView.module.css';
import SearchIcon from '@mui/icons-material/Search';
import { InputAdornment } from '@mui/material'

import RegisterDialog from './RegisterDialog';
import WithdrawDialog from './WithdrawDialog';

const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;

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
    const [openRegisterDialog, setOpenRegisterDialog] = useState(false);
    const [openWithdrawDialog, setOpenWithdrawDialog] = useState(false);
    const [selectedTournament, setSelectedTournament] = useState({});
    const [agreedToTerms, setAgreedToTerms] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedStatus, setSelectedStatus] = useState('');
    const [minElo, setMinElo] = useState('');
    const [maxElo, setMaxElo] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [timeControl, setTimeControl] = useState('');
    const [maxPlayers, setMaxPlayers] = useState('');

    const token = localStorage.getItem('token');
    const navigate = useNavigate();

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
                const tournamentResponse = await axios.get(baseURL, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                setTournaments(tournamentResponse.data);

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
    }, [token]);

    const isJoined = (tournamentId) => joinedTournaments.some(tournament => tournament.id === tournamentId);

    const handleJoin = (tournament) => {
        setSelectedTournament(tournament);
        setOpenRegisterDialog(true);
    };

    const handleWithdraw = (tournament) => {
        setSelectedTournament(tournament);
        setOpenWithdrawDialog(true);
    }
    const handleRegister = async () => {
        try {
            const response = await axios.post(`${baseURL2}/register/current/${selectedTournament.id}`, null, {
                headers: { 'Authorization': `Bearer ${token}` },
            });

            if (response.status !== 200) {
                throw new Error('Failed to enroll in the tournament');
            }

            setJoinedTournaments(prev => [...prev, selectedTournament]);
            setOpenRegisterDialog(false);
        } catch (err) {
            console.error(err);
        }
    };

    {/*WAITING FOR API TO WITHDRAW */ }
    const handleWithdrawConfirmation = async () => {
        // try {
        //     const response = await axios.post(`${baseURL2}/register/current/${selectedTournament.id}`, null, {
        //         headers: { 'Authorization': `Bearer ${token}` },
        //     });

        //     if (response.status !== 200) {
        //         throw new Error('Failed to enroll in the tournament');
        //     }

        //     setJoinedTournaments(prev => [...prev, selectedTournament]);
        //     setOpenWithdrawDialog(false);
        // } catch (err) {
        //     console.error(err);
        // }

        setOpenWithdrawDialog(false);
    };

    const handleViewDetails = (tournamentId) => {
        navigate(`${tournamentId}`);
    };

    if (loading) return <Typography>Loading tournaments...</Typography>;
    if (error) return <Typography>Error: {error}</Typography>;

    return (
        <div className={styles.container}>
            <Typography variant="h4" gutterBottom margin="0px 0px 10px 20px">
                All Tournaments
            </Typography>

            <Box display="flex" flexDirection="row" gap={1} margin="0px 0px 20px 20px"flexWrap="wrap">
                <TextField
                    label="Search Tournaments"
                    variant="outlined"
                    size="small"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    sx={{ flexShrink: 0, width:'300px'}}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <SearchIcon />
                            </InputAdornment>
                        ),
                    }}
                />
                <FormControl variant="outlined" size="small" sx={{ flexShrink: 0,width:'130px'} }>
                    <InputLabel>Status</InputLabel>
                    <Select
                        value={selectedStatus}
                        onChange={(e) => setSelectedStatus(e.target.value)}
                        label="Status"
                    >
                        <MenuItem value=""><em>All</em></MenuItem>
                        <MenuItem value="LIVE">Live</MenuItem>
                        <MenuItem value="UPCOMING">Upcoming</MenuItem>
                        <MenuItem value="COMPLETED">Completed</MenuItem>
                    </Select>
                </FormControl>
                <TextField
                    label="Min ELO"
                    variant="outlined"
                    type="number"
                    size="small"
                    value={minElo}
                    onChange={(e) => setMinElo(e.target.value)}
                    sx={{ flexShrink: 0, width:'100px'}}
                />
                <TextField
                    label="Max ELO"
                    variant="outlined"
                    type="number"
                    size="small"
                    value={maxElo}
                    onChange={(e) => setMaxElo(e.target.value)}
                    sx={{ flexShrink: 0, width:'100px'}}
                />
                <TextField
                    label="Start Date"
                    variant="outlined"
                    type="date"
                    size="small"
                    InputLabelProps={{ shrink: true }}
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    sx={{ flexShrink: 0, width:'140px'}}
                />
                <TextField
                    label="End Date"
                    variant="outlined"
                    type="date"
                    size="small"
                    InputLabelProps={{ shrink: true }}
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    sx={{ flexShrink: 0, width:'140px'}}
                />
                <TextField
                    label="Time Control"
                    variant="outlined"
                    type="text"
                    size="small"
                    value={timeControl}
                    onChange={(e) => setTimeControl(e.target.value)}
                    sx={{ flexShrink: 0, width:'120px'}}
                />
              <Button
                    variant="outlined"
                    color="primary"
                    onClick={() => {
                        setSearchQuery('');
                        setSelectedStatus('');
                        setMinElo('');
                        setMaxElo('');
                        setStartDate('');
                        setEndDate('');
                        setTimeControl('');
                        setMaxPlayers('');
                    }}
                >
                    Reset Filters
                </Button> 
            </Box>

            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 700 }} aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell>ID</StyledTableCell>
                            <StyledTableCell>Name</StyledTableCell>
                            <StyledTableCell>Start DateTime</StyledTableCell>
                            <StyledTableCell>End DateTime</StyledTableCell>
                            <StyledTableCell>Time Control</StyledTableCell>
                            <StyledTableCell>Min ELO</StyledTableCell>
                            <StyledTableCell>Max ELO</StyledTableCell>
                            <StyledTableCell>Players</StyledTableCell>
                            <StyledTableCell>Status</StyledTableCell>
                            <StyledTableCell>Actions</StyledTableCell>
                            <StyledTableCell>View</StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tournaments
                            .filter(tournament => 
                                tournament.name.toLowerCase().includes(searchQuery.toLowerCase()) &&
                                (selectedStatus ? tournament.status === selectedStatus : true) &&
                                (minElo ? tournament.minElo >= minElo : true) &&
                                (maxElo ? tournament.maxElo <= maxElo : true) &&
                                (startDate ? new Date(tournament.startDate) >= new Date(startDate) : true) &&
                                (endDate ? new Date(tournament.endDate) <= new Date(endDate) : true) &&
                                (timeControl ? tournament.timeControl.timeControlMinutes.toString() === timeControl : true) &&
                                (maxPlayers ? tournament.maxPlayers.toString() === maxPlayers : true)
                            )
                            .map((tournament) => (
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
            <RegisterDialog
                handleRegister={handleRegister}
                agreedToTerms={agreedToTerms}
                setAgreedToTerms={setAgreedToTerms}
                openRegisterDialog={openRegisterDialog}
                setOpenRegisterDialog={setOpenRegisterDialog}
            /> 
            <WithdrawDialog
                openWithdrawDialog={openWithdrawDialog}
                setOpenWithdrawDialog={setOpenWithdrawDialog}
                handleWithdrawConfirmation={handleWithdrawConfirmation}
            /> 
        </div>
        
    );
}

export default PlayerTournamentView;
