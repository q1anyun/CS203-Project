import React, { useState, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Typography, IconButton, TextField, Select, MenuItem, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Button, Fab, CircularProgress, InputLabel, FormControl, Box, Grid2 } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import axios from 'axios';
import styles from './AdminTournamentView.module.css';
import { useNavigate } from 'react-router-dom';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs from 'dayjs';

import DeleteConfirmationDialog from './DeleteConfirmationDialog';
import EditTournamentDialog from './EditTournamentDialog';


const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
const gameTypeURL = import.meta.env.VITE_TOURNAMENT_GAMETYPE_URL;
const roundTypeURL = import.meta.env.VITE_TOURNAMENT_ROUNDTYPE_URL;

const statusColorMap = {
    LIVE: 'success',
    UPCOMING: 'warning',
    EXPIRED: 'default',
};

const StyledTableCell = styled(TableCell)(({ theme }) => ({
    '&:first-of-type': {
        textAlign: 'center',
    },
    '&:last-of-type': {
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
    const [tournamentToEdit, setTournamentToEdit] = useState([]);
    const [tournamentToDelete, setTournamentToDelete] = useState(null);
    const [createDialogOpen, setCreateDialogOpen] = useState(false);
    const [editDialogOpen, setEditDialogOpen] = useState(false);
    const [timeControlOptions, setTimeControlOptions] = useState([]);
    const [roundTypeOptions, setRoundTypeOptions] = useState([]);
    const [errors, setErrors] = useState({});
    const [createFormError, setCreateFormError] = useState('');
    const [eloError, setEloError] = useState('');
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
        startDate: null,
        endDate: null,
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

    const resetNewTournament = () => {
        setNewTournament({
            name: '',
            startDate: '',
            endDate: '',
            timeControl: '',
            minElo: '',
            maxElo: '',
            maxPlayers: '',
        });
    };

    //set errors

    const validateForm = (tournament) => {
        // Check if any required field is empty
        const isAnyFieldEmpty = Object.keys(tournament).some((key) => {
            return !tournament[key];  // Returns true if any field is empty
        });

        // If any required field is missing, set the error message
        if (isAnyFieldEmpty) {
            setCreateFormError('Please fill up all required fields');
            return false;
        }

        const { minElo, maxElo } = tournament;
        if (maxElo < minElo) {
            setEloError('Max ELO must be greater than Min ELO.');
            setCreateFormError('');
            return false;
        }

        // Clear error and return true if validation passes
        setEloError('');
        setCreateFormError('');
        return true;
    };

    const handleViewDetails = (id) => {
        // Navigate to /tournament/id route
        navigate(`/admin/tournaments/${id}`);
    };

    useEffect(() => {
        // Fetch all tournaments from the backend
        const fetchTournaments = async () => {
            try {
                const response = await axios.get(`${baseURL}`);
                console.log(response.data);
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

    const handleEditClick = async (tournamentId) => {
        // Set the tournament ID to edit


        try {
            // Fetch tournament data using axios
            const response = await axios.get(`${baseURL}/${tournamentId}`);


            // Log the fetched data
            console.log(response.data);
            setTournamentToEdit(response.data);
            const timeControlOption = timeControlOptions.find(option => option.name === response.data.timeControl.name) || '';

            setUpdateTournament({
                name: response.data.name || '',
                startDate: response.data.startDate
                    ? new Date(response.data.startDate + 'Z')
                    : null,
                endDate: response.data.endDate
                    ? new Date(response.data.endDate + 'Z')
                    : null,
                timeControl: timeControlOption.id || '',
                minElo: response.data.minElo || '',
                maxElo: response.data.maxElo || '',
                maxPlayers: response.data.maxPlayers || '',
            });



            // Open the edit dialog after fetching the data
            setEditDialogOpen(true);

        } catch (error) {
            console.error('Error fetching tournament data:', error);
        }
    };

    const handleCreate = () => {
        setCreateDialogOpen(true);
    };

    const handleCreateDialogClose = () => {
        setCreateFormError('');
        resetNewTournament();
        setCreateDialogOpen(false);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewTournament({
            ...newTournament,
            [name]: value,
        });
    };

    const handleDateChange = (name, newValue) => {
        const localDate = newValue instanceof Date ? newValue : new Date(newValue);
        const localISOString = localDate ? localDate.toISOString() : '';
        setNewTournament({
            ...newTournament,
            [name]: localISOString,
        });
    };

    const handleCreateSubmit = async () => {

        if (validateForm(newTournament)) {
            const newTournamentData = {
                ...newTournament,
            };

            console.log("New Tournament Data:", newTournamentData);

            try {
                const response = await axios.post(`${baseURL}`, newTournamentData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    }
                });
                setTournaments([response.data, ...tournaments]);
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
        }


    };

   



    if (loading) {
        return <CircularProgress />;
    }

    if (error) {
        return <Typography color="error">Error loading tournaments: {error.message}</Typography>;
    }

    return (
        <div className={styles.container}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant="header1" component="h2" className={styles.title}>
                    All Tournaments
                </Typography>
                <Fab color="primary" aria-label="add" onClick={handleCreate} className={styles.fab} sx={{ ml: 2 }} >
                    <AddIcon />
                </Fab>
            </Box>
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
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tournaments.map((tournament, rowIndex) => (
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
                                    <>
                                        <IconButton
                                            onClick={() => handleEditClick(tournament.id)}
                                            disabled={tournament.status !== "UPCOMING"} // Disable if status is "Live"
                                        >
                                            <EditIcon />
                                        </IconButton>

                                        <IconButton onClick={() => handleDeleteClick(tournament.id)}>
                                            <DeleteIcon />
                                        </IconButton>
                                        <IconButton onClick={() => handleViewDetails(tournament.id)}>
                                            <VisibilityIcon />
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
                <DialogTitle>
                    <Typography variant="header3">
                        Create New Tournament
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <Grid2 container spacing={2}>
                        <Grid2 size={12}>
                            <TextField
                                name="name"
                                label="Tournament Name"
                                value={newTournament.name}
                                onChange={handleInputChange}
                                fullWidth
                            />
                        </Grid2>

                        <Grid2 container spacing={4}>
                            <Grid2 size={6}>
                                <LocalizationProvider dateAdapter={AdapterDayjs}>
                                    <DateTimePicker
                                        name="startDate"
                                        label="Start Date Time"
                                        value={newTournament.startDate ? dayjs(newTournament.startDate) : null}
                                        onAccept={(newValue) => handleDateChange("startDate", newValue)} />
                                </LocalizationProvider>
                            </Grid2>

                            <Grid2 size={6}>
                                <LocalizationProvider dateAdapter={AdapterDayjs}>
                                    <DateTimePicker
                                        name="endDate"
                                        label="End Date Time"
                                        value={newTournament.endDate ? dayjs(newTournament.endDate) : null}
                                        onAccept={(newValue) => handleDateChange("endDate", newValue)}  // Only update when accepted
                                    />
                                </LocalizationProvider>
                            </Grid2>
                        </Grid2>
                        <Grid2 size={12}>
                            <FormControl fullWidth error={!!errors.timeControl}>
                                <InputLabel>Time Control</InputLabel>
                                <Select
                                    name="timeControl"
                                    label="Time Control"
                                    value={newTournament.timeControl}
                                    onChange={handleInputChange}
                                    className="text-left"
                                >
                                    {timeControlOptions.map((option) => (
                                        <MenuItem key={option.id} value={option.id}>
                                            {option.name}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </Grid2>

                        <Grid2 size={12}>
                            <TextField
                                name="minElo"
                                label="Min ELO"
                                type="number"
                                value={newTournament.minElo}
                                onChange={handleInputChange}
                                fullWidth
                            />
                        </Grid2>

                        <Grid2 size={12}>
                            <TextField
                                name="maxElo"
                                label="Max ELO"
                                type="number"
                                value={newTournament.maxElo}
                                onChange={handleInputChange}
                                fullWidth
                                error={!!eloError && newTournament.maxElo < newTournament.minElo}
                                helperText={!!eloError && newTournament.maxElo < newTournament.minElo ? "Max ELO must be greater than Min ELO." : ""}
                            />
                        </Grid2>
                        <Grid2 size={12}>
                            <FormControl fullWidth error={!!errors.maxPlayers}>
                                <InputLabel>Max Players</InputLabel>
                                <Select
                                    name="maxPlayers"
                                    label="Max Players"
                                    value={newTournament.maxPlayers}
                                    onChange={handleInputChange}
                                >
                                    {roundTypeOptions.map((optionId) => (
                                        <MenuItem key={optionId} value={optionId}>
                                            {optionId}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                            {createFormError && (
                                <Grid2 size={12}>
                                    <h6 className={styles.errorMessage}>
                                        {createFormError}
                                    </h6>
                                </Grid2>
                            )}
                        </Grid2>
                    </Grid2>
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

            <EditTournamentDialog
                baseURL={baseURL}
                token={token}
                updateTournament={updateTournament}
                timeControlOptions={timeControlOptions}
                roundTypeOptions={roundTypeOptions}
                errors={errors}
                setErrors={setErrors}
                eloError={eloError}
                createFormError={createFormError}
                setCreateFormError={setCreateFormError}
                validateForm={validateForm}
                editDialogOpen={editDialogOpen}
                setUpdateTournament={setUpdateTournament}
                setEditDialogOpen={setEditDialogOpen}
                tournamentToEdit={tournamentToEdit}
                tournaments={tournaments}
                setTournaments={setTournaments}
            />

           

            <DeleteConfirmationDialog
                open={deleteDialogOpen}
                baseURL={baseURL}
                token={token}
                tournamentToDelete={tournamentToDelete}
                setTournaments={setTournaments}
                setDeleteDialogOpen={setDeleteDialogOpen}
                setTournamentToDelete={setTournamentToDelete}
                tournaments={tournaments}
            />
        </div>
    );
}