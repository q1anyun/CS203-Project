import { React, useState, useEffect } from 'react';
import styles from './Home.module.css';
import { Grid2, Button } from '@mui/material';
import image from '../../assets/welcomeBg.png';
import tournamentImage from '../../assets/tournament-icon.png';
import leaderboardImage from '../../assets/leaderboard-icon.png';
import profileImage from '../../assets/profile-icon.png';
import { Link } from 'react-router-dom';

function Home() {
  const [userRole, setUserRole] = useState(null);

  useEffect(() => {
    const role = localStorage.getItem('role');
    setUserRole(role);
  }, []);

  const getTournamentLink = () => {
    if (userRole === 'ADMIN') {
      return '/admin/tournaments';
    }
    return '/player/tournaments';
  };

  const getProfileLink = () => {
    if (userRole === 'ADMIN') {
      return '/admin/profile';
    }
    return '/player/profile';
  };

  return (
    <div className={styles.mainContainer}>
      <div className={styles.welcomeContainer}>
        <Grid2 container spacing={3}>
          <Grid2 size={4} className={styles.imageContainer}>
            <img src={image} alt="welcomeImage" className={styles.Image} />
          </Grid2>
          <Grid2 size={8} className={styles.textContainer}>
            <h1 className={styles.greetings}>Welcome to Chess MVP</h1>
            <h6 className={styles.description}>YOUR PLATFORM FOR KNOCKOUT CHESS TOURNAMENTS</h6>
          </Grid2>

        </Grid2>
      </div>

      <div className={styles.cardContainer}>
        <Grid2 container spacing={8}>
          <Grid2 size={4}>
            <div className={styles.card}>
              <h1 className={styles.cardTitle}>Tournaments</h1>
              <h5 className={styles.cardContent}>Register for upcoming knockout chess tournaments and compete against players.</h5>
              <img src={tournamentImage} alt="Tournaments" className={styles.cardImage} />
              <Button component={Link} to={getTournamentLink()} variant="contained" color="secondary" size="medium" sx={{ fontFamily: "Chewy, system-ui", fontSize: "20px" }}>View Tournaments</Button>
            </div>
          </Grid2>

          <Grid2 size={4}>
            <div className={styles.card}>
              <h1 className={styles.cardTitle}>Leaderboard</h1>
              <h5 className={styles.cardContent}>Check out the latest rankings and see where you stand among your peers.</h5>
              <img src={leaderboardImage} alt="leaderboard" className={styles.cardImage} />
              <Button component={Link} to="/leaderboard" variant="contained" color="secondary" size="medium" sx={{ fontFamily: "Chewy, system-ui", fontSize: "20px" }}>View Leaderboard</Button>
            </div>
          </Grid2>

          <Grid2 size={4}>
            <div className={styles.card}>
              <h1 className={styles.cardTitle}>Profile</h1>
              <h5 className={styles.cardContent}>View and manage your personal chess profile, check your statistics.</h5>
              <img src={profileImage} alt="leaderboard" className={styles.cardImage} />
              <Button component={Link} to={getProfileLink()} variant="contained" color="secondary" size="medium" sx={{ fontFamily: "Chewy, system-ui", fontSize: "20px" }}>View Profile</Button>
            </div>
          </Grid2>
        </Grid2>
      </div>
    </div>
  );
}

export default Home;
