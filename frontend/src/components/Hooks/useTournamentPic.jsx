import { useState, useEffect } from 'react';
import axios from 'axios';

const useTournamentPic = (tournamentId) => {
    const [tournamentPic, setTournamentPic] = useState(null);


    const baseURL = import.meta.env.VITE_TOURNAMENT_SERVICE_URL;
    

    useEffect(() => {
        const fetchTournamentPic = async () => {
         
                const response = await axios.get(`${baseURL}/getTournamentImage/${tournamentId}`, {
                    headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
                });
                setTournamentPic(response.data);
               
          
        };

        fetchTournamentPic(); 
    }, [tournamentId]);


    return { tournamentPic };
};

export default useTournamentPic;
