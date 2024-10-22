import React, {useState} from 'react';
import { Box, Typography, Chip, Button, Divider, Grid} from '@mui/material';
import { styled } from '@mui/system';
import defaultbackgroundImage from '../../assets/playerbg.jpg';

const DetailBox = styled(Box)({
    backgroundColor: '#fff', // White background for each detail box
    borderRadius: '8px', // Rounded corners
    padding: '16px', // Padding inside the box
    marginBottom: '10px', // Space between boxes
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)', 
 
});

const statusColorMap = {
    LIVE: 'success',
    UPCOMING: 'warning',
    EXPIRED: 'default',
};

function TournamentDescription({ tournament, handleStart }) {
    const[backgroundImage, setBackgroundImage] = useState(defaultbackgroundImage); 
    return (
        
        
        <Box sx={{ padding: 2 }}>
             <Box 
                sx={{
                    width: '100vw', // Full width of the parent
                    height: '200px', // Fixed height
                    position: 'relative', // Position relative for absolute child positioning if needed
                }}
            >
                <img
                    alt="Tournament"
                    src={backgroundImage}
                    style={{ 
                        width: '100%', // Full width of the container
                        height: '100%', // Full height of the container
                        objectFit: 'cover', // Cover the entire area
                        position: 'absolute', // Positioning to cover the box
                        top: -30,
                        left: -32, 
                    }}
                />
            </Box>
            


            <Typography variant="header1" >{tournament.name}</Typography>
            <Chip label={tournament.status} color={statusColorMap[tournament.status]} />
            <Button
                variant="contained"
                color="primary"
                onClick={handleStart}
                disabled={tournament.status !== 'UPCOMING'}
                sx={{ marginLeft: '10px' }}
            >
                Start Tournament
            </Button>
            <Typography variant="playerProfile2" display={'block'} textAlign={'left'} marginLeft={'20px'}>{tournament.description}</Typography>


            <Divider sx={{ margin: '20px 0' }} />
     

           
            {/* Tournament Details */}

            <Grid container spacing={2}>
                <Grid item xs={12} sm={4}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Format</strong></Typography>
                        <Typography variant="body2">{tournament.format}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={4}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Current Players</strong></Typography>
                        <Typography variant="body2">{tournament.currentPlayers} / {tournament.maxPlayers}</Typography>
                    </DetailBox>
                </Grid>
                
                <Grid item xs={12} sm={4}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Start Date</strong></Typography>
                        <Typography variant="body2">{new Date(tournament.startDate).toLocaleDateString()}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={4}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>End Date</strong></Typography>
                        <Typography variant="body2">{new Date(tournament.endDate).toLocaleDateString()}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={4}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Minimum Elo</strong></Typography>
                        <Typography variant="body2">{tournament.minElo}</Typography>
                    </DetailBox>
                </Grid>
                <Grid item xs={12} sm={4}>
                    <DetailBox>
                        <Typography variant="playerProfile2"><strong>Maximum Elo</strong></Typography>
                        <Typography variant="body2">{tournament.maxElo}</Typography>
                    </DetailBox>
                </Grid>
            </Grid>

        </Box>
    );
}

export default TournamentDescription;
