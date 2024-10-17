import React from 'react';
import { Typography, TextField, Select, MenuItem, Dialog, DialogActions, DialogContent, DialogTitle, Button, InputLabel, FormControl, Grid } from '@mui/material'; // Import necessary MUI components
import axios from 'axios';
import styles from './AdminTournamentView.module.css'; 
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'; 
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'; 
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'; 
import dayjs from 'dayjs';



function CreateTournamentDialog({
    createDialogOpen,
    setCreateDialogOpen,
    newTournament,
    setNewTournament,
    resetNewTournament,
    timeControlOptions,
    roundTypeOptions,
    validateForm,
    errors,
    eloError,
    createFormError,
    setCreateFormError,
    setTournaments,
    baseURL,
    token,
}) {
    const handleCreateDialogClose = () => {
        setCreateFormError('');
        resetNewTournament();
        setCreateDialogOpen(false);
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
                setTournaments((prevTournaments) => [response.data, ...prevTournaments]);
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
            }
        }
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

    return (
        <Dialog open={createDialogOpen} onClose={handleCreateDialogClose}>
            <DialogTitle>
                <Typography variant="header3">
                    Create New Tournament
                </Typography>
            </DialogTitle>
            <DialogContent>
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <TextField
                            name="name"
                            label="Tournament Name"
                            value={newTournament.name}
                            onChange={handleInputChange}
                            fullWidth
                        />
                    </Grid>

                    <Grid container spacing={4}>
                        <Grid item xs={6}>
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker
                                    name="startDate"
                                    label="Start Date Time"
                                    value={newTournament.startDate ? dayjs(newTournament.startDate) : null}
                                    onAccept={(newValue) => handleDateChange("startDate", newValue)} />
                            </LocalizationProvider>
                        </Grid>

                        <Grid item xs={6}>
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker
                                    name="endDate"
                                    label="End Date Time"
                                    value={newTournament.endDate ? dayjs(newTournament.endDate) : null}
                                    onAccept={(newValue) => handleDateChange("endDate", newValue)}  // Only update when accepted
                                />
                            </LocalizationProvider>
                        </Grid>
                    </Grid>
                    <Grid item xs={12}>
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
                    </Grid>

                    <Grid item xs={12}>
                        <TextField
                            name="minElo"
                            label="Min ELO"
                            type="number"
                            value={newTournament.minElo}
                            onChange={handleInputChange}
                            fullWidth
                        />
                    </Grid>

                    <Grid item xs={12}>
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
                    </Grid>
                    <Grid item xs={12}>
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
                            <Grid item xs={12}>
                                <h6 className={styles.errorMessage}>
                                    {createFormError}
                                </h6>
                            </Grid>
                        )}
                    </Grid>
                </Grid>
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
    );
};

export default CreateTournamentDialog;
