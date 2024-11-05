import * as React from 'react';
import { useState, useEffect } from 'react';

import { Button, Chip, TextField, FormControl, InputLabel, Select, MenuItem, Box, Typography, Grid, Card, CardActions} from '@mui/material';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import styles from './PlayerTournamentView.module.css';
import SearchIcon from '@mui/icons-material/Search';
import { InputAdornment } from '@mui/material'

import RegisterDialog from './RegisterDialog';
import WithdrawDialog from './WithdrawDialog';
import TournamentItem from '../TournamentItem/TournamentItem';

const tournamentURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const tournamentPlayerURL = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;
const playerServiceURL = import.meta.env.VITE_PLAYER_SERVICE_URL;

const statusColorMap = {
    LIVE: 'success',
    UPCOMING: 'warning',
    COMPLETED: 'default',
};

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
    const [elo, setElo] = useState('');

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

    const getPlayerElo = async () => {
        try {
            const response = await axios.get(`${playerServiceURL}/currentPlayerById`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                }
            });
            setElo(response.data.eloRating);
        } catch (error) {
            console.error('Failed to fetch player ELO:', error);
        }
    };

    useEffect(() => {
        if (token) {
            getPlayerElo();

        }
    }, [token]);

    useEffect(() => {
        const fetchTournaments = async () => {
            try {
                const tournamentResponse = await axios.get(tournamentURL, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                setTournaments(tournamentResponse.data);

                const registeredResponse = await axios.get(`${tournamentURL}/registered/current`, {
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
    {/* DOESNT ACCOUNT min max elo*/ }
    const handleRegister = async () => {
        try {
            console.log(token);
            console.log(selectedTournament.id);
            const response = await axios.post(`${tournamentPlayerURL}/register/current/${selectedTournament.id}`, null, {
                headers: { 'Authorization': `Bearer ${token}` },
            });

            setJoinedTournaments(prev => [...prev, selectedTournament]);
            setOpenRegisterDialog(false);
        } catch (err) {
            console.error(err);
        }
    };

    {/*WAITING FOR API TO WITHDRAW */ }
    const handleWithdrawConfirmation = async () => {
        try {
            const response = await axios.delete(`${tournamentPlayerURL}/current/${selectedTournament.id}`, {
                headers: { 'Authorization': `Bearer ${token}` },
            });

            setJoinedTournaments(prev => prev.filter(tournament => tournament.id !== selectedTournament.id));
            setOpenWithdrawDialog(false);
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
        <div className={styles.container}>
            <Typography variant="header1" gutterBottom>
                All Tournaments
            </Typography>

            <Box display="flex" flexDirection="row" gap={1} margin="0px 0px 20px 20px" flexWrap="wrap">
                <TextField
                    label="Search Tournaments"
                    variant="outlined"
                    size="small"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    sx={{ flexShrink: 0, width: '450px' }}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <SearchIcon />
                            </InputAdornment>
                        ),
                    }}
                />
                <FormControl variant="outlined" size="small" sx={{ flexShrink: 0, width: '125px' }}>
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
                    sx={{ flexShrink: 0, width: '100px' }}
                />
                <TextField
                    label="Max ELO"
                    variant="outlined"
                    type="number"
                    size="small"
                    value={maxElo}
                    onChange={(e) => setMaxElo(e.target.value)}
                    sx={{ flexShrink: 0, width: '100px' }}
                />
                <TextField
                    label="Start Date"
                    variant="outlined"
                    type="date"
                    size="small"
                    InputLabelProps={{ shrink: true }}
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    sx={{ flexShrink: 0, width: '140px' }}
                />
                <TextField
                    label="End Date"
                    variant="outlined"
                    type="date"
                    size="small"
                    InputLabelProps={{ shrink: true }}
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    sx={{ flexShrink: 0, width: '140px' }}
                />
                <TextField
                    label="Time Control"
                    variant="outlined"
                    type="text"
                    size="small"
                    value={timeControl}
                    onChange={(e) => setTimeControl(e.target.value)}
                    sx={{ flexShrink: 0, width: '120px' }}
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

            <Grid container spacing={3} padding={'8px'}>
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
                    .slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage)
                    .map((tournament) => (
                        <Grid item xs={12} sm={6} md={4} key={tournament.id}>
                            <Card sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                                
                             <TournamentItem key={tournament.id} tournament={tournament} />
                                <CardActions>
                                    <Button
                                        variant="contained"
                                        color={isJoined(tournament.id) ? 'secondary' : 'primary'}
                                        onClick={() => isJoined(tournament.id) ? handleWithdraw(tournament) : handleJoin(tournament)}
                                        disabled={
                                            tournament.status === 'COMPLETED' ||
                                            (tournament.status === 'LIVE' && true) ||
                                            elo < tournament.minElo ||
                                            elo > tournament.maxElo ||
                                            tournament.currentPlayers >= tournament.maxPlayers
                                        }
                                    >
                                        {tournament.status === 'COMPLETED'
                                            ? 'OVER' : tournament.currentPlayers >= tournament.maxPlayers ? 'FULL' : isJoined(tournament.id) ? 'Withdraw' : 'Join'}
                                    </Button>
                                    <Button variant="outlined" onClick={() => handleViewDetails(tournament.id)}>
                                        View
                                    </Button>
                                    <Chip label={tournament.status} color={statusColorMap[tournament.status]} />
                                </CardActions>
                            </Card>
                        </Grid>
                    ))}
            </Grid>

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
