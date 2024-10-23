import React from "react";
import TournamentDescription from "../AdminTournamentDetails/TournamentDescription";
import useTournamentDetails from "../Hooks/useTournamentDetails";
import { useParams } from "react-router-dom";

function TournamentLeaderboard(){
    
    const { id } = useParams();
    const{tournament}  = useTournamentDetails(id); 


return(
    <div>
    <TournamentDescription tournament={tournament} />
    {/* You can add more components here if needed */}
</div>

); 



}
export default TournamentLeaderboard; 