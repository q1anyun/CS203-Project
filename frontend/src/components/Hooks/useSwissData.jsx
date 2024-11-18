import { useState, useEffect } from 'react';
import axios from 'axios';
const swissStandingURL = import.meta.env.VITE_TOURNAMENT_SWISSSTANDING_URL; 
const swissBracketURL = import.meta.env.VITE_TOURNAMENT_SWISSBRACKET_URL;

function useSwissData(SwissBracketID) {
    const [swissRoundDetails, setSwissRoundDetails] = useState([]);
    const [swissStandings, setSwissStandings] = useState([{}]); 
    useEffect(() => {
        const fetchSwissBracket = async () => {
            const response = await axios.get(`${swissBracketURL}/${SwissBracketID}`);
            setSwissRoundDetails(response.data);
        };
        fetchSwissBracket();
    }, [SwissBracketID]);

    
    useEffect(() => {
        const fetchSwissStandings = async () => {
            const response = await axios.get(`${swissStandingURL}/${SwissBracketID}`);
            setSwissStandings(response.data);
        };
        fetchSwissStandings();
    }, [SwissBracketID]);

    return { swissStandings, swissRoundDetails };
}; 
export default useSwissData; 
