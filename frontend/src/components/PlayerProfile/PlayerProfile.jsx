import React, { useState } from 'react';
import './PlayerProfile.css';
import IconButton from '@mui/material/IconButton';
import EditIcon from '@mui/icons-material/Edit';



function PlayerProfile({ profilePic, onProfilePicUpdate }) {
  const [localProfilePic, setLocalProfilePic] = useState(profilePic);
  const [playerName, setPlayerName] = useState('Magnus Carlsen');
  const [isEditing, setIsEditing] = useState(false); 
  const wins = 8;
  const losses = 8;
  const totalGames = wins + losses;
  const winrate = (wins / totalGames) * 100;



  const handleImageUpload = (event) => {
    const file = event.target.files[0];
    if (file) {
      const imageUrl = URL.createObjectURL(file);
      setLocalProfilePic(imageUrl);
      onProfilePicUpdate(imageUrl);  // Pass updated image to the parent (AppContent)
    }
  };
  const handleNameChange = (event) => {
    setPlayerName(event.target.value);
  };
  const handleEditClick = () => {
    setIsEditing(!isEditing); // Toggle editing mode
  };

  return (
    <div className="playerprofile">
      <div className="bg">
        <div className="profile-down">
          <img src = {localProfilePic}  alt="Player Profile"
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
        <div className="profile-section">
          <div className="profile-name">
          <h1>{playerName}</h1> {/* Display dynamic player name */}
          <IconButton aria-label="edit" color="secondary" onClick={handleEditClick}>
                <EditIcon />
              </IconButton>
              {isEditing && (
            <input
              type="text"
              value={playerName}
              onChange={handleNameChange}
              className="name-input"
              placeholder="Enter player name"
            />
          )}
            <div className="stats">
            <div className="stat-item">
                <span className="stat-label">Wins</span>
                <span className="stat-value">{wins}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Losses</span>
                <span className="stat-value">{losses}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Winrate</span>
                <span className="stat-value">{winrate.toFixed(2)}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
     

    </div>
    
  );
}

export default PlayerProfile;