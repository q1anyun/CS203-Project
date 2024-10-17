import React from 'react';
import { Table, TableBody, TableContainer, TableHead, TableRow, Typography, Chip, IconButton, Box, Fab, Paper } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import AddIcon from '@mui/icons-material/Add'; 
import styles from './TournamentTable.module.css'; 

const TournamentTable = ({ tournaments, handleCreate, handleEditClick, handleDeleteClick, handleViewDetails}) => {
    const statusColorMap = {
        LIVE: 'success',
        UPCOMING: 'warning',
        EXPIRED: 'default',
    };

    const StyledTableCell = styled(TableCell)(({ theme }) => ({
        '&:first-of-type': {
            textAlign: 'center',
        },
        '&:last-of-type': {
            textAlign: 'center',
        },
    }));

    const StyledTableRow = styled(TableRow)(({ theme }) => ({
        '&:nth-of-type(odd)': {
            backgroundColor: theme.palette.action.hover,
        },
    }));
    return (
        <div>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant="h4" component="h2" className={styles.title}>
                    All Tournaments
                </Typography>
                <Fab color="primary" aria-label="add" onClick={handleCreate} className={styles.fab} sx={{ ml: 2 }} >
                    <AddIcon />
                </Fab>
            </Box>
            <TableContainer component={Paper} className={styles.table}>
                <Table sx={{ minWidth: 700 }} aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell><Typography variant="h6">ID</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="h6">Name</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="h6">Start DateTime</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="h6">End DateTime</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="h6">Time Control</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="h6">Min ELO</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="h6">Max ELO</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="h6">Players</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="h6">Status</Typography></StyledTableCell>
                            <StyledTableCell><Typography variant="h6">Actions</Typography></StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tournaments.map((tournament, rowIndex) => (
                            <StyledTableRow key={tournament.id}>
                                <StyledTableCell><Typography variant="body1">{tournament.id}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body1">{tournament.name}</Typography></StyledTableCell>
                                <StyledTableCell>
                                    <Typography variant="body1">
                                        {new Date(tournament.startDate + "Z").toLocaleString('en-GB', {
                                            timeZone: 'Asia/Singapore',
                                            year: 'numeric',
                                            month: '2-digit',
                                            day: '2-digit',
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        })}
                                    </Typography>
                                </StyledTableCell>

                                <StyledTableCell>
                                    <Typography variant="body1">
                                        {new Date(tournament.endDate + "Z").toLocaleString('en-GB', {
                                            timeZone: 'Asia/Singapore',
                                            year: 'numeric',
                                            month: '2-digit',
                                            day: '2-digit',
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        })}
                                    </Typography>
                                </StyledTableCell>
                                <StyledTableCell><Typography variant="body1">{tournament.timeControl.timeControlMinutes}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body1">{tournament.minElo}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body1">{tournament.maxElo}</Typography></StyledTableCell>
                                <StyledTableCell><Typography variant="body1">{tournament.maxPlayers}</Typography></StyledTableCell>
                                <StyledTableCell>
                                    <Chip label={tournament.status} color={statusColorMap[tournament.status]} />
                                </StyledTableCell>
                                <StyledTableCell>
                                    <>
                                        <IconButton
                                            onClick={() => handleEditClick(tournament.id)}
                                            disabled={tournament.status !== "UPCOMING"} // Disable if status is "Live"
                                        >
                                            <EditIcon />
                                        </IconButton>

                                        <IconButton onClick={() => handleDeleteClick(tournament.id)}>
                                            <DeleteIcon />
                                        </IconButton>
                                        <IconButton onClick={() => handleViewDetails(tournament.id)}>
                                            <VisibilityIcon />
                                        </IconButton>
                                    </>
                                </StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer> 
        </div>
    );
};

export default TournamentTable;
