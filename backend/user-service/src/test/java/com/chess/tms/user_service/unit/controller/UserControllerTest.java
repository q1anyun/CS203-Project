package com.chess.tms.user_service.unit.controller;

import com.chess.tms.user_service.controller.UserController;
import com.chess.tms.user_service.dto.UpdateUserRequestDTO;
import com.chess.tms.user_service.dto.UserResponseDTO;
import com.chess.tms.user_service.enums.UserRole;
import com.chess.tms.user_service.exception.GlobalExceptionHandler;
import com.chess.tms.user_service.exception.UserNotFoundException;
import com.chess.tms.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class UserControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController; 

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getUser_ValidUserId_ReturnsUserDetails() throws Exception {
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setUsername("johndoe");
        userResponse.setEmail("johndoe@example.com");
        userResponse.setRole(UserRole.PLAYER); 

        when(userService.getUser(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/user/current")
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("johndoe@example.com"))
                .andExpect(jsonPath("$.role").value("PLAYER"));
        
        verify(userService, times(1)).getUser(1L);
    }

    @Test
    void getAllUsers_ReturnsUserList() throws Exception {
        UserResponseDTO user1 = new UserResponseDTO();
        user1.setUsername("johndoe");
        user1.setEmail("johndoe@example.com");
        user1.setRole(UserRole.PLAYER);

        UserResponseDTO user2 = new UserResponseDTO();
        user2.setUsername("janedoe");
        user2.setEmail("janedoe@example.com");
        user2.setRole(UserRole.ADMIN);
        
        List<UserResponseDTO> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("johndoe"))
                .andExpect(jsonPath("$[1].username").value("janedoe"));
        
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void updateUser_ValidRequest_UpdatesUser() throws Exception {
        UpdateUserRequestDTO updateUserRequest = new UpdateUserRequestDTO();
        updateUserRequest.setEmail("johndoe@example.com");
        updateUserRequest.setOldPassword("oldPassword123");
        updateUserRequest.setNewPassword("newPassword123");
        updateUserRequest.setRole(UserRole.ADMIN);

        String updateUserRequestJson = "{ \"email\": \"johndoe@example.com\", \"oldPassword\": \"oldPassword123\", \"newPassword\": \"newPassword123\", \"role\": \"ADMIN\" }";

        mockMvc.perform(put("/api/user/current")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateUserRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated credentials"));

        verify(userService, times(1)).updateUser(eq(1L), any(UpdateUserRequestDTO.class));
    }

    @Test
    void getUser_InvalidUserId_ReturnsNotFound() throws Exception {
        when(userService.getUser(99L)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/user/current")
                .header("X-User-Id", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService, times(1)).getUser(99L);
    }

    @Test
    void getAllUsers_NoUsers_ReturnsEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void updateUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        String invalidUpdateJson = "{ \"email\": \"\", \"oldPassword\": \"\", \"newPassword\": \"\", \"role\": \"\" }";

        mockMvc.perform(put("/api/user/current")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUpdateJson))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).updateUser(anyLong(), any(UpdateUserRequestDTO.class));
    }



  

}
