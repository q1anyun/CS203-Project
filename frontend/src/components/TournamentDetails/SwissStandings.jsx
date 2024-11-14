import React from 'react';
import { Table, TableBody, TableCell, TableContainer, tableCellClasses, TableHead, TableRow, Paper, Typography } from '@mui/material';
import { styled } from '@mui/material/styles';


const StyledTableCell = styled(TableCell)(({ theme }) => ({
    [`&.${tableCellClasses.head}`]: {
        backgroundColor: theme.palette.common.black,
        color: theme.palette.common.white,
    },
    [`&.${tableCellClasses.body}`]: {

        fontSize: 14,
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

const SwissStandings = ({ swissStandings, playersWithPhotos }) => {
  return (
    <TableContainer component={Paper} sx={{ padding: '20px' }}>
      <Table sx={{ minWidth: 700 }} aria-label="Swiss Standings table">
        <TableHead>
          <StyledTableRow>
          <StyledTableCell>Rank</StyledTableCell>
            <StyledTableCell >Name</StyledTableCell>
            <StyledTableCell align="center">Matches Won</StyledTableCell>
            <StyledTableCell align="center">Matches Lost</StyledTableCell>
            <StyledTableCell align="center">Win Rate</StyledTableCell>
            <StyledTableCell align="center">Elo Rating</StyledTableCell>
          </StyledTableRow>
        </TableHead>
        <TableBody>
          {swissStandings.map((standing, index) => (
            <StyledTableRow key={standing.player?.id} hover>
                <StyledTableCell>{index +1}</StyledTableCell>
              <StyledTableCell component="th" scope="row">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <img
                    src={playersWithPhotos[standing.player?.id]}
                    alt={`${standing.player?.firstName} ${standing.player?.lastName}`}
                    style={{ width: '32px', height: '32px', borderRadius: '50%' }}
                  />
                  {`${standing.player?.firstName} ${standing.player?.lastName}`}
                </div>
              </StyledTableCell>
              <StyledTableCell align="center">{standing.wins}</StyledTableCell>
              <StyledTableCell align="center">{standing.losses}</StyledTableCell>
              <StyledTableCell align="center">
                {standing.losses > 0 ? ((standing.wins / (standing.losses + standing.wins)) * 100).toFixed(1) : '100'}%
              </StyledTableCell>
              <StyledTableCell align="center">{standing.player?.eloRating}</StyledTableCell>
            </StyledTableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default SwissStandings;
