import React from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Grid,
    TextField,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Button,
    Typography,
} from '@mui/material';
import { LocalizationProvider, DateTimePicker } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs from 'dayjs';
import styles from './YourStyles.module.css'; // Adjust the path as needed

const EditTournamentDialog = ({
    open,
    onClose,
    tournament,
    onChange,
    timeControlOptions,
    createFormError,
    onSubmit,
}) => {
    return (
        <Dialog open={open} onClose={onClose}>
            <DialogTitle>
                <Typography variant="header3" sx={{ mb: 2 }}>Edit Tournament</Typography>
            </DialogTitle>
            <DialogContent>
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <TextField
                            name="name"
                            label="Name"
                            value={tournament.name}
                            onChange={onChange}
                            fullWidth
                        />
                    </Grid>
                    <Grid container spacing={4}>
                        <Grid item xs={6}>
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker
                                    name="startDate"
                                    label="Start Date Time"
                                    value={tournament.startDate ? dayjs(tournament.startDate) : null}
                                    onAccept={(newValue) => onChange({ target: { name: 'startDate', value: newValue } })} 
                                />
                            </LocalizationProvider>
                        </Grid>

                        <Grid item xs={6}>
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker
                                    name="endDate"
                                    label="End Date Time"
                                    value={tournament.endDate ? dayjs(tournament.endDate) : null}
                                    onAccept={(newValue) => onChange({ target: { name: 'endDate', value: newValue } })}  
                                />
                            </LocalizationProvider>
                        </Grid>
                    </Grid>

                    <Grid item xs={12}>
                        <FormControl fullWidth>
                            <InputLabel>Time Control</InputLabel>
                            <Select
                                name="timeControl"
                                label="Time Control"
                                value={tournament.timeControl || ''}
                                onChange={onChange}
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
                            value={tournament.minElo}
                            onChange={onChange}
                            fullWidth
                            error={tournament.minElo < 0}
                            helperText={tournament.minElo < 0 ? "Min Elo must be more than 0." : ""}
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <TextField
                            name="maxElo"
                            label="Max ELO"
                            type="number"
                            value={tournament.maxElo}
                            onChange={onChange}
                            fullWidth
                            error={tournament.maxElo < tournament.minElo}
                            helperText={tournament.maxElo < tournament.minElo ? "Max ELO must be greater than Min ELO." : ""}
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <FormControl fullWidth margin="dense">
                            <InputLabel>Max Players</InputLabel>
                            <Select
                                name="maxPlayers"
                                label="Max Players"
                                value={tournament.maxPlayers || ''}
                                onChange={onChange}
                            >
                                {/* Assuming roundTypeOptions is available in props */}
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
                <Button onClick={onClose} color="secondary">
                    Cancel
                </Button>
                <Button onClick={onSubmit} color="primary"> 
                    Save
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default EditTournamentDialog;
