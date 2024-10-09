import { useState, useEffect } from 'react';
import axios from 'axios';
import defaultProfilePic from '../../assets/default_user.png';

const baseURL = import.meta.env.VITE_PLAYER_SERVICE_URL;

const useProfilePic = () => {
  const [profilePic, setProfilePic] = useState(defaultProfilePic);

  useEffect(() => {
    const fetchProfilePic = async () => {
      const token = localStorage.getItem('token'); // Retrieve the token from local storage

      if (!token) {
        console.error('No token found');
        setProfilePic(defaultProfilePic); // Use default profile picture if no token
        return;
      }

      try {
        const response = await axios.get(`${baseURL}/profilePicture`, {
          headers: {
            Authorization: `Bearer ${token}`, // Include the token in the headers
          },
          responseType: 'blob', // Important for handling images
        });

        if (response.data) {
          const imageUrl = URL.createObjectURL(response.data);
          setProfilePic(imageUrl);
        } else {
          console.error('Profile picture not found, using default.');
          setProfilePic(defaultProfilePic); // Use default profile picture if no data
        }
      } catch (error) {
        console.error('Error fetching profile picture, using default:', error);
        setProfilePic(defaultProfilePic); // Use default profile picture on error
      }
    };

    fetchProfilePic();
  }, []); // Runs on component mount

  return profilePic;
};

export default useProfilePic;
