import * as React from 'react'; 
import { useState } from 'react';
import { styled } from '@mui/material/styles';
import { useNavigate } from 'react-router-dom';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import { Typography, Grid2, Chip, IconButton, Fab, TextField, Dialog, DialogActions, DialogContent, DialogTitle, Button, Select, MenuItem } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import VisibilityIcon from '@mui/icons-material/Visibility';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import DeleteIcon from '@mui/icons-material/Delete';
import styles from './AdminTournamentView.module.css';

const tournamentsData = [
    {
        tournamentId: 100823,
        tournamentName: "Chess Masters",
        startDate: "2024-09-10",
        endDate: "2024-09-15",
        timeControl: "Rapid",
        numberOfPlayers: 10,
        status: "Expired",
    },
    {
        tournamentId: 200564,
        tournamentName: "Junior Championship",
        startDate: "2024-09-12",
        endDate: "2024-09-18",
        timeControl: "Blitz",
        numberOfPlayers: 8,
        status: "Upcoming",
    },
    {
        tournamentId: 200789,
        tournamentName: "Grand Slam",
        startDate: "2024-09-20",
        endDate: "2024-09-30",
        timeControl: "Classic",
        numberOfPlayers: 16,
        status: "Live",
    },
];

const statusColorMap = {
    Live: 'success',
    Upcoming: 'warning',
    Expired: 'default',
};

const StyledTableCell = styled(TableCell)(({ theme }) => ({
    [`&.${tableCellClasses.head}`]: {
        backgroundColor: theme.palette.common.black,
        color: theme.palette.common.white,
        textAlign: 'center',
    },
    [`&.${tableCellClasses.body}`]: {
        fontSize: 14,
        textAlign: 'center',
    },
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
    '&:nth-of-type(odd)': {
        backgroundColor: theme.palette.action.hover,
    },
    '&:last-child td, &:last-child th': {
        border: 0,
    },
}));

export default function AdminTournamentView() {
    const navigate = useNavigate();
    const [tournaments, setTournaments] = useState(tournamentsData);
    const [editableRow, setEditableRow] = useState(null);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [tournamentToDelete, setTournamentToDelete] = useState(null);
    const [newTournament, setNewTournament] = useState({
        tournamentId: '',
        tournamentName: '',
        startDate: '',
        endDate: '',
        timeControl: 'Classic', // Default time control
        numberOfPlayers: '',
        status: 'Upcoming',
    });
    const [createDialogOpen, setCreateDialogOpen] = useState(false);

    const timeControlOptions = ["Blitz", "Rapid", "Classic"];

    const handleView = (tournamentId) => {
        navigate(`/tournamentdetails/${tournamentId}`);
    };

    const handleEdit = (rowIndex) => {
        setEditableRow(rowIndex);
    };

    const handleSave = (rowIndex) => {
        setEditableRow(null);
        // Implement save logic here, e.g., API call
    };

    const handleChange = (e, rowIndex, field) => {
        const newTournaments = [...tournaments];
        newTournaments[rowIndex][field] = e.target.value;
        setTournaments(newTournaments);
    };

    const handleDeleteClick = (tournamentId) => {
        setTournamentToDelete(tournamentId);
        setDeleteDialogOpen(true);
    };

    const handleDeleteConfirm = () => {
        const updatedTournaments = tournaments.filter(t => t.tournamentId !== tournamentToDelete);
        setTournaments(updatedTournaments);
        setDeleteDialogOpen(false);
        setTournamentToDelete(null);
    };

    const handleDeleteCancel = () => {
        setDeleteDialogOpen(false);
        setTournamentToDelete(null);
    };

    const handleCreate = () => {
        setCreateDialogOpen(true);
    };

    const handleCreateDialogClose = () => {
        setCreateDialogOpen(false);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewTournament({
            ...newTournament,
            [name]: value,
        });
    };

    const handleCreateSubmit = () => {
        const newTournamentData = {
            ...newTournament,
            tournamentId: Math.floor(Math.random() * 100000), // Simple ID generation
        };
        setTournaments([newTournamentData, ...tournaments]);
        setNewTournament({
            tournamentId: '',
            tournamentName: '',
            startDate: '',
            endDate: '',
            timeControl: 'Classic',
            numberOfPlayers: '',
            status: 'Upcoming',
        });
        setCreateDialogOpen(false);
    };

    return (
        <div>
            <Typography variant="h4" component="h2" gutterBottom className={styles.title}>
                All Tournaments
            </Typography> 
            <TableContainer component={Paper} className={styles.tableContainer}>
                <Table sx={{ minWidth: 700 }} aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell>Tournament ID</StyledTableCell>
                            <StyledTableCell>Tournament Name</StyledTableCell>
                            <StyledTableCell>Start Date</StyledTableCell>
                            <StyledTableCell>End Date</StyledTableCell>
                            <StyledTableCell>Time Control</StyledTableCell>
                            <StyledTableCell>Number of Players</StyledTableCell>
                            <StyledTableCell>Status</StyledTableCell>
                            <StyledTableCell>Actions</StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tournaments.map((tournament, rowIndex) => (
                            <StyledTableRow key={tournament.tournamentId}>
                                <StyledTableCell>{tournament.tournamentId}</StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <TextField
                                            value={tournament.tournamentName}
                                            onChange={(e) => handleChange(e, rowIndex, 'tournamentName')}
                                            variant="outlined"
                                            size="small"
                                        />
                                    ) : (
                                        tournament.tournamentName
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <TextField
                                            type="date"
                                            value={tournament.startDate}
                                            onChange={(e) => handleChange(e, rowIndex, 'startDate')}
                                            variant="outlined"
                                            size="small"
                                        />
                                    ) : (
                                        tournament.startDate
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <TextField
                                            type="date"
                                            value={tournament.endDate}
                                            onChange={(e) => handleChange(e, rowIndex, 'endDate')}
                                            variant="outlined"
                                            size="small"
                                        />
                                    ) : (
                                        tournament.endDate
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <Select
                                            value={tournament.timeControl}
                                            onChange={(e) => handleChange(e, rowIndex, 'timeControl')}
                                            variant="outlined"
                                            size="small"
                                        >
                                            {timeControlOptions.map(option => (
                                                <MenuItem key={option} value={option}>{option}</MenuItem>
                                            ))}
                                        </Select>
                                    ) : (
                                        tournament.timeControl
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <TextField
                                            type="number"
                                            value={tournament.numberOfPlayers}
                                            onChange={(e) => handleChange(e, rowIndex, 'numberOfPlayers')}
                                            variant="outlined"
                                            size="small"
                                            sx={{ width: '80px' }}
                                        />
                                    ) : (
                                        tournament.numberOfPlayers
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>
                                    <Chip label={tournament.status} variant="outlined" color={statusColorMap[tournament.status]} />
                                </StyledTableCell>
                                <StyledTableCell>
                                    {editableRow === rowIndex ? (
                                        <IconButton aria-label="save" color="primary" onClick={() => handleSave(rowIndex)}>
                                            <SaveIcon />
                                        </IconButton>
                                    ) : (
                                        <IconButton aria-label="edit" color="secondary" onClick={() => handleEdit(rowIndex)}>
                                            <EditIcon />
                                        </IconButton>
                                    )}
                                    <IconButton aria-label="view" color="primary" onClick={() => handleView(tournament.tournamentId)}>
                                        <VisibilityIcon />
                                    </IconButton>
                                    <IconButton aria-label="delete" color="error" onClick={() => handleDeleteClick(tournament.tournamentId)}>
                                        <DeleteIcon />
                                    </IconButton>
                                </StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <Fab 
                color="success" 
                onClick={handleCreate} 
                aria-label="add"
                style={{ marginBottom: '16px' }}
            >
                <AddIcon />
            </Fab>

            <Dialog open={createDialogOpen} onClose={handleCreateDialogClose}>
                <DialogTitle>Create New Tournament</DialogTitle>
                <DialogContent>
                    <Grid2 container spacing={2}>
                        {[
                            { label: "Tournament Name", name: "tournamentName", type: "text" },
                            { label: "Start Date", name: "startDate", type: "date" },
                            { label: "End Date", name: "endDate", type: "date" },
                            { label: "Time Control", name: "timeControl", type: "text" },
                            { label: "Number of Players", name: "numberOfPlayers", type: "number" },
                        ].map(({ label, name, type }) => (
                            <Grid2 container item xs={12} spacing={2} key={name} alignItems="center">
                                <Grid2 item xs={4}>
                                    <Typography variant="subtitle1">{label}</Typography>
                                </Grid2>
                                <Grid2 item xs={8} display="flex" justifyContent="flex-end">
                                    <TextField
                                        type={type}
                                        name={name}
                                        value={newTournament[name]}
                                        onChange={handleInputChange}
                                        fullWidth
                                    />
                                </Grid2>
                            </Grid2>
                        ))}
                    </Grid2>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCreateDialogClose} color="primary">
                        Cancel
                    </Button>
                    <Button onClick={handleCreateSubmit} color="primary">
                        Create
                    </Button>
                </DialogActions>
            </Dialog>

            <Dialog open={deleteDialogOpen} onClose={handleDeleteCancel}>
                <DialogTitle>Confirm Deletion</DialogTitle>
                <DialogContent>
                    Are you sure you want to delete this tournament?
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleDeleteCancel} color="primary">
                        Cancel
                    </Button>
                    <Button onClick={handleDeleteConfirm} color="error">
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
