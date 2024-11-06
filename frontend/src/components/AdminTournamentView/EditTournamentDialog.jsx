import { Typography, TextField, Select, MenuItem, Dialog, DialogActions, DialogContent, DialogTitle, Button, InputLabel, FormControl, Grid2, FormHelperText } from '@mui/material';
import axios from 'axios';
import styles from './AdminTournamentView.module.css';
import { useNavigate } from 'react-router-dom';

function EditTournamentDialog({
    baseURL,
    token,
    updateTournament,
    timeControlOptions,
    roundTypeOptions,
    errors,
    setErrors,
    eloError,
    maxPlayerError,
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
    const navigate = useNavigate();

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

                setUpdateTournament({
                    name: '',
                    startDate: '',
                    endDate: '',
                    timeControl: '',
                    minElo: '',
                    maxElo: '',
                    maxPlayers: '',
                    description: '',
                    tournamentType: '',
                    format: ''
                });

                setEditDialogOpen(false);

                window.location.reload();
            } catch (error) {
                if (error.response) {
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
                            {(!!maxPlayerError && updateTournament.tournamentType === "2" && updateTournament.maxPlayers < 8) && (
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
                                value={updateTournament.format}
                                onChange={handleEditInputChange}
                            >
                                <MenuItem value="ONLINE">Online</MenuItem>
                                <MenuItem value="PHYSICAL">Physical</MenuItem>
                            </Select>
                        </FormControl>
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