import React, { useState } from 'react';
import { Bracket, Seed, SeedItem, SeedTeam } from 'react-brackets';
import { Typography, Dialog, Select, MenuItem, DialogTitle, DialogContent, DialogActions, Button, IconButton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import EditIcon from '@mui/icons-material/Edit';
import axios from 'axios';
import useHandleError from '../Hooks/useHandleError';

const token = localStorage.getItem('token');
const baseURL = import.meta.env.VITE_MATCHMAKING_SERVICE_URL;

const CustomSeed = ({ seed, handleEditWinner }) => {
    const winnerId = seed.winnerId;
    const isAutoAdvance = !seed.teams[0]?.id && !seed.teams[1]?.id && winnerId !== null;

    return (
        <Seed style={{ fontSize: 20, justifyContent: 'center', alignItems: 'center', color: 'white' }}>
            <SeedItem>
                <div>
                    {isAutoAdvance ? (
                        <SeedTeam style={{ backgroundColor: 'green' }}>
                            <Typography variant="h6" component="span" style={{ color: 'white' }}>
                                Auto Advance PLAYER {winnerId}
                            </Typography>
                        </SeedTeam>
                    ) : (
                        <>
                            <SeedTeam style={{ backgroundColor: winnerId === seed.teams[0]?.id ? 'green' : 'white' }}>
                                <Typography variant="body1" component="span" style={{ color: winnerId === seed.teams[0]?.id ? 'white' : 'black' }}>
                                    {seed.teams[0]?.name || 'Pending'}
                                </Typography>
                            </SeedTeam>
                            <SeedTeam style={{ backgroundColor: winnerId === seed.teams[1]?.id ? 'green' : 'white' }}>
                                <Typography variant="body1" component="span" style={{ color: winnerId === seed.teams[1]?.id ? 'white' : 'black' }}>
                                    {seed.teams[1]?.name || 'Pending'}
                                </Typography>
                            </SeedTeam>
                            {winnerId === null && (
                                <IconButton onClick={() => handleEditWinner(seed.id, seed.teams)} aria-label="edit winner" sx={{ color: 'white' }}>
                                    <EditIcon />
                                </IconButton>
                            )}
                        </>
                    )}
                </div>
            </SeedItem>
        </Seed>
    );
};

const Knockout = ({ rounds }) => {
    const [selectedMatchId, setSelectedMatchId] = useState(null);
    const [selectedTeams, setSelectedTeams] = useState([]);
    const [open, setOpen] = useState(false);
    const [winner, setWinner] = useState('');
    const navigate = useNavigate();

    const handleEditWinner = (matchId, teams) => {
        setSelectedMatchId(matchId);
        setSelectedTeams(teams);
        setOpen(true);
    };

    const handleCloseEdit = () => {
        setOpen(false);
    };

    const handleSaveWinner = async () => {
        try {
            await axios.put(`${baseURL}/${selectedMatchId}/winner/${winner}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            setOpen(false);
            window.location.reload();
        } catch (error) {
            useHandleError(error);
        }
    };

    return (
        <>
            <Bracket
                rounds={rounds}
                renderSeedComponent={(props) => (
                    <CustomSeed {...props} handleEditWinner={handleEditWinner} />
                )}
                roundTitleComponent={(title) => (
                    <Typography variant="header3" align="center">
                        {title}
                    </Typography>
                )}
            />
            {/* Modal for editing winner */}
            <Dialog open={open} onClose={handleCloseEdit}>
                <DialogTitle>Edit Winner</DialogTitle>
                <DialogContent>
                    <Select
                        value={winner}
                        onChange={(e) => setWinner(e.target.value)}
                        sx={{ width: '300px', height: '50px', fontSize: '18px', padding: '10px' }}
                    >
                        {selectedTeams.map((team) => (
                            <MenuItem key={team.id} value={team.id}>
                                {team.name}
                            </MenuItem>
                        ))}
                    </Select>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseEdit}>Cancel</Button>
                    <Button onClick={handleSaveWinner} color="primary">
                        Save
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};

export default Knockout;
