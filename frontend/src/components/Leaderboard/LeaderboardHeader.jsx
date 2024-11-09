import React from 'react';
import styles from './LeaderboardHeader.module.css';
import { Typography, Stack, Box, Avatar, Divider } from '@mui/material';
import { Link } from 'react-router-dom';
import trophy from '../../assets/trophy.png';


function LeaderboardHeader({ topPlayers }) {

    return (
        <div className={styles.container}>

            <div className={styles.content}>
                <img src={trophy} alt="trophy" style={{ width: '60px', height: 'auto', marginTop: '-100px', marginLeft: '250px' }} />
                <img src={trophy} alt="trophy" style={{ width: '60px', height: 'auto', marginTop: '-100px', marginLeft: '-250px' }} />
                <span><Typography variant="homePage" >Leaderboard</Typography></span>

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
                        <Avatar
                            src={topPlayers[1]?.profilePhoto}
                            alt={`${topPlayers[1]?.firstName}'s profile`}
                            sx={{
                                width: '5rem', // w-20
                                height: '5rem', // h-20
                                backgroundColor: '#E5E7EB', // gray-200
                                margin: '0 auto 0.5rem',
                            }}
                        />
                        <Typography variant="header3" component='h2' sx={{ textAlign: 'center' }}>2</Typography>
                        <Typography variant="header3" sx={{ color: 'black' }}>
                            {topPlayers[1]?.firstName} {topPlayers[1]?.lastName}
                        </Typography>
                        <Typography sx={{ color: '#A9A9A9' }}> 
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

                        <Avatar
                            src={topPlayers[0]?.profilePhoto}
                            alt={`${topPlayers[0]?.firstName}'s profile`}
                            sx={{
                                width: '6rem',
                                height: '6rem',
                                backgroundColor: '#FCD34D', 
                                margin: '0 auto 0.5rem',
                            }}
                        />
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

                        <Avatar
                            src={topPlayers[2]?.profilePhoto}
                            alt={`${topPlayers[2]?.firstName}'s profile`}
                            sx={{
                                width: '5rem',
                                height: '5rem',
                                backgroundColor: '#FDB068', 
                                margin: '0 auto 0.5rem',
                            }}
                        />

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
