import { Typography, TextField, Select, MenuItem,Dialog, DialogActions, DialogContent, DialogTitle, Button,InputLabel, FormControl, Grid2 } from '@mui/material';
import axios from 'axios';
import styles from './AdminTournamentView.module.css';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs from 'dayjs';

function EditTournamentDialog({
    baseURL,
    token,
    updateTournament,
    timeControlOptions,
    roundTypeOptions,
    errors,
    eloError,
    createFormError,
    editDialogOpen,
    setUpdateTournament,
    setEditDialogOpen,
    tournamentToEdit,
    setTournaments
}) {
    const handleEditSubmit = async () => {
    
        if (validateForm(updateTournament)) {
            const updatedTournamentData = {
                ...updateTournament,
            };
            console.log(updatedTournamentData);
    
            try {
                const response = await axios.put(`${baseURL}/${tournamentToEdit.id}`, updatedTournamentData, {
                    headers: {
                        'Authorization': `Bearer ${token}`, // Include JWT token
                    }
                });
                console.log(response.data); 
    
                const updatedTournaments = tournaments.map(t =>
                    t.tournamentId === tournamentToEdit.id ? response.data : t
                );
    
                setTournaments(updatedTournaments);
                console.log(tournaments); 
    
                setUpdateTournament({
                    name: '',
                    startDate: '',
                    endDate: '',
                    timeControl: '',
                    minElo: '',
                    maxElo: '',
                    maxPlayers: ''
                });
    
                setEditDialogOpen(false);
    
                window.location.reload();
            } catch (error) {
                if (error.response) {
                    console.error('Error data:', error.response.data);
                    console.error('Error status:', error.response.status);
                    console.error('Error headers:', error.response.headers);
                } else {
                    console.error('Error message:', error.message);
                }
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
    const handleEditDateChange = (name, newValue) => {
        const localDate = newValue instanceof Date ? newValue : new Date(newValue);
        const localISOString = localDate ? localDate.toISOString() : '';
        setUpdateTournament((prevState) => ({
            ...prevState,
            [name]: localISOString,
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
                    <Grid2 container spacing={4}>
                        <Grid2 size={6}>
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker
                                    name="startDate"
                                    label="Start Date Time"
                                    value={updateTournament.startDate ? dayjs(updateTournament.startDate) : null}
                                    onAccept={(newValue) => handleEditDateChange("startDate", newValue)} />
                            </LocalizationProvider>
                        </Grid2>

                        <Grid2 size={6}>
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker
                                    name="endDate"
                                    label="End Date Time"
                                    value={updateTournament.endDate ? dayjs(updateTournament.endDate) : null}
                                    onAccept={(newValue) => handleEditDateChange("endDate", newValue)}  // Only update when accepted
                                />
                            </LocalizationProvider>
                        </Grid2>
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
                        <FormControl fullWidth margin="dense" error={!!errors.maxPlayers}>
                            <InputLabel>Max Players</InputLabel>
                            <Select
                                name="maxPlayers"
                                label="Max Players"
                                value={updateTournament.maxPlayers || ''}
                                onChange={handleEditInputChange}
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
                <Button onClick={handleEditDialogClose} color="secondary">
                    Cancel
                </Button>
                <Button onClick={handleEditSubmit} color="primary" > 
                    Save
                </Button>
            </DialogActions>
        </Dialog>
    );
}
export default EditTournamentDialog;