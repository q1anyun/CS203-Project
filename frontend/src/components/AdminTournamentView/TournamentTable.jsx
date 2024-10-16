import React from 'react';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography, Chip, IconButton } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';

const TournamentTable = ({ tournaments, handleEditClick, handleDeleteClick, handleViewDetails, statusColorMap }) => {
    return (
        <div>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant="header1" component="h2" className={styles.title}>
                    All Tournaments
                </Typography>
                <Fab color="primary" aria-label="add" onClick={handleCreate} className={styles.fab} sx={{ ml: 2 }} >
                    <AddIcon />
                </Fab>
            </Box>
            <TableContainer component={Paper}>
                <Table aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <TableCell><Typography variant="header4">ID</Typography></TableCell>
                            <TableCell><Typography variant="header4">Name</Typography></TableCell>
                            <TableCell><Typography variant="header4">Start DateTime</Typography></TableCell>
                            <TableCell><Typography variant="header4">End DateTime</Typography></TableCell>
                            <TableCell><Typography variant="header4">Time Control</Typography></TableCell>
                            <TableCell><Typography variant="header4">Min ELO</Typography></TableCell>
                            <TableCell><Typography variant="header4">Max ELO</Typography></TableCell>
                            <TableCell><Typography variant="header4">Players</Typography></TableCell>
                            <TableCell><Typography variant="header4">Status</Typography></TableCell>
                            <TableCell><Typography variant="header4">Actions</Typography></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tournaments.map((tournament) => (
                            <TableRow key={tournament.id}>
                                <TableCell><Typography variant="body4">{tournament.id}</Typography></TableCell>
                                <TableCell><Typography variant="body4">{tournament.name}</Typography></TableCell>
                                <TableCell><Typography variant="body4">{new Date(tournament.startDate + "Z").toLocaleString('en-GB')}</Typography></TableCell>
                                <TableCell><Typography variant="body4">{new Date(tournament.endDate + "Z").toLocaleString('en-GB')}</Typography></TableCell>
                                <TableCell><Typography variant="body4">{tournament.timeControl.timeControlMinutes}</Typography></TableCell>
                                <TableCell><Typography variant="body4">{tournament.minElo}</Typography></TableCell>
                                <TableCell><Typography variant="body4">{tournament.maxElo}</Typography></TableCell>
                                <TableCell><Typography variant="body4">{tournament.maxPlayers}</Typography></TableCell>
                                <TableCell><Chip label={tournament.status} color={statusColorMap[tournament.status]} /></TableCell>
                                <TableCell>
                                    <IconButton onClick={() => handleEditClick(tournament.id)} disabled={tournament.status !== "UPCOMING"}>
                                        <EditIcon />
                                    </IconButton>
                                    <IconButton onClick={() => handleDeleteClick(tournament.id)}>
                                        <DeleteIcon />
                                    </IconButton>
                                    <IconButton onClick={() => handleViewDetails(tournament.id)}>
                                        <VisibilityIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </div>
    );
};

export default TournamentTable;
