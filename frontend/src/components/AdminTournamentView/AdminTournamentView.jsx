import React, { useState, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Typography, IconButton, TextField, Select, MenuItem, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Button, Fab, CircularProgress, InputLabel, FormControl } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import axios from 'axios';
import styles from './AdminTournamentView.module.css';
import { useNavigate } from 'react-router-dom';

const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const gameTypeURL = import.meta.env.VITE_TOURNAMENT_GAMETYPE_URL;
const roundTypeURL = import.meta.env.VITE_TOURNAMENT_ROUNDTYPE_URL;

const statusColorMap = {
    LIVE: 'success',
    UPCOMING: 'warning',
    EXPIRED: 'default',
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
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [tournamentToEdit, setTournamentToEdit] = useState(null);
    const [tournamentToDelete, setTournamentToDelete] = useState(null);
    const [createDialogOpen, setCreateDialogOpen] = useState(false);
    const [editDialogOpen, setEditDialogOpen] = useState(false);
    const [timeControlOptions, setTimeControlOptions] = useState([]);
    const [roundTypeOptions, setRoundTypeOptions] = useState([]);

    const token = localStorage.getItem('token');

    const navigate = useNavigate();
    useEffect(() => {
        const fetchTimeControls = async () => {
            const response = await axios.get(`${gameTypeURL}`);
            setTimeControlOptions(response.data);
        };

        fetchTimeControls();
    }, []);

    useEffect(() => {
        const fetchRoundType = async () => {
            const response = await axios.get(`${roundTypeURL}/choices`);
            setRoundTypeOptions(response.data);
        };

        fetchRoundType();
    }, []);

    // for create new table
    const [newTournament, setNewTournament] = useState({
        name: '',
        startDate: '',
        endDate: '',
        timeControl: '',
        minElo: '',
        maxElo: '',
        maxPlayers: '',
    });

    // for edit 
    const [updateTournament, setUpdateTournament] = useState({
        name: '',
        startDate: '',
        endDate: '',
        timeControl: '',
        minElo: '',
        maxElo: '',
        maxPlayers: '',
    });

    const handleViewDetails = (id) => {
        // Navigate to /tournament/id route
        navigate(`/admin/tournaments/${id}`);
    };

    useEffect(() => {
        // Fetch all tournaments from the backend
        const fetchTournaments = async () => {
            try {
                const response = await axios.get(`${baseURL}`);
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

    const handleChange = (e, rowIndex, field) => {
        const newTournaments = [...tournaments];
        newTournaments[rowIndex][field] = e.target.value;
        setTournaments(newTournaments);
    };

    const handleDeleteClick = (tournamentId) => {
        setTournamentToDelete(tournamentId);
        setDeleteDialogOpen(true);
    };

    const handleEditClick = (tournamentId) => {
        setTournamentToEdit(tournamentId);
        setEditDialogOpen(true);
    };

    {/* DELETE TOURNAMENT */ }
    const handleDeleteConfirm = async () => {
        try {
            console.log(baseURL);
            const response = await axios.delete(`${baseURL}/${tournamentToDelete}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                }
            });

            // Check if the deletion was successful
            if (response.status === 200) {
                const updatedTournaments = tournaments.filter(t => t.tournamentId !== tournamentToDelete);
                setTournaments(updatedTournaments);
                setDeleteDialogOpen(false);
                setTournamentToDelete(null);
                console.log('Tournament deleted successfully.');
                window.location.reload();
            } else {
                console.warn('Delete request did not return a success status:', response.status);
            }
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

    const handleEdit = () => {
        setEditDialogOpen(true);
    };

    const handleEditDialogClose = () => {
        setEditDialogOpen(false);
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

    const handleEditInputChange = (e) => {
        const { name, value } = e.target;
        setUpdateTournament({
            ...updateTournament,
            [name]: value,
        });
    };

    const handleCreateSubmit = async () => {
        const startDateWithTime = `${newTournament.startDate}T00:00:00`; // Add 12 AM time to start date
        const endDateWithTime = `${newTournament.endDate}T00:00:00`;     // Add 12 AM time to end date

        const newTournamentData = {
            ...newTournament,
            tournamentId: Math.floor(Math.random() * 100000),
            startDate: startDateWithTime,
            endDate: endDateWithTime
        };

        console.log("New Tournament Data:", newTournamentData);

        try {
            const response = await axios.post(`${baseURL}`, newTournamentData, {
                headers: {
                    'Authorization': `Bearer ${token}`, // Add the JWT token here
                }
            });
            // console.error('Error data:', error.response.data);
            setTournaments([response.data, ...tournaments]);

            // Reset to default values
            setNewTournament({
                name: '',
                startDate: '',
                endDate: '',
                timeControl: '',
                minElo: '',
                maxElo: '',
                maxPlayers: '',
            });

            setCreateDialogOpen(false);
            window.location.reload();
        } catch (error) {
            if (error.response) {
                console.error('Error data:', error.response.data); // Response from the backend
                console.error('Error status:', error.response.status); // Status code (e.g., 400)
                console.error('Error headers:', error.response.headers); // Response headers
            } else {
                console.error('Error message:', error.message);
            }
            // console.error('Error creating tournament:', error);
        }
    };

    const handleEditSubmit = async () => {
        const startDateWithTime = `${updateTournament.startDate}T00:00:00`; // Add 12 AM time to start date
        const endDateWithTime = `${updateTournament.endDate}T00:00:00`;     // Add 12 AM time to end date

        const updatedTournamentData = {
            ...updateTournament,
            startDate: startDateWithTime,
            endDate: endDateWithTime
        };
        console.log(updatedTournamentData)
        try {
            // Send the update request to the backend (assuming PUT is for updating)
            const response = await axios.put(`${baseURL}/${tournamentToEdit}`, updatedTournamentData, {
                headers: {
                    'Authorization': `Bearer ${token}`, // Include JWT token
                }
            });

            // Update the tournaments list by replacing the updated tournament
            const updatedTournaments = tournaments.map(t =>
                t.tournamentId === tournamentToEdit ? response.data : t
            );

            // Update the state with the new tournaments data
            setTournaments(updatedTournaments);

            // Reset to default values after successful edit
            setUpdateTournament({
                name: '',
                startDate: '',
                endDate: '',
                timeControl: '',
                minElo: '',
                maxElo: '',
                maxPlayers: ''
            });

            // Close the edit dialog
            setEditDialogOpen(false);
            window.location.reload();
        } catch (error) {
            if (error.response) {
                console.error('Error data:', error.response.data); // Backend response
                console.error('Error status:', error.response.status); // HTTP status code
                console.error('Error headers:', error.response.headers); // Headers
            } else {
                console.error('Error message:', error.message); // General error
            }
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
            <Fab color="primary" aria-label="add" onClick={handleCreate} className={styles.fab}>
                <AddIcon />
            </Fab>
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
                            <StyledTableCell>MaxPlayers</StyledTableCell>
                            <StyledTableCell>Status</StyledTableCell>
                            <StyledTableCell>Actions</StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tournaments.map((tournament, rowIndex) => (
                            <StyledTableRow key={tournament.id}>
                                <StyledTableCell>{tournament.id}</StyledTableCell>
                                <StyledTableCell>
                                    {/* Display as text */}
                                    <Typography>{tournament.name}</Typography>
                                </StyledTableCell>
                                <StyledTableCell>
                                    {/* Display date as text */}
                                    <Typography>{tournament.startDate}</Typography>
                                </StyledTableCell>
                                <StyledTableCell>
                                    <Typography>{tournament.endDate}</Typography>
                                </StyledTableCell>
                                <StyledTableCell>
                                    <Typography>{tournament.timeControl?.name || ''}</Typography>
                                </StyledTableCell>
                                <StyledTableCell>
                                    <Typography>{tournament.minElo}</Typography>
                                </StyledTableCell>
                                <StyledTableCell>
                                    <Typography>{tournament.maxElo}</Typography>
                                </StyledTableCell>
                                <StyledTableCell>
                                    <Typography>{tournament.maxPlayers}</Typography>
                                </StyledTableCell>
                                <StyledTableCell>
                                    <Chip label={tournament.status} color={statusColorMap[tournament.status]} />
                                </StyledTableCell>
                                <StyledTableCell>
                                    <>
                                        <IconButton onClick={() => handleEditClick(tournament.id)}>
                                            <EditIcon />
                                        </IconButton>
                                        <IconButton onClick={() => handleDeleteClick(tournament.id)}>
                                            <DeleteIcon />
                                        </IconButton>
                                        <IconButton>
                                            <VisibilityIcon onClick={() => handleViewDetails(tournament.id)} />
                                        </IconButton>
                                    </>
                                </StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>


            {/* Create Tournament Dialog */}
            <Dialog open={createDialogOpen} onClose={handleCreateDialogClose}>
                <DialogTitle>Create New Tournament</DialogTitle>
                <DialogContent>
                    <TextField
                        name="name"
                        label="Tournament Name"
                        value={newTournament.name}
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
                    <FormControl fullWidth>
                        <InputLabel>Time Control</InputLabel>
                        <Select
                            name="timeControl"
                            label="Time Control"
                            value={newTournament.timeControl}
                            onChange={handleInputChange}
                            sx={{ textAlign: 'left' }}
                            MenuProps={{
                                PaperProps: {
                                    style: {
                                        maxHeight: 200,
                                        width: 'auto',
                                    },
                                },
                            }}
                        >
                            {timeControlOptions.map((option) => (
                                <MenuItem key={option.id} value={option.id}>
                                    {option.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
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

                    <FormControl fullWidth>
                        <InputLabel>Max Players</InputLabel>
                        <Select
                            name="maxPlayers"
                            label="Max Players"
                            value={newTournament.maxPlayers}
                            onChange={handleInputChange}
                            sx={{ textAlign: 'left' }}
                            MenuProps={{
                                PaperProps: {
                                    style: {
                                        maxHeight: 200,
                                        width: 'auto',
                                    },
                                },
                            }}
                        >
                            {roundTypeOptions.map((optionId) => (
                                <MenuItem key={optionId} value={optionId}>
                                    {optionId}
                                </MenuItem>
                            ))}

                        </Select>
                    </FormControl>
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

            {/* Edit Tournament Dialog */}
            <Dialog open={editDialogOpen} onClose={handleEditDialogClose}>
                <DialogTitle>Edit Tournament</DialogTitle>
                <DialogContent>
                    <TextField
                        name="name"
                        label="Tournament Name"
                        value={updateTournament.name}
                        onChange={handleEditInputChange}
                        fullWidth
                        margin="dense"
                    />
                    <TextField
                        name="startDate"
                        label="Start Date"
                        type="date"
                        value={updateTournament.startDate}
                        onChange={handleEditInputChange}
                        fullWidth
                        margin="dense"
                        InputLabelProps={{ shrink: true }}
                    />
                    <TextField
                        name="endDate"
                        label="End Date"
                        type="date"
                        value={updateTournament.endDate}
                        onChange={handleEditInputChange}
                        fullWidth
                        margin="dense"
                        InputLabelProps={{ shrink: true }}
                    />
                    <FormControl fullWidth>
                        <InputLabel>Time Control</InputLabel>
                        <Select
                            name="timeControl"
                            label="Time Control"
                            value={updateTournament.timeControl}
                            onChange={handleEditInputChange}
                            sx={{ textAlign: 'left' }}
                            MenuProps={{
                                PaperProps: {
                                    style: {
                                        maxHeight: 200,
                                        width: 'auto',
                                    },
                                },
                            }}
                        >
                            {timeControlOptions.map((option) => (
                                <MenuItem key={option.id} value={option.id}>
                                    {option.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                    <TextField
                        name="minElo"
                        label="Min ELO"
                        type="number"
                        value={updateTournament.minElo}
                        onChange={handleEditInputChange}
                        fullWidth
                        margin="dense"
                    />
                    <TextField
                        name="maxElo"
                        label="Max ELO"
                        type="number"
                        value={updateTournament.maxElo}
                        onChange={handleEditInputChange}
                        fullWidth
                        margin="dense"
                    />

                    <FormControl fullWidth>
                        <InputLabel>Max Players</InputLabel>
                        <Select
                            name="maxPlayers"
                            label="Max Players"
                            value={updateTournament.maxPlayers}
                            onChange={handleEditInputChange}
                            sx={{ textAlign: 'left' }}
                            MenuProps={{
                                PaperProps: {
                                    style: {
                                        maxHeight: 200,
                                        width: 'auto',
                                    },
                                },
                            }}
                        >
                            {roundTypeOptions.map((optionId) => (
                                <MenuItem key={optionId} value={optionId}>
                                    {optionId}
                                </MenuItem>
                            ))}

                        </Select>
                    </FormControl>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleEditDialogClose} color="secondary">
                        Cancel
                    </Button>
                    <Button onClick={handleEditSubmit} color="primary">
                        Save
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
