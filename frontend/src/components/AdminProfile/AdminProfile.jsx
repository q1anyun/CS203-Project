import React, { useState }  from 'react';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { styled } from '@mui/material/styles';
import { Card, CardContent, Typography, Avatar, Box,  Button, Tabs, Tab, Dialog, DialogTitle, DialogContent, TextField } from '@mui/material';

const VisuallyHiddenInput = styled('input')({
  clip: 'rect(0 0 0 0)',
  clipPath: 'inset(50%)',
  height: 1,
  overflow: 'hidden',
  position: 'absolute',
  bottom: 0,
  left: 0,
  whiteSpace: 'nowrap',
  width: 1,
});

function AdminProfile({ profilePic , onProfilePicUpdate}) {
  const [value, setValue] = useState(0); // State for managing tab selection
  const [openEdit, setOpenEdit] = useState(false);
  const [localProfilePic, setLocalProfilePic] = useState(profilePic);
  const [AdminName, setAdminName] = useState('Magnus Carlsen');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  const handleOpenEdit = () => setOpenEdit(true);
  const handleCloseEdit = () => setOpenEdit(false);

  const handleNameChange = (event) => setAdminName(event.target.value);
  const handleFirstNameChange = (event) => setFirstName(event.target.value);
  const handleLastNameChange = (event) => setLastName(event.target.value);
  const handleEmailChange = (event) => setEmail(event.target.value);
  const handlePasswordChange = (event) => setPassword(event.target.value);

  const handleFileAndImageUpload = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      const imageUrl = URL.createObjectURL(file);
      setLocalProfilePic(imageUrl);
      onProfilePicUpdate(imageUrl);
    }
  };
  

  const handleSave = async () => {
    const formData = new FormData();
    formData.append('AdminName', AdminName);
    formData.append('firstName', firstName);
    formData.append('lastName', lastName);
    formData.append('email', email);
    formData.append('password', password);

    if (selectedFile) {
      formData.append('profilePic', selectedFile);
    }

    try {
      const response = await fetch('/api/update-profile', {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        console.log('Profile updated successfully');
        // Optionally, refresh the UI with the new data
      } else {
        console.error('Error updating profile');
      }
    } catch (error) {
      console.error('Error:', error);
    }

    handleCloseEdit();
  };

  return (
    <Box
      sx={{

        display: 'grid',
        gridTemplateRows: '1fr 1fr',
        height: 'auto', // Full viewport height
        backgroundColor: '#f0f0f0',// Optional: background color for the page
        justifyItems: 'center',

      }}
    >

      <Card sx={{ width: '80%', height: '500px', padding: 2, marginTop: '5%' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          {/* Profile Card Section */}
          <Avatar
            sx={{ width: 200, height: 200, marginTop: 2 }}
            alt={AdminName}
            src={localProfilePic}
          />

          <Button
            className="button"
            variant="contained"
            color="primary"
            onClick={handleOpenEdit}
          >
            Edit Profile
          </Button>
          <CardContent>
            <Typography variant="h4">{AdminName}</Typography>
          </CardContent>
        </Box>
      </Card>
     
      
      {/*dialog to edit the profile*/}
      <Dialog open={openEdit} onClose={handleCloseEdit}
       sx={{ '& .MuiDialog-paper': { width: '80%', maxWidth: '600px' } }} >
        <DialogTitle>Edit Profile</DialogTitle>
        <DialogContent
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            
          }}>
          
          {/* Display Existing Profile Picture */}

          <Avatar
            sx={{ width: 200, height: 200, marginTop: 1 }}
            alt={AdminName}
            src={localProfilePic}  // Use current profile picture
          />


        

        {/* Label to Trigger File Input */}
        <Button
      component="label"
      variant="contained"
      tabIndex={-1}
      startIcon={<CloudUploadIcon />}
    >
      Upload files
      <VisuallyHiddenInput
        type="file"
        onChange={handleFileAndImageUpload}
      
      />
    </Button>

          <TextField
            margin="dense"
            label="Username"
            fullWidth
            value={AdminName}
            onChange={handleNameChange}
          />
          <TextField
            margin="dense"
            label="First Name"
            fullWidth
            value={firstName}
            onChange={handleFirstNameChange}
          />
          <TextField
            margin="dense"
            label="Last Name"
            fullWidth
            value={lastName}
            onChange={handleLastNameChange}
          />
          <TextField
            margin="dense"
            label="Email"
            fullWidth
            value={email}
            onChange={handleEmailChange}
          />
          <TextField
            margin="dense"
            label="Password"
            fullWidth
            type="password"
            value={password}
            onChange={handlePasswordChange}
          />


          <Button onClick={handleSave} color="primary">
            Save
          </Button>
        </DialogContent>
      </Dialog>

    </Box>
  );
}

export default AdminProfile;
