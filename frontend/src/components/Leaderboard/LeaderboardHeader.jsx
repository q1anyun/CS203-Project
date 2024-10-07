import React from 'react';
import image from '../../assets/leaderboard_bg.jpg';
import styles from './LeaderboardHeader.module.css';
import {Typography} from '@mui/material';


function LeaderboardHeader() {
    return (
        <div className={styles.container}>
            <img src={image} alt="Leaderboard Background" />
            <div className={styles.content}>
                <Typography variant = "h3" sx={{fontFamily: "Chewy, system-ui"}}>LEADERBOARD</Typography>
                <Typography variant="h6" sx = {{fontFamily: "League Spartan, sans-serif", fontStyle: "italic"}}>"When you see a good move, look for a better one."</Typography>
            </div>
        </div>
    );
}

export default LeaderboardHeader;
