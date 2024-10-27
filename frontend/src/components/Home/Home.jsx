import { React, useState, useEffect } from 'react';
import styles from './Home.module.css';
import { Grid2, Button, Typography, Box } from '@mui/material';
import image from '../../assets/welcomeBg.png';
import tournamentImage from '../../assets/tournament-icon.png';
import leaderboardImage from '../../assets/leaderboard-icon.png';
import profileImage from '../../assets/profile-icon.png';
import { Link } from 'react-router-dom';
import { styled } from '@mui/system';

const CustomButton = styled(Button)({
  backgroundColor: '#000000',
  color: 'white',
  '&:hover': {
      backgroundColor: '#808080'
  },
  padding: '10px 20px',
  borderRadius: '4px',
  boxShadow: '0 3px 5px rgba(0, 0, 0, 0.1)'
});

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

    <Box  >
      <Box className={styles.welcomeContainer}>
        {/* Image or other components can be added here */}
        <Box  >
          <Typography variant="homePage" className={styles.greetings} display="block">
            Welcome to Chess MVP
          </Typography>

          <Typography variant="homePage2" className={styles.description} align="center" display="block">
            Your platform for Chess Knockout Tournaments
          </Typography>
        </Box>
      </Box>


      <div className={styles.cardContainer} style={{ marginTop: '-130px' }}>
        <Grid2 container spacing={8}>
          <Grid2 size={4}>
            <div className={styles.card}>
              <Typography variant="homePage3" className={styles.cardTitle} display="block">Leaderboard</Typography>
              <Typography variant="homePage2" className={styles.cardContent} display="block">Check out the latest rankings and see where you stand among your peers.</Typography>
              <img src={leaderboardImage} alt="leaderboard" className={styles.cardImage} />
              <CustomButton component={Link} to="/leaderboard" variant="contained" color="secondary" size="medium"  sx={{ fontFamily: "Chewy, system-ui", fontSize: "20px" }}>View Leaderboard</CustomButton>
            </div>
          </Grid2>

          <Grid2 size={4}>
            <div className={styles.card}>
            <Typography variant="homePage3" className={styles.cardTitle} display="block">Tournaments</Typography>
            <Typography variant="homePage2" className={styles.cardContent} display="block">Register for upcoming knockout chess tournaments and compete against players.</Typography>

              <img src={tournamentImage} alt="Tournaments" className={styles.cardImage} />
              <CustomButton component={Link} to={getTournamentLink()} variant="contained" color="secondary" size="medium" sx={{ fontFamily: "Chewy, system-ui", fontSize: "20px" }}>View Tournaments</CustomButton>
            </div>
          </Grid2>

          <Grid2 size={4}>
            <div className={styles.card}>
            <Typography variant="homePage3" className={styles.cardTitle} display="block">Profile</Typography>
            <Typography variant="homePage2" className={styles.cardContent} display="block">View and manage your personal chess profile, check your statistics.</Typography>
              <img src={profileImage} alt="leaderboard" className={styles.cardImage} />
              <CustomButton component={Link} to={getProfileLink()} variant="contained" color="secondary" size="medium" sx={{ fontFamily: "Chewy, system-ui", fontSize: "20px" }}>View Profile</CustomButton>
            </div>
          </Grid2>
        </Grid2>
      </div>
    </Box>
  );
}

export default Home;