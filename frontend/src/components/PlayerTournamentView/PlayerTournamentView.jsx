import * as React from 'react';
import { useState, useEffect } from 'react';

import { Button, Chip, TextField, FormControl, InputLabel, Select, MenuItem, Box, Typography, Grid, Card, CardActions } from '@mui/material';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import styles from './PlayerTournamentView.module.css';
import SearchIcon from '@mui/icons-material/Search';
import { InputAdornment } from '@mui/material'
import useHandleError from '../Hooks/useHandleError';

import TournamentItem from '../TournamentItem/TournamentItem';

const tournamentURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;

const statusColorMap = {
    LIVE: 'success',
    UPCOMING: 'warning',
    COMPLETED: 'default',
};

function PlayerTournamentView() {
    const [tournaments, setTournaments] = useState([]);
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
    const itemsPerPage = 9;
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
                const tournamentResponse = await axios.get(tournamentURL, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                setTournaments(tournamentResponse.data);

            } catch (error) {
                useHandleError(error);
            }
        };

        fetchTournaments();
    }, [token]);

    const handleViewDetails = (tournamentId) => {
        navigate(`${tournamentId}`);
    };

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
        </div>

    );
}

export default PlayerTournamentView;
