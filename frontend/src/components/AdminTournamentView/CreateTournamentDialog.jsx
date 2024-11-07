import React from 'react';
import { Typography, TextField, Select, MenuItem, Dialog, DialogActions, DialogContent, DialogTitle, Button, InputLabel, FormControl, Grid2, FormHelperText } from '@mui/material';
import axios from 'axios';
import styles from './AdminTournamentView.module.css';
import { useNavigate } from 'react-router-dom';

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
    maxPlayerError,
    createFormError,
    setCreateFormError,
    setTournaments,
    tournamentURL,
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
                const response = await axios.post(`${tournamentURL}`, newTournamentData, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    }
                });
                setTournaments((prevTournaments) => [response.data, ...prevTournaments]);
                setCreateDialogOpen(false);
                window.location.reload();
            } catch (error) {
                if (error.response) {
                    console.log(error.message);
                    const statusCode = error.response.status;
                    const errorMessage = error.response.data?.message || 'An unexpected error occurred';
                    navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
                } else if (err.request) {
                    navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
                } else {
                    navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + err.message)}`);
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

    const navigate = useNavigate();

    return (
        <Dialog open={createDialogOpen} onClose={handleCreateDialogClose} maxWidth="sm">
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

                    <Grid2 size={12}>
                        <TextField
                            name="description"
                            label="Description"
                            value={newTournament.description}
                            onChange={handleInputChange}
                            multiline
                            rows={3}
                            fullWidth
                        />
                    </Grid2>

                    <Grid2 size={6}>
                        <TextField
                            name="startDate"
                            label="Start Date"
                            type="date"
                            value={newTournament.startDate}
                            onChange={handleInputChange}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            fullWidth
                        />
                    </Grid2>

                    <Grid2 size={6}>
                        <TextField
                            name="endDate"
                            label="End Date"
                            type="date"
                            value={newTournament.endDate}
                            onChange={handleInputChange}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            fullWidth
                        />
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
                            {(!!maxPlayerError && newTournament.tournamentType === "2" && newTournament.maxPlayers < 8) && (
                                <FormHelperText error={true}>Max Players must be greater than 8 for swiss tournaments.</FormHelperText>
                            )}
                        </FormControl>
                    </Grid2>
                    <Grid2 size={12}>
                        <FormControl fullWidth>
                            <InputLabel>Format</InputLabel>
                            <Select
                                name="format"
                                label="Format"
                                value={newTournament.format}
                                onChange={handleInputChange}
                            >
                                <MenuItem value="ONLINE">Online</MenuItem>
                                <MenuItem value="HYBRID">Hybrid</MenuItem>
                                <MenuItem value="PHYSICAL">Physical</MenuItem>
                            </Select>
                        </FormControl>
                    </Grid2>

                    {(newTournament.format === 'PHYSICAL' || newTournament.format === 'HYBRID') && (
                        <>
                            <Grid2 size={12}>
                                <TextField
                                    name="locationAddress"
                                    label="Location Address"
                                    value={newTournament.locationAddress}
                                    onChange={handleInputChange}
                                    fullWidth
                                />
                            </Grid2>

                            <Grid2 size={6}>
                                <TextField
                                    name="locationLatitude"
                                    label="Latitude (Optional)"
                                    value={newTournament.locationLatitude}
                                    onChange={handleInputChange}
                                    type="number"
                                    fullWidth
                                />
                            </Grid2>

                            <Grid2 size={6}>
                                <TextField
                                    name="locationLongitude"
                                    label="Longitude (Optional)"
                                    value={newTournament.locationLongitude}
                                    onChange={handleInputChange}
                                    type="number"
                                    fullWidth
                                />
                            </Grid2>
                        </>
                    )}

                    <Grid2 size={12}>
                        <FormControl fullWidth>
                            <InputLabel>Tournament Type</InputLabel>
                            <Select
                                name="tournamentType"
                                label="Tournament Type"
                                value={newTournament.tournamentType}
                                onChange={handleInputChange}
                            >
                                <MenuItem value="1">Knockout</MenuItem>
                                <MenuItem value="2">Swiss</MenuItem>
                            </Select>
                        </FormControl>
                    </Grid2>

                    {createFormError && (
                        <Grid2 size={12}>
                            <h6 className={styles.errorMessage}>
                                {createFormError}
                            </h6>
                        </Grid2>
                    )}
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
    );
};

export default CreateTournamentDialog;
