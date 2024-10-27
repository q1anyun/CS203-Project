import React, { useState, useEffect } from 'react';
import { Grid, Card, CardContent, Typography, Divider, Box, Chip } from '@mui/material';
import axios from 'axios'; 
import defaultbackgroundImage from '../../assets/welcome.jpg';

const statusColorMap = {
    LIVE: 'success',
    UPCOMING: 'warning',
    COMPLETED: 'default',
};


const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;

const TournamentItem = ({ tournament }) => {
    const [localTournamentPic, setLocalTournamentPic] = useState(defaultbackgroundImage)
      // useTournamentPic hook should return the URL of the image

      useEffect(() => {
        const fetchTournamentPic = async () => {
         try {

            
            const response = await axios.get(`${baseURL}/getTournamentImage/${tournament.id}`, {
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
                responseType: 'blob', // Important for handling images
            });
            
            console.log(response.data); 

            if (response.data) {
                const imageUrl = URL.createObjectURL(response.data);
                setLocalTournamentPic(imageUrl);
            }
            
         } catch (error) {
                    console.error('Profile picture not found, using default.');
                    setLocalTournamentPic(defaultbackgroundImage); // Use default profile picture if no data
                  }
               
          
        };

        fetchTournamentPic(); 
    }, []);
   
    return (
      
            <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                <img
                    src={localTournamentPic} // get the tournament upload photo (to be updated at another date)
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

