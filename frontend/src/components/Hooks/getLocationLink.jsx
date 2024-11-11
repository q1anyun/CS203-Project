import React from 'react';

const LocationLink = ({ address, latitude, longitude }) => {
    // Construct the Google Maps URL only if coordinates are available
    const googleMapsUrl = latitude && longitude 
        ? `https://www.google.com/maps?q=${latitude},${longitude}` 
        : null;

    return (
        <div>
            {googleMapsUrl ? (
                <a href={googleMapsUrl} target="_blank" rel="noopener noreferrer">
                    {address || "View Location on Google Maps"}
                </a>
            ) : (
                <p>{address || "Location coordinates are not available."}</p>
            )}
        </div>
    );
};

export default LocationLink;