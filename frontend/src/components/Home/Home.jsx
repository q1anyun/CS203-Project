import React from 'react';
import styles from './Home.module.css';
import { Container, Grid2, Typography, Card, CardContent, CardActions, Button } from '@mui/material';
import image from '../../assets/welcome.jpg';
import { Link } from 'react-router-dom';

function Home() {
  return (
    <div className={styles.bgContainer}>
      <Container maxWidth="lg" sx={{ height: '500px', marginTop: 20, marginBottom: 10, display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: 2 }} className={styles.welcomeContainer}>
        <Grid2 container spacing={4}>
          <Grid2 size={6}>
            <Typography variant="h4" className={styles.greetings} sx={{ marginBottom: 5 }}>Welcome to CHESS MVP</Typography>
            <Typography variant="h6" className={styles.description}>Chess MVP is your go-to platform for knockout chess tournaments. Enjoy seamless tournament management, real-time updates, and dynamic leaderboards, all designed for players of every skill level. Dive into competitive play with ease and precision. </Typography>
            <h2>working in progress...</h2>
          </Grid2>
          <Grid2 size={6}>
            <img src={image} alt="Description" style={{ width: '100%', height: 'auto' }} />
          </Grid2>
        </Grid2>
      </Container>

      <Container maxWidth="lg">
        <Grid2 container spacing={3}>
          <Grid2 size={4}>
            <Card sx={{ maxWidth: 345, height: 200 }}>
              <CardContent>
                <Typography variant="h5">Tournaments</Typography>
                <Typography variant="body2">Register for upcoming knockout chess tournaments and compete against players of all levels.</Typography>
              </CardContent>
              <CardActions className={styles.cardActions}>
                <Button component={Link} to="/player/tournaments" variant="outlined" color="secondary" size="medium">To Tournaments</Button>
              </CardActions>
            </Card>
          </Grid2>

          <Grid2 size={4}>
            <Card sx={{ maxWidth: 345, height: 200 }}>
              <CardContent>
                <Typography variant="h5">Leaderboard</Typography>
                <Typography variant="body2">Check out the latest rankings and see where you stand among your peers.</Typography>
              </CardContent>
              <CardActions className={styles.cardActions}>
                <Button component={Link} to="/leaderboard" variant="outlined" color="secondary" size="medium">To Leaderboard</Button>
              </CardActions>
            </Card>
          </Grid2>

          <Grid2 size={4}>
            <Card sx={{ maxWidth: 345, height: 200 }}>
              <CardContent>
                <Typography variant="h5">Profile</Typography>
                <Typography variant="body2">View and manage your personal chess profile, including detailed statistics, past tournament results, and performance trends.</Typography>
              </CardContent>
              <CardActions className={styles.cardActions}>
                <Button component={Link} to="/player/profile" variant="outlined" color="secondary" size="medium">To Profile</Button>
              </CardActions>
            </Card>
          </Grid2>
        </Grid2>
      </Container>
    </div>
  );
}

export default Home;
