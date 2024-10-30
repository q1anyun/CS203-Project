// fetchProfilePic.js
import axios from 'axios';
import defaultProfilePic from '../../assets/default_user.png'; // Adjust path as needed

const baseURL = import.meta.env.VITE_PLAYER_SERVICE_URL;

// Create a cache for profile picture
const profilePicCache = {};

// Fetch Profile Picture Function
export const fetchProfilePic = async () => {
    if (profilePicCache['profile']) {
        return profilePicCache['profile'];
    }

    try {
        const response = await axios.get(`${baseURL}/photo`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            responseType: 'blob',
        });

        const imageUrl = URL.createObjectURL(response.data);
        profilePicCache['profile'] = imageUrl; // Cache the image URL
        return imageUrl;
    } catch (error) {
        console.error(`Error fetching profile picture, using default:`, error);
        return defaultProfilePic; // Return default if error occurs
    }
};
