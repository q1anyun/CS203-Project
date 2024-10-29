// Knockout.js
import React from 'react';
import { Bracket, Seed, SeedItem, SeedTeam } from 'react-brackets';
import { Typography, Dialog, Select, MenuItem, DialogTitle, DialogContent, DialogActions, Button, IconButton } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';

const CustomSeed = ({ seed, handleEditWinner }) => {
    const winnerId = seed.winnerId;

    // Check if both player1Id and player2Id are null and winnerId exists (auto-advance case)
    const isAutoAdvance = !seed.teams[0]?.id && !seed.teams[1]?.id && winnerId !== null;

    return (
        <Seed style={{ fontSize: 20, justifyContent: 'center', alignItems: 'center', color: 'white' }}>
            <SeedItem>
                <div>
                    {isAutoAdvance ? (
                        <SeedTeam style={{ backgroundColor: 'green' }}>
                            <Typography variant="header3" component="span" style={{ color: 'white' }}>
                                Auto Advance PLAYER {winnerId}
                            </Typography>
                        </SeedTeam>
                    ) : (
                        <>
                            <SeedTeam style={{ backgroundColor: winnerId === seed.teams[0]?.id ? 'green' : 'white' }}>
                                <Typography variant="playerProfile2" component="span" style={{ color: winnerId === seed.teams[0]?.id ? 'white' : 'black' }}>
                                    {seed.teams[0]?.name || 'Pending'}
                                </Typography>
                            </SeedTeam>
                            <SeedTeam style={{ backgroundColor: winnerId === seed.teams[1]?.id ? 'green' : 'white' }}>
                                <Typography variant="playerProfile2" component="span" style={{ color: winnerId === seed.teams[1]?.id ? 'white' : 'black' }}>
                                    {seed.teams[1]?.name || 'Pending'}
                                </Typography>
                            </SeedTeam>

                            {/* Only show the edit icon if there's no winner yet */}
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

const Knockout = ({ rounds, handleEditWinner, winner, setWinner, selectedTeams, open, handleCloseEdit, handleSaveWinner }) => {
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
                        sx={{
                            width: '300px',
                            height: '50px',
                            fontSize: '18px',
                            padding: '10px',
                        }}>
                        <MenuItem value={selectedTeams[0]?.id}>{selectedTeams[0]?.name}</MenuItem>
                        <MenuItem value={selectedTeams[1]?.id}>{selectedTeams[1]?.name}</MenuItem>
                    </Select>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseEdit}>Cancel</Button>
                    <Button onClick={handleSaveWinner} color="primary">Save</Button>
                </DialogActions>
            </Dialog>
        </>
    );
};

export default Knockout;
