import { useState, useEffect } from 'react';
import axios from 'axios';
import defaultProfilePic from '../../assets/default_user.png';


const tournamentPlayerURL = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;
const playerURL = import.meta.env.VITE_PLAYER_SERVICE_URL

const useTournamentParticipants = (tournamentId) => {
  const [participants, setParticipants] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const createData = (id, firstName, lastName, country, eloRating) => ({
    id, firstName, lastName, country, eloRating
  });

  const attachProfilePhotos = async (players) => {
    const token = localStorage.getItem('token');
    return await Promise.all(
      players.map(async (player) => {
        try {
          const profilePictureResponse = await axios.get(
            `${playerURL}/photo/${player.id}`, 
            {
              headers: { Authorization: `Bearer ${token}` },
              responseType: 'blob',
            }
          );
          const imageUrl = URL.createObjectURL(profilePictureResponse.data);
          return { ...player, profilePhoto: imageUrl };
        } catch (error){
          setError(error); 
          return { ...player, profilePhoto: defaultProfilePic };
        }
      })
    );
  };

  const fetchParticipants = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${tournamentPlayerURL}/${tournamentId}`);
      const data = response.data;
      const formattedData = data.map((participant) => 
        createData(
          participant.id, 
          participant.firstName, 
          participant.lastName, 
          participant.country, 
          participant.eloRating
        )
      );
      const participantsWithPhotos = await attachProfilePhotos(formattedData);
      setParticipants(participantsWithPhotos);
      setError(null);
    } catch (error) {
      setError(error);
      setParticipants([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchParticipants();
  }, [tournamentId]);

  return { participants, loading, refetch: fetchParticipants };
};

export default useTournamentParticipants;