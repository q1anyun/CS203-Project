import React, { useState, useEffect } from 'react';
import { Box, Typography, Chip, Button, Divider, Grid } from '@mui/material';
import { styled } from '@mui/system';
import defaultbackgroundImage from '../../assets/playerbg.jpg';
import { fetchTournamentPic } from '../Hooks/fetchTournamentPic';
import LocationLink from '../Hooks/getLocationLink';

const DetailBox = styled(Box)({
  backgroundColor: '#fff',
  borderRadius: '8px',
  padding: '16px',
  marginBottom: '10px',
  boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
});

const STATUS_COLOR_MAP = {
  LIVE: 'success',
  UPCOMING: 'warning',
  EXPIRED: 'default',
};

const TournamentDetail = ({ title, value }) => (
  <Grid item xs={12} sm={3}>
    <DetailBox>
      <Typography variant="playerProfile2">
        <strong>{title}</strong>
      </Typography>
      <Typography variant="body2">{value}</Typography>
    </DetailBox>
  </Grid>
);

function TournamentDescription({ tournament, handleStart, handleViewRegisteredPlayers }) {
  const [tournamentImage, setTournamentImage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadTournamentImage = async () => {
      if (!tournament?.id) return;
      
      setIsLoading(true);
      try {
        const imageUrl = await fetchTournamentPic(tournament.id);
        setTournamentImage(imageUrl);
      } catch (err) {
        setError('Failed to load tournament image');
      } finally {
        setIsLoading(false);
      }
    };

    loadTournamentImage();
  }, [tournament?.id]);

  const formatDate = (date) => {
    try {
      return new Date(date).toLocaleDateString();
    } catch (err) {
      return 'Invalid date';
    }
  };

  const tournamentDetails = [
   // { title: 'Format', value: tournament.format },
    { title: 'Current Players', value: `${tournament.currentPlayers} / ${tournament.maxPlayers}` },
    { title: 'Start Date', value: formatDate(tournament.startDate) },
    { title: 'End Date', value: formatDate(tournament.endDate) },
    { title: 'Minimum Elo', value: tournament.minElo },
    { title: 'Maximum Elo', value: tournament.maxElo },
    { title: 'Time Control', value: tournament.timeControl?.name || 'N/A' },
    { title: 'Tournament Type', value: tournament.tournamentType?.typeName || 'N/A' }
  ];

  return (
    <Box sx={{ padding: 2 }}>
      {/* Tournament Image */}
      <Box sx={{ width: '100vw', height: '200px', position: 'relative' }}>
        {isLoading ? (
          <Box sx={{ 
            width: '100%', 
            height: '100%', 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center',
            bgcolor: 'grey.100' 
          }}>
            <Typography>Loading...</Typography>
          </Box>
        ) : error ? (
          <Box sx={{ 
            width: '100%', 
            height: '100%', 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center',
            bgcolor: 'error.light' 
          }}>
            <Typography color="error">{error}</Typography>
          </Box>
        ) : (
          <img
            alt="Tournament"
            src={tournamentImage || defaultbackgroundImage}
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              position: 'absolute',
              top: -30,
              left: -32,
            }}
          />
        )}
      </Box>

      {/* Tournament Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
        <Typography variant="header1">{tournament.name}</Typography>
        <Chip 
          label={tournament.status} 
          color={STATUS_COLOR_MAP[tournament.status]} 
        />
        <Button
          variant="contained"
          color="primary"
          onClick={handleStart}
          disabled={tournament.status !== 'UPCOMING'}
        >
          Start Tournament
        </Button>
      </Box>

      {/* Tournament Description */}
      <Typography 
        variant="playerProfile2" 
        sx={{ display: 'block', textAlign: 'left', ml: '20px', mb: 2 }}
      >
        {tournament.description}
      </Typography>

      {/* Location */}
      <Typography 
        variant="playerProfile2" 
        sx={{ display: 'block', textAlign: 'left', ml: '20px', mb: 2 }}
      >
        <LocationLink
          address={tournament.locationAddress}
          latitude={tournament.locationLatitude}
          longitude={tournament.locationLongitude}
        />
      </Typography>

      <Divider sx={{ margin: '20px 0' }} />

      {/* Tournament Details Grid */}
      <Grid container spacing={2}>
        {tournamentDetails.map((detail, index) => (
          <TournamentDetail
            key={index}
            title={detail.title}
            value={detail.value}
          />
        ))}
      </Grid>

      {/* View Players Button */}
      <Button 
        variant="contained" 
        onClick={handleViewRegisteredPlayers} 
        color="outlined"
        sx={{ mt: 2 }}
      >
        <Typography variant="body4">
          Click to view registered players
        </Typography>
      </Button>
    </Box>
  );
}

export default TournamentDescription;