import axios from 'axios';
import defaultbackgroundImage from '../../assets/welcome.jpg'; 

const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;

// Create a cache for tournament images
const imageCache = {};

// Fetch Tournament Image Function
export const fetchTournamentPic = async (tournamentId) => {
    if (imageCache[tournamentId]) {
        return imageCache[tournamentId];
    }

    try {
        const response = await axios.get(`${baseURL}/photo/${tournamentId}`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            responseType: 'blob',
        });
        const imageUrl = URL.createObjectURL(response.data);
        imageCache[tournamentId] = imageUrl; // Cache the image URL
        return imageUrl;
    } catch (error) {
        console.error(`Image not found for tournament ${tournamentId}, using default.`);
        return defaultbackgroundImage; // Return default if error occurs
    }
};
