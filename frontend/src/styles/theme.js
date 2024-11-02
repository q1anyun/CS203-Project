import { createTheme } from '@mui/material/styles';


// Define your theme
const theme = createTheme({
    fontFamily: 'League Spartan, sans-serif',
  typography: {
    //styles of font in general 
    header1: {
      fontSize: '40px',
      fontWeight: 'bold',
      textAlign: 'left',
      marginBottom: '30px',
      marginLeft: '20px',
    },
    header2: {
      fontSize: '30px',
      fontWeight: 'bold',
      textAlign: 'left',
    },
    header3: {
      fontSize: '20px',
      fontWeight: 'bold',
    
    },
    header4:{
      fontSize:'15px', 
      fontWeight: 'semibold',
      
    },
    body4: {
      fontSize: '15px',
      fontWeight: 'light',
      textAlign: 'left'
    },
    //font specific to the page 
    
    navBar: {
        fontSize: '18px',
        textAlign: 'center',
         
        fontWeight: 'light',

    }, 

    homePage:{
      fontSize:'70px', 
      fontWeight:'bold', 
      textAlign: 'center',
      

    }, 

    homePage2: {
      fontSize:'20px', 
      letterSpacing: '0.5px',
      fontWeight:'medium',
      textAlign:'center'
     
    }, 
    homePage3: {
      fontSize: '40px',
      fontWeight: 'bold',
      textAlign: 'center',
     
    },

    playerProfile: {
      fontSize: '40px',
      fontWeight:'Medium',
      textAlign: 'center',
     
    },

    playerProfile2: {
      fontSize: '20px',
      fontWeight:'Medium',
      textAlign: 'center',
    }
    // Add more styles as needed
  },
});

// Export your theme or use it directly in your component
export default theme;
