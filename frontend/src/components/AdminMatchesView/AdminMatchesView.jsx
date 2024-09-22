import * as React from 'react'; 
import { useState } from 'react';
import { styled } from '@mui/material/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import { Typography, Chip } from '@mui/material';
import styles from './AdminMatchesView.module.css';

const matchesData = [
    {
        matchId: 5001,
        player1Id: 101,
        player2Id: 102,
        tournamentId: 100823,
        winnerId: 101,
        roundId: 1,
    },
    {
        matchId: 5002,
        player1Id: 103,
        player2Id: 104,
        tournamentId: 200564,
        winnerId: 104,
        roundId: 1,
    },
    {
        matchId: 5003,
        player1Id: 105,
        player2Id: 106,
        tournamentId: 200789,
        winnerId: null,  // Match is ongoing or no winner yet
        roundId: 2,
    },
    {
        matchId: 5004,
        player1Id: 107,
        player2Id: 108,
        tournamentId: 100823,
        winnerId: 108,
        roundId: 2,
    },
    {
        matchId: 5005,
        player1Id: 109,
        player2Id: 110,
        tournamentId: 200564,
        winnerId: null,  // No result yet
        roundId: 3,
    },
];

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

function AdminMatchView() {
    const [matches] = useState(matchesData);

    return (
        <div>
            <Typography variant="h4" component="h2" gutterBottom className={styles.title}>
                All Matches
            </Typography>
            <TableContainer component={Paper} className={styles.tableContainer}>
                <Table sx={{ minWidth: 700 }} aria-label="customized table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell>Match ID</StyledTableCell>
                            <StyledTableCell>Player 1 ID</StyledTableCell>
                            <StyledTableCell>Player 2 ID</StyledTableCell>
                            <StyledTableCell>Tournament ID</StyledTableCell>
                            <StyledTableCell>Winner ID</StyledTableCell>
                            <StyledTableCell>Round ID</StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {matches.map((match) => (
                            <StyledTableRow key={match.matchId}>
                                <StyledTableCell>{match.matchId}</StyledTableCell>
                                <StyledTableCell>{match.player1Id}</StyledTableCell>
                                <StyledTableCell>{match.player2Id}</StyledTableCell>
                                <StyledTableCell>{match.tournamentId}</StyledTableCell>
                                <StyledTableCell>
                                    {match.winnerId ? (
                                        match.winnerId
                                    ) : (
                                        <Chip label="Ongoing" color="warning" size="small" />
                                    )}
                                </StyledTableCell>
                                <StyledTableCell>{match.roundId}</StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </div>
    );
}

export default AdminMatchView;
