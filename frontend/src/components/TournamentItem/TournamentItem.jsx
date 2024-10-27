import React from 'react';
import { Grid, Card, CardContent, Typography, Divider, Box } from '@mui/material';

const TournamentItem = ({ tournament }) => {
    return (
      
            <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                <img
                    src={`https://via.placeholder.com/300x200?text=${tournament.name}`} // get the tournament upload photo (to be updated at another date)
                    alt={tournament.name}
                    style={{ width: '100%', height: '200px', objectFit: 'cover' }}
                />
                <CardContent sx={{ flexGrow: 1 }}>
                    <Typography variant="header2">{tournament.name}</Typography> {/* Header for the tournament name */}
                    <Divider sx={{ margin: '8px 0' }} /> {/* Divider after the title */}

                    <Typography variant="header3">Start: </Typography>
                    <Typography variant="playerProfile2">
                        {new Date(tournament.startDate + "Z").toLocaleString()}
                    </Typography> {/* Body for the Start date */}

                    <Box display="flex" flexDirection="row" alignItems="center">
                        <Typography variant="header3" display="block" sx={{ marginRight: '8px' }}>End:</Typography>
                        <Typography variant="playerProfile2">
                            {new Date(tournament.endDate + "Z").toLocaleString()}
                        </Typography> {/* Body for the End date */}
                    </Box>

                    <Box display="flex" flexDirection="row" alignItems="center">
                        <Typography variant="header3" display="block" sx={{ marginRight: '8px' }}>Min ELO:</Typography>
                        <Typography variant="playerProfile2">
                            {tournament.minElo}
                        </Typography>
                    </Box>
                    <Box display="flex" flexDirection="row" alignItems="center">
                        <Typography variant="header3" display="block" sx={{ marginRight: '8px' }}>Max ELO:</Typography>

                        <Typography variant="playerProfile2">
                            {tournament.maxElo}
                        </Typography>
                    </Box>
                    <Box display="flex" flexDirection="row" alignItems="center">
                        <Typography variant="header3" display="block" sx={{ marginRight: '8px' }}>Time Control:</Typography>

                        <Typography variant="playerProfile2">
                            {tournament.timeControl.timeControlMinutes} minutes
                        </Typography>
                    </Box>


                </CardContent>
            </Box>
       
    );
};

export default TournamentItem;

