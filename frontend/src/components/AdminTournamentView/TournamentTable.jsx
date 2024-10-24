import { Table, TableBody, TableContainer, TableHead, TableRow, TableCell, tableCellClasses, Typography, Chip, IconButton, Box, Fab, Paper, Button, Card, Grid, CardActions, CardContent, Divider } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import AddIcon from '@mui/icons-material/Add';
import styles from './AdminTournamentView.module.css';
import * as React from 'react';
import { useState, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import { TextField, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { InputAdornment } from '@mui/material'

function TournamentTable({ tournaments, handleCreate, handleEditClick, handleDeleteClick, handleViewDetails }) {
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 7;
    const totalPages = Math.ceil(tournaments.length / itemsPerPage);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedStatus, setSelectedStatus] = useState('');
    const [minElo, setMinElo] = useState('');
    const [maxElo, setMaxElo] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [timeControl, setTimeControl] = useState('');
    const [maxPlayers, setMaxPlayers] = useState('');

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

    const statusColorMap = {
        LIVE: 'success',
        UPCOMING: 'warning',
        EXPIRED: 'default',
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

    const tournamentsToShow = tournaments.slice(
        (currentPage - 1) * itemsPerPage,
        currentPage * itemsPerPage
    );

    return (
        <div>
            <Box sx={{ display: 'flex', alignItems: 'center', margin: '0px' }}>
                <Typography variant="header1" component="h2" className={styles.title}>
                    All Tournaments
                </Typography>
                <Fab color="primary" aria-label="add" onClick={handleCreate} className={styles.fab} sx={{ ml: 2 }}>
                    <AddIcon />
                </Fab>
            </Box>
            <Box display="flex" flexDirection="row" gap={1} margin="0px 0px 20px 20px" flexWrap="wrap">
                <TextField
                    label="Search Tournaments"
                    variant="outlined"
                    size="small"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    sx={{ flexShrink: 0, width: '300px' }}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <SearchIcon />
                            </InputAdornment>
                        ),
                    }}
                />
                <FormControl variant="outlined" size="small" labelProps={{ shrink: true }} sx={{ flexShrink: 0, width: '125px' }} >
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
                                <img
                                    src={`https://via.placeholder.com/300x200?text=${tournament.name}`} // get the tournament upload photo (to be updated at another date)
                                    alt={tournament.name}
                                    style={{ width: '100%', height: '200px', objectFit: 'cover' }}
                                />
                                <CardContent sx={{ flexGrow: 1 }}>
                                    <Typography variant="header2">
                                        <span style={{ marginRight: '16px' }}>{tournament.name}</span> {/* Tournament name with margin-right to create space */}
                                        <Chip label={tournament.status} color={statusColorMap[tournament.status]} /> {/* Chip next to the name */}
                                    </Typography>
                                    <Divider sx={{ margin: '8px 0' }} /> {/* Divider after the title */}

                                    <Typography variant="header3">Start: </Typography>
                                    <Typography variant="playerProfile2">
                                        {new Date(tournament.startDate + "Z").toLocaleString()}
                                    </Typography> {/* Body for the Start date */}

                                    <Box display="flex" flexDirection="row" alignItems="center">
                                        <Typography variant="header3" display="block" sx={{ marginRight: '8px' }}>End:</Typography>
                                        <Typography variant="playerProfile2">
                                            {new Date(tournament.endDate + "Z").toLocaleString()}
                                        </Typography> {/* Body for the End date */}
                                    </Box>

                                    <Box display="flex" flexDirection="row" alignItems="center">
                                        <Typography variant="header3" display="block" sx={{ marginRight: '8px' }}>Min ELO:</Typography>
                                        <Typography variant="playerProfile2">
                                            {tournament.minElo}
                                        </Typography>
                                    </Box>
                                    <Box display="flex" flexDirection="row" alignItems="center">
                                        <Typography variant="header3" display="block" sx={{ marginRight: '8px' }}>Max ELO:</Typography>

                                        <Typography variant="playerProfile2">
                                            {tournament.maxElo}
                                        </Typography>
                                    </Box>
                                    <Box display="flex" flexDirection="row" alignItems="center">
                                        <Typography variant="header3" display="block" sx={{ marginRight: '8px' }}>Time Control:</Typography>

                                        <Typography variant="playerProfile2">
                                            {tournament.timeControl.timeControlMinutes} minutes
                                        </Typography>
                                    </Box>


                                </CardContent>

                                <CardActions>
                                    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                                        <IconButton
                                            onClick={() => handleEditClick(tournament.id)}
                                            sx={{ color: 'primary.main', mr: 4 }}
                                        >
                                            <EditIcon />
                                        </IconButton>

                                        <IconButton
                                            onClick={() => handleDeleteClick(tournament.id)}
                                            sx={{ color: 'error.main', mr: 4 }}
                                        >
                                            <DeleteIcon />
                                        </IconButton>

                                        <IconButton
                                            onClick={() => handleViewDetails(tournament.id)}
                                            sx={{ color: 'secondary.main', mr: 4 }}
                                        >
                                            <VisibilityIcon />
                                        </IconButton>


                                    </Box>




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

export default TournamentTable;
