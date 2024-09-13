import React, { useState } from 'react';
import './PlayerProfile.css';
import defaultPlayerIcon from '../../assets/playericon.jpg';


function PlayerProfile() {
  const [profileImage, setProfileImage] = useState(defaultPlayerIcon);

  const handleImageUpload = (event) => {
    const file = event.target.files[0];
    if (file) {
      const imageUrl = URL.createObjectURL(file);  // Create a URL for the uploaded image
      setProfileImage(imageUrl);  // Update the profile image with the uploaded image
    }
  };

  return (
    <div className="playerprofile">
      <div className="bg">
        <div className="profile-down">
          <img src={profileImage} alt="Player Profile"
            style={{ width: '200px', height: '200px', borderRadius: '100px' }} />
        </div>
        <div className="profile-button">
          <label htmlFor="upload-photo" className="upload-label">Upload photo</label>
          <input 
            type="file" 
            id="upload-photo" 
            accept="image/*"  // Accept only image files
            onChange={handleImageUpload}  // Handle the image upload
            style={{ display: 'none' }} 
          />
        </div>
        <div className="profile-title">
          <h3>Magnus Carlsen</h3>
        </div>
        <div className="profile-Description">
          Wins: 8 <br />
          Loss: 8 <br />
          Winrate = 50%
        </div>
      </div>
    </div>
  );
}

export default PlayerProfile;