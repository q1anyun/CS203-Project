import React, { useState } from 'react';
import { styled } from '@mui/material/styles';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Typography, IconButton, TextField, Select, MenuItem, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Button, Fab } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import styles from './AdminTournamentView.module.css';
import axios from 'axios';

const tournamentsData = [
    {
        tournamentId: 100823,
        tournamentName: "Chess Masters",
        startDate: "2024-09-10",
        endDate: "2024-09-15",
        timeControl: "Rapid",
        minElo: 1200,
        maxElo: 1600,
        currentPlayers: 8,
        totalPlayers: 10,
        status: "Expired",
    },
    {
        tournamentId: 200564,
        tournamentName: "Junior Championship",
        startDate: "2024-09-12",
        endDate: "2024-09-18",
        timeControl: "Blitz",
        minElo: 800,
        maxElo: 1200,
        currentPlayers: 6,
        totalPlayers: 8,
        status: "Upcoming",
    },
    {
        tournamentId: 200789,
        tournamentName: "Grand Slam",
        startDate: "2024-09-20",
        endDate: "2024-09-30",
        timeControl: "Classic",
        minElo: 1600,
        maxElo: 2000,
        currentPlayers: 15,
        totalPlayers: 16,
        status: "Live",
    },
];

const statusColorMap = {
    Live: 'success',
    Upcoming: 'warning',
    Expired: 'default',
};

const StyledTableCell = styled(TableCell)(({ theme }) => ({
    '&:first-child': {
        textAlign: 'center',
    },
    '&:last-child': {
        textAlign: 'center',
    },
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
    '&:nth-of-type(odd)': {
        backgroundColor: theme.palette.action.hover,
    },
}));

export default function AdminTournamentView() {
    const [tournaments, setTournaments] = useState(tournamentsData);
    const [editableRow, setEditableRow] = useState(null);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [tournamentToDelete, setTournamentToDelete] = useState(null);
    const [createDialogOpen, setCreateDialogOpen] = useState(false);
    const [newTournament, setNewTournament] = useState({
        tournamentId: '',
        tournamentName: '',
        startDate: '',
        endDate: '',
        timeControl: '',
        minElo: '',
        maxElo: '',
        currentPlayers: '0',
        totalPlayers: '',
        status: 'Upcoming',
    });

    const timeControlOptions = ["Blitz", "Rapid", "Classic"];

    const handleEdit = (rowIndex) => {
        setEditableRow(rowIndex);
    };

    //BACKEND
    const handleSave = async (rowIndex) => {
        const updatedTournament = tournaments[rowIndex];
        
        try {
            const response = await axios.put(`http://localhost:8080/api/tournaments/${updatedTournament.tournamentId}`, updatedTournament);
            setTournaments(prevTournaments => {
                const newTournaments = [...prevTournaments];
                newTournaments[rowIndex] = response.data; // Update with the response from the backend
                return newTournaments;
            });
            setEditableRow(null);
        } catch (error) {
            console.error('Error updating tournament:', error);
        }
    };
    

    const handleChange = (e, rowIndex, field) => {
        const newTournaments = [...tournaments];
        newTournaments[rowIndex][field] = e.target.value;
        setTournaments(newTournaments);
    };

    const handleDeleteClick = (tournamentId) => {
        setTournamentToDelete(tournamentId);
        setDeleteDialogOpen(true);
    };

    //BACKEND
    const handleDeleteConfirm = async () => {
        try {
            await axios.delete(`http://localhost:8080/api/tournaments/${tournamentToDelete}`);
            const updatedTournaments = tournaments.filter(t => t.tournamentId !== tournamentToDelete);
            setTournaments(updatedTournaments);
            setDeleteDialogOpen(false);
            setTournamentToDelete(null);
        } catch (error) {
            console.error('Error deleting tournament:', error);
        }
    };

    const handleDeleteCancel = () => {
        setDeleteDialogOpen(false);
        setTournamentToDelete(null);
    };

    const handleCreate = () => {
        setCreateDialogOpen(true);
    };

    const handleCreateDialogClose = () => {
        setCreateDialogOpen(false);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewTournament({
            ...newTournament,
            [name]: value,
        });
    };

    //BACKEND
    const handleCreateSubmit = async () => {
        const newTournamentData = {
            ...newTournament,
            tournamentId: Math.floor(Math.random() * 100000), 
            currentPlayers: 0, 
            status: 'Upcoming', 
        };
    
        try {
            const response = await axios.post('http://localhost:8080/api/tournaments', newTournamentData);
            setTournaments([response.data, ...tournaments]); 
            
            // Reset to default values
            setNewTournament({
                tournamentId: '',
                tournamentName: '',
                startDate: '',
                endDate: '',
                timeControl: 'Classic',
                minElo: '',
                maxElo: '',
                currentPlayers: 0,
                totalPlayers: '',
                status: 'Upcoming',
            });
            
            setCreateDialogOpen(false);
        } catch (error) {
            console.error('Error creating tournament:', error);
        }
    };
    

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
                        {tournaments.map((tournament, rowIndex) => (
                            <StyledTableRow key={tournament.tournamentId}>
                                <StyledTableCell>{tournament.tournamentId}</StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <TextField
                                            value={tournament.tournamentName}
                                            onChange={(e) => handleChange(e, rowIndex, 'tournamentName')}
                                            variant="outlined"
                                            size="small"
                                        />
                                    ) : (
                                        tournament.tournamentName
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <TextField
                                            type="date"
                                            value={tournament.startDate}
                                            onChange={(e) => handleChange(e, rowIndex, 'startDate')}
                                            variant="outlined"
                                            size="small"
                                        />
                                    ) : (
                                        tournament.startDate
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <TextField
                                            type="date"
                                            value={tournament.endDate}
                                            onChange={(e) => handleChange(e, rowIndex, 'endDate')}
                                            variant="outlined"
                                            size="small"
                                        />
                                    ) : (
                                        tournament.endDate
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <Select
                                            value={tournament.timeControl}
                                            onChange={(e) => handleChange(e, rowIndex, 'timeControl')}
                                            variant="outlined"
                                            size="small"
                                        >
                                            {timeControlOptions.map(option => (
                                                <MenuItem key={option} value={option}>{option}</MenuItem>
                                            ))}
                                        </Select>
                                    ) : (
                                        tournament.timeControl
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <TextField
                                            type="number"
                                            value={tournament.minElo}
                                            onChange={(e) => handleChange(e, rowIndex, 'minElo')}
                                            variant="outlined"
                                            size="small"
                                        />
                                    ) : (
                                        tournament.minElo
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <TextField
                                            type="number"
                                            value={tournament.maxElo}
                                            onChange={(e) => handleChange(e, rowIndex, 'maxElo')}
                                            variant="outlined"
                                            size="small"
                                        />
                                    ) : (
                                        tournament.maxElo
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <div style={{ display: 'flex', alignItems: 'center' }}>
                                            <span style={{ marginRight: '8px' }}>{tournament.currentPlayers}/</span>
                                            <TextField
                                                type="number"
                                                value={tournament.totalPlayers}
                                                onChange={(e) => handleChange(e, rowIndex, 'totalPlayers')}
                                                variant="outlined"
                                                size="small"
                                                sx={{ width: '80px' }}
                                            />
                                        </div>
                                    ) : (
                                        `${tournament.currentPlayers}/${tournament.totalPlayers}`
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    <Chip label={tournament.status} variant="outlined" color={statusColorMap[tournament.status]} />
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <IconButton aria-label="save" color="primary" onClick={() => handleSave(rowIndex)}>
                                            <SaveIcon />
                                        </IconButton>
                                    ) : (
                                        <IconButton aria-label="edit" color="secondary" onClick={() => handleEdit(rowIndex)}>
                                            <EditIcon />
                                        </IconButton>
                                    )}
                                    <IconButton aria-label="view" color="primary" onClick={() => console.log(`Viewing ${tournament.tournamentId}`)}>
                                        <VisibilityIcon />
                                    </IconButton>
                                    <IconButton aria-label="delete" color="error" onClick={() => handleDeleteClick(tournament.tournamentId)}>
                                        <DeleteIcon />
                                    </IconButton>
                                </StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <Fab color="primary" aria-label="add" onClick={handleCreate}>
                <AddIcon />
            </Fab>

            <Dialog open={createDialogOpen} onClose={handleCreateDialogClose}>
    <DialogTitle>Create New Tournament</DialogTitle>
    <Dialog open={createDialogOpen} onClose={handleCreateDialogClose}>
    <DialogTitle>Create New Tournament</DialogTitle>
    <DialogContent>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
            <span style={{ marginRight: '8px', fontSize: '0.875rem' }}>Tournament Name:</span>
            <TextField
                name="tournamentName"
                fullWidth
                onChange={handleInputChange}
                variant="outlined"
                size="small" // Smaller size
            />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
            <span style={{ marginRight: '8px', fontSize: '0.875rem' }}>Start Date:</span>
            <TextField
                type="date"
                name="startDate"
                fullWidth
                onChange={handleInputChange}
                variant="outlined"
                size="small" // Smaller size
            />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
            <span style={{ marginRight: '8px', fontSize: '0.875rem' }}>End Date:</span>
            <TextField
                type="date"
                name="endDate"
                fullWidth
                onChange={handleInputChange}
                variant="outlined"
                size="small" // Smaller size
            />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
            <span style={{ marginRight: '8px', fontSize: '0.875rem' }}>Time Control:</span>
            <Select
                name="timeControl"
                fullWidth
                onChange={handleInputChange}
                variant="outlined"
                size="small" // Smaller size
            >
                {timeControlOptions.map(option => (
                    <MenuItem key={option} value={option}>{option}</MenuItem>
                ))}
            </Select>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
            <span style={{ marginRight: '8px', fontSize: '0.875rem' }}>Min ELO:</span>
            <TextField
                type="number"
                name="minElo"
                fullWidth
                onChange={handleInputChange}
                variant="outlined"
                size="small" // Smaller size
            />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
            <span style={{ marginRight: '8px', fontSize: '0.875rem' }}>Max ELO:</span>
            <TextField
                type="number"
                name="maxElo"
                fullWidth
                onChange={handleInputChange}
                variant="outlined"
                size="small" // Smaller size
            />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
            <span style={{ marginRight: '8px', fontSize: '0.875rem' }}>Total Players:</span>
            <TextField
                type="number"
                name="totalPlayers"
                fullWidth
                onChange={handleInputChange}
                variant="outlined"
                size="small" // Smaller size
            />
        </div>
        </DialogContent>
        <DialogActions>
            <Button onClick={handleCreateDialogClose}>Cancel</Button>
            <Button onClick={handleCreateSubmit} color="primary">Create</Button>
        </DialogActions>
        </Dialog>

            <DialogActions>
                <Button onClick={handleCreateDialogClose}>Cancel</Button>
                <Button onClick={handleCreateSubmit} color="primary">Create</Button>
            </DialogActions>
        </Dialog>

        <Dialog open={deleteDialogOpen} onClose={handleDeleteCancel}>
            <DialogTitle>Delete Tournament</DialogTitle>
            <DialogContent>
                Are you sure you want to delete this tournament?
            </DialogContent>
            <DialogActions>
                <Button onClick={handleDeleteCancel}>Cancel</Button>
                <Button onClick={handleDeleteConfirm} color="error">Delete</Button> 
            </DialogActions>
        </Dialog>
        </div>
    );
}