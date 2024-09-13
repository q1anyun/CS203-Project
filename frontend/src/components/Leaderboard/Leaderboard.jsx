import React from 'react';
import Profile from './Profile';
import LeaderboardHeader from './LeaderboardHeader';
import sampleData from './data'; 
import axios from 'axios';
import { Container, Grid2 } from '@mui/material';


function Leaderboard() {
  // const [profiles, setProfiles] = useState([]); 
  // const [loading, setLoading] = useState(true); 
  // const [error, setError] = useState(null); 

  // useEffect(() => {
  //   const fetchData = async () => {
  //     try {
  //       const response = await axios.get('/api/leaderboard'); 
  //       setProfiles(response.data); 
  //       setLoading(false); 
  //     } catch (err) {
  //       setError('Failed to load data'); 
  //       setLoading(false);
  //     }
  //   };

  //   fetchData(); 
  // }, []); 

  // if (loading) return <p>Loading...</p>; 
  // if (error) return <p>{error}</p>; 

  return (
    <div>
      <LeaderboardHeader/>
      <Container maxWidth="lg" sx={{ marginTop: 4 , marginBottom: 10}}>
        <Grid2 container spacing={2}>
          {sampleData.map((profile) => (
            <Grid2 size={12} key={profile.id}>
              <Profile 
                key={profile.userId}
                rank={profile.rank}
                firstName={profile.firstName}
                lastName={profile.lastName}
                username={profile.username}
                eloPoints={profile.eloPoints}
                profilePhoto={profile.photo}
              />
            </Grid2>
          ))}
        </Grid2>
          {/*To create specific leaderboard position for player*/ }
      </Container>
    </div>
  );
}

export default Leaderboard;
