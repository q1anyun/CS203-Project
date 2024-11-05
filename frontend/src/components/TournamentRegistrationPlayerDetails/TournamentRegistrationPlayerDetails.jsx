import * as React from 'react';
import { styled } from '@mui/material/styles';
import { Typography, Avatar, Box } from '@mui/material';
import styles from './TournamentRegistrationPlayerDetails.module.css';
import axios from 'axios';
import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';

const baseURL = import.meta.env.VITE_TOURNAMENT_PLAYER_URL;

const DetailBox = styled(Box)({
    backgroundColor: '#fff',
    borderRadius: '8px',
    padding: '16px',
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
    display: 'flex',
    alignItems: 'center',
});

const DetailBoxContainer = styled(Box)({
    display: 'grid',
    gridTemplateColumns: 'repeat(3, 1fr)',
    gap: '25px',
    marginTop: '20px',
});


function createData(id, firstName, lastName, country) {
    return { id, firstName, lastName, country };
}

function TournamentRegistrationPlayerDetails() {
    const { id } = useParams();
    const [participants, setParticipants] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchParticipants = async () => {
            try {
                const response = await axios.get(`${baseURL}/${id}`);
                const data = response.data;
                console.log(data);
                const formattedData = data.map((participant) =>
                    createData(participant.id, participant.firstName, participant.lastName, participant.country)
                );
                setParticipants(formattedData);
            } catch (error) {
                if (error.response) {
                    const statusCode = error.response.status;
                    const errorMessage = error.response.data?.message || 'An unexpected error occurred';
                    navigate(`/error?statusCode=${statusCode}&errorMessage=${encodeURIComponent(errorMessage)}`);
                } else if (err.request) {
                    navigate(`/error?statusCode=0&errorMessage=${encodeURIComponent('No response from server')}`);
                } else {
                    navigate(`/error?statusCode=500&errorMessage=${encodeURIComponent('Error: ' + err.message)}`);
                }
            }
        };

        fetchParticipants();
    }, [id]);

    return (
        <>
            <div className={styles.container}>
                <Typography variant="header1" component="h2">
                    Registered Participants
                </Typography>
                <DetailBoxContainer>
                    {participants.length === 0 ? (
                        <Typography variant="body1" align="center">
                            Be the first to register!
                        </Typography>
                    ) : (
                        participants.map((participant) => (
                            <DetailBox key={participant.id}>
                                <Avatar
                                    alt={`${participant.firstName} ${participant.lastName}`}
                                    src={participant.profilePhoto}
                                    sx={{ width: 56, height: 56, marginRight: '16px' }}
                                />
                                <Box>
                                    <Link
                                        to={`/profileview/${participant.id}`}
                                        style={{ textDecoration: 'none', color: 'inherit' }}
                                    >
                                        <Typography variant="header3">{`${participant.firstName} ${participant.lastName}`}</Typography>
                                    </Link>
                                    <Typography variant="body1">{participant.country}</Typography>
                                </Box>
                            </DetailBox>
                        ))
                    )}
                </DetailBoxContainer>
            </div>
        </>
    );
}

export default TournamentRegistrationPlayerDetails;