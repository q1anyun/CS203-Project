import React from 'react';
import { Typography } from '@mui/material';
import { Bracket, Seed, SeedItem, SeedTeam } from 'react-brackets';

const CustomSeed = ({ seed }) => {
    const winnerId = seed.winnerId;
    const isAutoAdvance = !seed.teams[0]?.id && !seed.teams[1]?.id && winnerId !== null;

    return (
        <Seed style={{ fontSize: 20, justifyContent: 'center', alignItems: 'center', color: 'white' }}>
            <SeedItem>
                <div>
                    {isAutoAdvance ? (
                        <SeedTeam style={{ backgroundColor: 'green' }}>
                            <Typography variant="header3" component="span" style={{ color: 'white' }}>
                                Auto Advance PLAYER {winnerId}
                            </Typography>
                        </SeedTeam>
                    ) : (
                        <>
                            <SeedTeam
                                style={{
                                    backgroundColor: winnerId === seed.teams[0]?.id ? 'green' : 'white'
                                }}
                            >
                                <Typography variant="playerProfile2" component="span" style={{ color: winnerId === seed.teams[0]?.id ? 'white' : 'black' }}>
                                    {seed.teams[0]?.name || 'Pending'}
                                </Typography>
                            </SeedTeam>
                            <SeedTeam
                                style={{
                                    backgroundColor: winnerId === seed.teams[1]?.id ? 'green' : 'white'
                                }}
                            >
                                <Typography variant="playerProfile2" component="span" style={{ color: winnerId === seed.teams[1]?.id ? 'white' : 'black' }}>
                                    {seed.teams[1]?.name || 'Pending'}
                                </Typography>
                            </SeedTeam>
                        </>
                    )}
                </div>
            </SeedItem>
        </Seed>
    );
};

const Knockout = ({ rounds }) => {
    return (
        <Bracket
            rounds={rounds}
            renderSeedComponent={(props) => <CustomSeed {...props} />}
            roundTitleComponent={(title) => (
                <Typography variant="header3" align="center">{title}</Typography>
            )}
        />
    );
};

export default Knockout;
