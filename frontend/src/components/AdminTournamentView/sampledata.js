const columns = [
    { name: "Tournament ID", uid: "tournamentId" },
    { name: "Tournament Name", uid: "tournamentName" },
    { name: "Start Date", uid: "startDate" },
    { name: "End Date", uid: "endDate" },
    { name: "Time Control", uid: "timeControl" },
    { name: "Number of Players", uid: "numberOfPlayers" },
    { name: "Status", uid: "status" },
    { name: "Actions", uid: "actions" },
  ];
  
  const tournaments = [
    {
      tournamentId: 1,
      tournamentName: "Chess Masters",
      startDate: "2024-09-10",
      endDate: "2024-09-15",
      timeControl: "Rapid",
      numberOfPlayers: 10,
      status: "active",
    },
    {
      tournamentId: 2,
      tournamentName: "Junior Championship",
      startDate: "2024-09-12",
      endDate: "2024-09-18",
      timeControl: "Blitz",
      numberOfPlayers: 8,
      status: "paused",
    },
    {
      tournamentId: 3,
      tournamentName: "Grand Slam",
      startDate: "2024-09-20",
      endDate: "2024-09-30",
      timeControl: "Classic",
      numberOfPlayers: 16,
      status: "active",
    },
  ];
  