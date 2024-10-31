import React from 'react';
import image from '../../assets/leaderboard_bg.jpg';
import styles from './LeaderboardHeader.module.css';
import {Typography} from '@mui/material';


function LeaderboardHeader() {
    return (
        <div className={styles.container}>
            <img src={image} alt="Leaderboard Background" />
            <div className={styles.content}>
                <Typography variant = "homePage" textAlign={'center'} >Leaderboard</Typography>
                <Typography variant="homePage2" display='block' textAlign={'left'}>"When you see a good move, look for a better one."</Typography>
            </div>
        </div>
    );
}

export default LeaderboardHeader;
