import { useState, useEffect } from 'react';
import axios from 'axios';

const useTournamentDetails = (tournamentId) => {
    const [tournament, setTournament] = useState({});


    const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
    

    useEffect(() => {
        const fetchTournamentDetails = async () => {
         
                const response = await axios.get(`${baseURL}/${tournamentId}`, {
                    headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
                });
                setTournament(response.data);
                console.log(tournament); 
               
          
        };

        fetchTournamentDetails();
    }, [tournamentId]);


    return { tournament };
};

export default useTournamentDetails;
