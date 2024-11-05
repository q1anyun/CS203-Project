import React from 'react';
import { Dialog, DialogTitle, DialogContent, TextField, FormControl, InputLabel, Select, MenuItem, Button, Avatar } from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { styled } from '@mui/material/styles';

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

function EditProfileDialog({ open, handleClose, playerDetails, handleDetailChange, handleFileAndImageUpload, handleSave, options, profilePic }) {

  return (
    <Dialog open={open} onClose={handleClose} sx={{ '& .MuiDialog-paper': { width: '80%', maxWidth: '600px' } }}>
      <DialogTitle>Edit Profile</DialogTitle>
      <DialogContent sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <Avatar sx={{ width: 200, height: 200, marginTop: 15 }} alt={playerDetails.firstName} src={profilePic} />
        <Button component="label" variant="contained" startIcon={<CloudUploadIcon />}>
          Upload files
          <VisuallyHiddenInput type="file" onChange={handleFileAndImageUpload} />
        </Button>
        <TextField
          label="First Name"
          name="firstName"
          value={playerDetails.firstName}
          onChange={handleDetailChange}
          variant="outlined"
          fullWidth
          margin="normal"
        />
        <TextField
          label="Last Name"
          name="lastName"
          value={playerDetails.lastName}
          onChange={handleDetailChange}
          variant="outlined"
          fullWidth
          margin="normal"
        />
        <FormControl fullWidth margin="normal">
          <InputLabel>Country</InputLabel>
          <Select
            name="country"
            value={playerDetails.country}
            label="Country"
            onChange={handleDetailChange}
          >
            {options.map((country) => (
              <MenuItem key={country.value} value={country.value}>
                {country.label}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <Button onClick={handleSave} color="primary" variant="contained" sx={{ mt: 2 }}>
          Save
        </Button>
      </DialogContent>
    </Dialog>
  );
}

export default EditProfileDialog;
