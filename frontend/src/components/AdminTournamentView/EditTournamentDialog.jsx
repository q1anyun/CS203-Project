import { Typography, TextField, Select, MenuItem, Dialog, DialogActions, DialogContent, DialogTitle, Button, InputLabel, FormControl, Grid2, FormHelperText } from '@mui/material';
import axios from 'axios';
import styles from './AdminTournamentView.module.css';
import useHandleError from '../Hooks/useHandleError';

function EditTournamentDialog({
    tournamentURL,
    token,
    updateTournament,
    timeControlOptions,
    roundTypeOptions,
    errors,
    setErrors,
    eloError,
    createFormError,
    setCreateFormError,
    validateForm,
    editDialogOpen,
    setUpdateTournament,
    setEditDialogOpen,
    tournamentToEdit,
    tournaments,
    setTournaments
}) {
    const handleError = useHandleError();

    const handleEditSubmit = async () => {
        console.log(updateTournament.tournamentType, updateTournament.maxPlayers);
        if (validateForm(updateTournament)) {
            const updatedTournamentData = {
                ...updateTournament,
            };

            try {
                const response = await axios.put(`${tournamentURL}/${tournamentToEdit.id}`, updatedTournamentData, {
                    headers: {
                        'Authorization': `Bearer ${token}`, 
                    }
                });

                const updatedTournaments = tournaments.map(t =>
                    t.tournamentId === tournamentToEdit.id ? response.data : t
                );

                setTournaments(updatedTournaments);
                setEditDialogOpen(false);
                window.location.reload();

            } catch (error) {
                handleError(error);
            }
        }
    };
    const handleEditDialogClose = () => {
        setCreateFormError('');
        setErrors({});
        setEditDialogOpen(false);
    };
    const handleEditInputChange = (e) => {
        const { name, value } = e.target;
        setUpdateTournament(prevState => ({
            ...prevState,
            [name]: value
        }));
    };
    return (
        <Dialog open={editDialogOpen} onClose={handleEditDialogClose}>

            <DialogTitle>
                <Typography variant="header3" sx={{ mb: 2 }}>Edit Tournament</Typography>
            </DialogTitle>
            <DialogContent>
                <Grid2 container spacing={2}>
                    <Grid2 size={12}>
                        <TextField
                            name="name"
                            label="Name"
                            value={updateTournament.name}
                            onChange={handleEditInputChange}
                            fullWidth
                        />
                    </Grid2>

                    <Grid2 size={12}>
                        <TextField
                            name="description"
                            label="Description"
                            value={updateTournament.description}
                            onChange={handleEditInputChange}
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
                            value={updateTournament.startDate}
                            onChange={handleEditInputChange}
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
                            value={updateTournament.endDate}
                            onChange={handleEditInputChange}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            fullWidth
                        />
                    </Grid2>

                    <Grid2 size={12}>
                        <FormControl fullWidth>
                            <InputLabel>Time Control</InputLabel>
                            <Select
                                name="timeControl"
                                label="Time Control"
                                value={updateTournament?.timeControl || ''}
                                onChange={handleEditInputChange}
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
                            value={updateTournament.minElo}
                            onChange={handleEditInputChange}
                            fullWidth
                        />
                    </Grid2>

                    <Grid2 size={12}>
                        <TextField
                            name="maxElo"
                            label="Max ELO"
                            type="number"
                            value={updateTournament.maxElo}
                            onChange={handleEditInputChange}
                            fullWidth
                            error={!!eloError && updateTournament.maxElo < updateTournament.minElo}
                            helperText={!!eloError && updateTournament.maxElo < updateTournament.minElo ? "Max ELO must be greater than Min ELO." : ""}
                        />
                    </Grid2>
                    <Grid2 size={12}>
                        <FormControl fullWidth>
                            <InputLabel>Tournament Type</InputLabel>
                            <Select
                                name="tournamentType"
                                label="Tournament Type"
                                value={updateTournament.tournamentType}
                                onChange={handleEditInputChange}
                            >
                                <MenuItem value="1">Knockout</MenuItem>
                                <MenuItem value="2">Swiss</MenuItem>
                            </Select>
                        </FormControl>
                    </Grid2>
                    {updateTournament.tournamentType && (
                        <Grid2 size={12}>
                            <FormControl fullWidth error={!!errors.maxPlayers}>
                                <InputLabel>Max Players</InputLabel>
                                <Select
                                    name="maxPlayers"
                                    label="Max Players"
                                    value={updateTournament.maxPlayers}
                                    onChange={handleEditInputChange}
                                >
                                    {roundTypeOptions
                                        .filter((optionId) => {
                                            if (updateTournament.tournamentType === "1") {
                                                return true;
                                            } else if (updateTournament.tournamentType === "2") {
                                                return optionId !== 2 && optionId !== 4;
                                            }
                                            return true;
                                        })
                                        .map((optionId) => (
                                            <MenuItem key={optionId} value={optionId}>
                                                {optionId}
                                            </MenuItem>
                                        ))}
                                </Select>
                            </FormControl>
                        </Grid2>
                    )}
                
                    <Grid2 size={12}>
                        <FormControl fullWidth>
                            <InputLabel>Format</InputLabel>
                            <Select
                                name="format"
                                label="Format"
                                value={updateTournament.format}
                                onChange={handleEditInputChange}
                            >
                                <MenuItem value="ONLINE">Online</MenuItem>
                                <MenuItem value="HYBRID">Hybrid</MenuItem>
                                <MenuItem value="PHYSICAL">Physical</MenuItem>
                            </Select>
                        </FormControl>
                    </Grid2>

                    {(updateTournament.format === 'PHYSICAL'  || updateTournament.format === 'HYBRID') && (
                        <>
                            <Grid2 size={12}>
                                <TextField
                                    name="locationAddress"
                                    label="Location Address"
                                    value={updateTournament.locationAddress}
                                    onChange={handleEditInputChange}
                                    fullWidth
                                />
                            </Grid2>

                            <Grid2 size={6}>
                                <TextField
                                    name="locationLatitude"
                                    label="Latitude (Optional)"
                                    value={updateTournament.locationLatitude}
                                    onChange={handleEditInputChange}
                                    fullWidth
                                    type="number"
                                />
                            </Grid2>

                            <Grid2 size={6}>
                                <TextField
                                    name="locationLongitude"
                                    label="Longitude (Optional)"
                                    value={updateTournament.locationLongitude}
                                    onChange={handleEditInputChange}
                                    fullWidth
                                />
                            </Grid2>
                        </>
                    )}

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
                <Button onClick={handleEditDialogClose} color="secondary">
                    Cancel
                </Button>
                <Button onClick={handleEditSubmit} color="primary">
                    Save
                </Button>
            </DialogActions>
        </Dialog>
    );
}
export default EditTournamentDialog;