import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, FormControl, InputLabel, Select, MenuItem, Grid } from '@mui/material';
import { LocalizationProvider, DateTimePicker } from '@mui/x-date-pickers';
import AdapterDayjs from '@mui/x-date-pickers/AdapterDayjs';

const CreateTournamentDialog = ({ open, handleClose, newTournament, handleInputChange, handleCreateSubmit, timeControlOptions, errors }) => {
    return (
        <Dialog open={open} onClose={handleClose}>
            <DialogTitle>Create New Tournament</DialogTitle>
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
                                    onAccept={(newValue) => handleInputChange("startDate", newValue)} />
                            </LocalizationProvider>
                        </Grid>
                        <Grid item xs={6}>
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker
                                    name="endDate"
                                    label="End Date Time"
                                    value={newTournament.endDate ? dayjs(newTournament.endDate) : null}
                                    onAccept={(newValue) => handleInputChange("endDate", newValue)} />
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
                                onChange={handleInputChange}>
                                {timeControlOptions.map((option) => (
                                    <MenuItem key={option.id} value={option.id}>
                                        {option.name}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Grid>
                    {/* Add other fields like Min ELO, Max ELO, and Max Players here */}
                </Grid>
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose} color="secondary">Cancel</Button>
                <Button onClick={handleCreateSubmit} color="primary">Create</Button>
            </DialogActions>
        </Dialog>
    );
};

export default CreateTournamentDialog;
