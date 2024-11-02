package com.chess.tms.auth_service.unit;
import com.chess.tms.auth_service.controller.OTPController;
import com.chess.tms.auth_service.dto.OtpRequest;
import com.chess.tms.auth_service.dto.OtpVerificationDTO;
import com.chess.tms.auth_service.service.OTPService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;

class OTPControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OTPService otpService;

    @InjectMocks
    private OTPController otpController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(otpController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSendOtp_Success() throws Exception {
        // Arrange
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setUsername("testuser");
        otpRequest.setEmail("testuser@example.com");

        when(otpService.sendOtp(otpRequest.getUsername(), otpRequest.getEmail())).thenReturn("OTP sent successfully");

        // Act & Assert
        mockMvc.perform(post("/api/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent successfully"));
        
        verify(otpService, times(1)).sendOtp(otpRequest.getUsername(), otpRequest.getEmail());
    }

    @Test
    void testValidateOtp_Success() throws Exception {
        // Arrange
        OtpVerificationDTO otpVerificationDTO = new OtpVerificationDTO();
        otpVerificationDTO.setEmail("testuser@example.com");
        otpVerificationDTO.setOtp("123456");

        when(otpService.validateOtp(otpVerificationDTO.getEmail(), otpVerificationDTO.getOtp()))
                .thenReturn("OTP validated successfully");

        // Act & Assert
        mockMvc.perform(post("/api/otp/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpVerificationDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP validated successfully"));
        
        verify(otpService, times(1)).validateOtp(otpVerificationDTO.getEmail(), otpVerificationDTO.getOtp());
    }

    @Test
    void testResendOtp_Success() throws Exception {
        // Arrange
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setUsername("testuser");
        otpRequest.setEmail("testuser@example.com");

        when(otpService.resendOtp(otpRequest.getUsername(), otpRequest.getEmail())).thenReturn("OTP resent successfully");

        // Act & Assert
        mockMvc.perform(post("/api/otp/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP resent successfully"));
        
        verify(otpService, times(1)).resendOtp(otpRequest.getUsername(), otpRequest.getEmail());
    }
}
