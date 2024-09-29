import React, { useState, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Typography, IconButton, TextField, Select, MenuItem, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Button, Fab, CircularProgress } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import axios from 'axios';
import styles from './AdminTournamentView.module.css';

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
    const [tournaments, setTournaments] = useState([]);
    const [loading, setLoading] = useState(true); // Loading state
    const [error, setError] = useState(null); // Error state
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

    useEffect(() => {
        // Fetch all tournaments from the backend
        const fetchTournaments = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/tournaments');
                setTournaments(response.data); // Populate tournaments with the fetched data
                setLoading(false); // Turn off loading
            } catch (error) {
                console.error('Error fetching tournaments:', error);
                setError(error); // Capture the error
                setLoading(false); // Turn off loading even if there's an error
            }
        };

        fetchTournaments();
    }, []); // Empty dependency array to run the effect only once when the component mounts

    const handleEdit = (rowIndex) => {
        setEditableRow(rowIndex);
    };

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

    if (loading) {
        return <CircularProgress />;
    }

    if (error) {
        return <Typography color="error">Error loading tournaments: {error.message}</Typography>;
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
                                <StyledTableCell>{tournament.currentPlayers}/{tournament.totalPlayers}</StyledTableCell>
                                <StyledTableCell>
                                    <Chip label={tournament.status} color={statusColorMap[tournament.status]} />
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <IconButton onClick={() => handleSave(rowIndex)}>
                                            <SaveIcon />
                                        </IconButton>
                                    ) : (
                                        <>
                                            <IconButton onClick={() => handleEdit(rowIndex)}>
                                                <EditIcon />
                                            </IconButton>
                                            <IconButton onClick={() => handleDeleteClick(tournament.tournamentId)}>
                                                <DeleteIcon />
                                            </IconButton>
                                            <IconButton>
                                                <VisibilityIcon />
                                            </IconButton>
                                        </>
                                    )}
                                </StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <Fab color="primary" aria-label="add" onClick={handleCreate} className={styles.fab}>
                <AddIcon />
            </Fab>

            {/* Create Tournament Dialog */}
            <Dialog open={createDialogOpen} onClose={handleCreateDialogClose}>
                <DialogTitle>Create New Tournament</DialogTitle>
                <DialogContent>
                    <TextField
                        name="tournamentName"
                        label="Tournament Name"
                        value={newTournament.tournamentName}
                        onChange={handleInputChange}
                        fullWidth
                        margin="dense"
                    />
                    <TextField
                        name="startDate"
                        label="Start Date"
                        type="date"
                        value={newTournament.startDate}
                        onChange={handleInputChange}
                        fullWidth
                        margin="dense"
                        InputLabelProps={{ shrink: true }}
                    />
                    <TextField
                        name="endDate"
                        label="End Date"
                        type="date"
                        value={newTournament.endDate}
                        onChange={handleInputChange}
                        fullWidth
                        margin="dense"
                        InputLabelProps={{ shrink: true }}
                    />
                    <Select
                        name="timeControl"
                        label="Time Control"
                        value={newTournament.timeControl}
                        onChange={handleInputChange}
                        fullWidth
                        margin="dense"
                    >
                        {timeControlOptions.map(option => (
                            <MenuItem key={option} value={option}>{option}</MenuItem>
                        ))}
                    </Select>
                    <TextField
                        name="minElo"
                        label="Min ELO"
                        type="number"
                        value={newTournament.minElo}
                        onChange={handleInputChange}
                        fullWidth
                        margin="dense"
                    />
                    <TextField
                        name="maxElo"
                        label="Max ELO"
                        type="number"
                        value={newTournament.maxElo}
                        onChange={handleInputChange}
                        fullWidth
                        margin="dense"
                    />
                    <TextField
                        name="totalPlayers"
                        label="Total Players"
                        type="number"
                        value={newTournament.totalPlayers}
                        onChange={handleInputChange}
                        fullWidth
                        margin="dense"
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCreateDialogClose} color="secondary">
                        Cancel
                    </Button>
                    <Button onClick={handleCreateSubmit} color="primary">
                        Create
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Delete Confirmation Dialog */}
            <Dialog open={deleteDialogOpen} onClose={handleDeleteCancel}>
                <DialogTitle>Confirm Delete</DialogTitle>
                <DialogActions>
                    <Button onClick={handleDeleteCancel} color="secondary">
                        Cancel
                    </Button>
                    <Button onClick={handleDeleteConfirm} color="primary">
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
