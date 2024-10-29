import React from 'react';
import { Box, Typography, Grid } from '@mui/material';

const SwissBracket = ({ rounds }) => {
    return (
        <Box sx={{ padding: 2 }}>
            {rounds.map((round, roundIndex) => (
                <Box key={roundIndex} sx={{ marginBottom: 2 }}>
                    <Typography variant="h6" align="center">
                        Round {roundIndex + 1}
                    </Typography>
                    <Grid container spacing={2} justifyContent="center">
                        {round.seeds.map((match, matchIndex) => (
                            <Grid item xs={3} key={matchIndex}>
                                <Box
                                    sx={{
                                        border: '1px solid #ccc',
                                        borderRadius: '4px',
                                        padding: 1,
                                        backgroundColor: '#fff',
                                        textAlign: 'center',
                                    }}
                                >
                                    <Typography variant="body1">{match.teams[0]?.name || 'Pending'}</Typography>
                                    <Typography variant="body2">{match.winnerId === match.teams[0]?.id ? match.winnerId : ''}</Typography>
                                    <Typography variant="body1">{match.teams[1]?.name || 'Pending'}</Typography>
                                    <Typography variant="body2">{match.winnerId === match.teams[1]?.id ? match.winnerId : ''}</Typography>
                                </Box>
                            </Grid>
                        ))}
                    </Grid>
                </Box>
            ))}
        </Box>
    );
};

export default SwissBracket;
