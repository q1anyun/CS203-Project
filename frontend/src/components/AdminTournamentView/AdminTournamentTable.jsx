import * as React from 'react';
import { styled } from '@mui/material/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import { Chip, IconButton } from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import styles from './AdminTournamentTable.module.css';
// import axios from 'axios';

const tournaments = [
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

export default function AdminTournamentTable() {

    // const [tournaments, setTournaments] = useState([]);
    // const [loading, setLoading] = useState(true);
    // const [error, setError] = useState(null);

    // useEffect(() => {
    //     // Replace with your Spring Boot API URL
    //     axios.get('http://localhost:8080/api/tournaments')
    //         .then(response => {
    //             setTournaments(response.data);
    //             setLoading(false);
    //         })
    //         .catch(error => {
    //             setError(error);
    //             setLoading(false);
    //         });
    // }, []);

    // if (loading) return <CircularProgress />;
    // if (error) return <Typography color="error">Error loading data</Typography>;

    return (
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
                    {tournaments.map((tournament) => (
                        <StyledTableRow key={tournament.tournamentId}>
                            <StyledTableCell>{tournament.tournamentId}</StyledTableCell>
                            <StyledTableCell>{tournament.tournamentName}</StyledTableCell>
                            <StyledTableCell>{tournament.startDate}</StyledTableCell>
                            <StyledTableCell>{tournament.endDate}</StyledTableCell>
                            <StyledTableCell>{tournament.timeControl}</StyledTableCell>
                            <StyledTableCell>{tournament.numberOfPlayers}</StyledTableCell>
                            <StyledTableCell>
                                <Chip label={tournament.status} variant="outlined" color={statusColorMap[tournament.status]} />
                            </StyledTableCell>
                            <StyledTableCell>
                                <IconButton aria-label="view" color="primary">
                                    <VisibilityIcon />
                                </IconButton>
                                <IconButton aria-label="edit" color="secondary">
                                    <EditIcon />
                                </IconButton>
                                <IconButton aria-label="delete" color="error">
                                    <DeleteIcon />
                                </IconButton>
                            </StyledTableCell>
                        </StyledTableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
}
