import { Table, TableBody, TableContainer, TableHead, TableRow, TableCell, tableCellClasses, Typography, Chip, IconButton, Box, Fab, Paper, Button } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import AddIcon from '@mui/icons-material/Add';
import styles from './AdminTournamentView.module.css';
import * as React from 'react';
import { useState, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import {TextField, FormControl, InputLabel, Select, MenuItem} from '@mui/material';
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
            <Box sx={{ display: 'flex', alignItems: 'center', margin:'0px'}}>
                <Typography variant="header1" component="h2" className={styles.title}>
                    All Tournaments
                </Typography>
                <Fab color="primary" aria-label="add" onClick={handleCreate} className={styles.fab} sx={{ ml: 2 }}>
                    <AddIcon />
                </Fab>
            </Box>
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
                <FormControl variant="outlined" size="small" labelProps={{ shrink: true }} sx={{ flexShrink: 0, width:'125px'}} >
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
            <TableContainer component={Paper} className={styles.table}>
                <Table sx={{ minWidth: 700 }} aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell><Typography variant="header4">ID</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Name</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Start DateTime</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">End DateTime</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Time Control</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Min ELO</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Max ELO</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Players</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Status</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="header4">Actions</Typography></StyledTableCell>
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
                                        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', mt: 2 }}>
                                            <IconButton 
                                                onClick={() => handleEditClick(tournament.id)} 
                                                sx={{ color: 'primary.main' }}  
                                            >
                                                <EditIcon />
                                            </IconButton>

                                            <IconButton 
                                                onClick={() => handleDeleteClick(tournament.id)} 
                                                sx={{ color: 'error.main' }}  
                                            >
                                                <DeleteIcon />
                                            </IconButton>

                                            <IconButton 
                                                onClick={() => handleViewDetails(tournament.id)} 
                                                sx={{ color: 'secondary.main' }}  
                                            >
                                                <VisibilityIcon />
                                            </IconButton>
                                        </Box>
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
