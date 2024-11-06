import React from 'react';
import styles from './LeaderboardHeader.module.css';
import { Typography, Stack, Box, } from '@mui/material';
import { Link } from 'react-router-dom';


function LeaderboardHeader({ topPlayers }) {

    return (
        <div className={styles.container}>

            <div className={styles.content}>
                <Typography variant="header1" textAlign={'center'} >Leaderboard</Typography>

            </div>


            <Stack
                direction="row"
                sx={{
                    justifyContent: 'space-around',
                    alignItems: 'flex-end',
                    marginBottom: '2rem'
                }}
            >
                {/* Second Place */}
                <Link to={`/profileview/${topPlayers[1]?.playerId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                    <Box sx={{ textAlign: 'center' }}>
                        <Box sx={{
                            backgroundColor: '#E5E7EB', // gray-200
                            borderRadius: '50%',
                            width: '5rem', // w-20
                            height: '5rem', // h-20
                            margin: '0 auto',
                            marginBottom: '0.5rem',
                            overflow: 'hidden'
                        }}>

                        </Box>
                        <Typography variant="header3" component='h2' sx={{ textAlign: 'center' }}>2</Typography>
                        <Typography variant="header3" sx={{ color: 'black' }}>
                            {topPlayers[1]?.firstName} {topPlayers[1]?.lastName}
                        </Typography>
                        <Typography sx={{ color: '#A9A9A9' }}> {/* gray-300 */}
                            {topPlayers[1]?.eloRating}
                        </Typography>
                    </Box>
                </Link>

                {/* First Place */}
                <Link to={`/profileview/${topPlayers[0]?.playerId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                    <Box sx={{
                        textAlign: 'center',
                        marginBottom: '-1rem'
                    }}>
                        <Box sx={{
                            backgroundColor: '#FCD34D', // yellow-400
                            borderRadius: '50%',
                            width: '6rem', // w-24
                            height: '6rem', // h-24
                            margin: '0 auto',
                            marginBottom: '0.5rem',
                            overflow: 'hidden'
                        }}>
                        </Box>
                        <Typography variant="header2" component='h3' sx={{ textAlign: 'center' }}>1</Typography>
                        <Typography variant="header2" sx={{ color: 'black' }}>
                            {topPlayers[0]?.firstName} {topPlayers[0]?.lastName}
                        </Typography>
                        <Typography variant='header3' component='h2' sx={{ color: '#A9A9A9', textAlign: 'center' }}> {/* gray-300 */}
                            {topPlayers[0]?.eloRating}
                        </Typography>

                    </Box>
                </Link>

                {/* Third Place */}
                <Link to={`/profileview/${topPlayers[2]?.playerId}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                    <Box sx={{ textAlign: 'center' }}>
                        <Box sx={{
                            backgroundColor: '#FDB068', // orange-300
                            borderRadius: '50%',
                            width: '5rem', // w-20
                            height: '5rem', // h-20
                            margin: '0 auto',
                            marginBottom: '0.5rem',
                            overflow: 'hidden'
                        }}>

                        </Box>
                        <Typography variant="header3" component='h2' sx={{ textAlign: 'center' }}>3</Typography>
                        <Typography variant='header3' sx={{ color: 'black' }}>
                            {topPlayers[2]?.firstName} {topPlayers[2]?.lastName}
                        </Typography>
                        <Typography sx={{ color: '#A9A9A9' }}> {/* gray-300 */}
                            {topPlayers[2]?.eloRating}
                        </Typography>
                    </Box>
                </Link>
            </Stack>
        </div>
    );
}

export default LeaderboardHeader;
