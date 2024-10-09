import { useState, useEffect } from 'react';
import axios from 'axios';

const baseURL = import.meta.env.VITE_PLAYER_SERVICE_URL;

const useProfilePic = () => {
  const [profilePic, setProfilePic] = useState(null);

  useEffect(() => {
    const fetchProfilePic = async () => {
      const token = localStorage.getItem('token'); // Retrieve the token from local storage

      if (!token) {
        console.error('No token found');
        return; // Exit if no token is available
      }

      try {
        const response = await axios.get(`${baseURL}/profilePicture`, {
          headers: {
            Authorization: `Bearer ${token}`, // Include the token in the headers
          },
          responseType: 'blob', // Important for handling images
        });

        const imageUrl = URL.createObjectURL(response.data);
        setProfilePic(imageUrl);
      } catch (error) {
        console.error('Error fetching profile picture:', error);
      }
    };

    fetchProfilePic();
  }, []); // No dependencies needed as token is fetched inside the effect

  return profilePic;
};

export default useProfilePic;

