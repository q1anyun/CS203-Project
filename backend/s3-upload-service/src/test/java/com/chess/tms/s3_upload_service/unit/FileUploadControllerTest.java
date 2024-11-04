package com.chess.tms.s3_upload_service.unit;

import com.chess.tms.s3_upload_service.controller.FileUploadController;
import com.chess.tms.s3_upload_service.service.S3FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

    @Mock
    private S3FileUploadService fileUploadService;

    @InjectMocks
    private FileUploadController fileUploadController;

    private MultipartFile mockFile;
    private static final String FILENAME = "test-image.jpg";
    private static final byte[] FILE_CONTENT = "test image content".getBytes();

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
            "file",
            FILENAME,
            MediaType.IMAGE_JPEG_VALUE,
            FILE_CONTENT
        );
    }

    // Upload Tests
    @Test
    void uploadFile_Success() throws IOException {
        // Act
        ResponseEntity<String> response = fileUploadController.uploadFile(mockFile, FILENAME);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully as " + FILENAME + "!", response.getBody());
        verify(fileUploadService).uploadFile(eq(FILENAME), eq(mockFile));
    }

    @Test
    void uploadFile_WhenIOException_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        doThrow(new IOException("Upload failed")).when(fileUploadService)
            .uploadFile(any(), any());

        // Act
        ResponseEntity<String> response = fileUploadController.uploadFile(mockFile, FILENAME);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error uploading file: Upload failed", response.getBody());
        verify(fileUploadService).uploadFile(eq(FILENAME), eq(mockFile));
    }

    // Find File Tests
    @Test
    void findFile_Success_JPG() throws IOException {
        // Arrange
        when(fileUploadService.findFile(FILENAME)).thenReturn(FILE_CONTENT);

        // Act
        ResponseEntity<byte[]> response = fileUploadController.findFile(FILENAME);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
        assertArrayEquals(FILE_CONTENT, response.getBody());
        verify(fileUploadService).findFile(FILENAME);
    }


        // Find File Tests
        @Test
        void findFile_Success_JPEG() throws IOException {

            // Arrange
            String jpegFileName = "test.jpeg";
            when(fileUploadService.findFile(jpegFileName)).thenReturn(FILE_CONTENT);
    
            // Act
            ResponseEntity<byte[]> response = fileUploadController.findFile(jpegFileName);
    
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
            assertArrayEquals(FILE_CONTENT, response.getBody());
            verify(fileUploadService).findFile(jpegFileName);
        }

    @Test
    void findFile_Success_PNG() throws IOException {
        // Arrange
        String pngFilename = "test.png";
        when(fileUploadService.findFile(pngFilename)).thenReturn(FILE_CONTENT);

        // Act
        ResponseEntity<byte[]> response = fileUploadController.findFile(pngFilename);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertArrayEquals(FILE_CONTENT, response.getBody());
        verify(fileUploadService).findFile(pngFilename);
    }

    @Test
    void findFile_Success_GIF() throws IOException {
        // Arrange
        String gifFilename = "test.gif";
        when(fileUploadService.findFile(gifFilename)).thenReturn(FILE_CONTENT);

        // Act
        ResponseEntity<byte[]> response = fileUploadController.findFile(gifFilename);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_GIF, response.getHeaders().getContentType());
        assertArrayEquals(FILE_CONTENT, response.getBody());
        verify(fileUploadService).findFile(gifFilename);
    }

    @Test
    void findFile_Success_UnknownType() throws IOException {
        // Arrange
        String unknownFilename = "test.xyz";
        when(fileUploadService.findFile(unknownFilename)).thenReturn(FILE_CONTENT);

        // Act
        ResponseEntity<byte[]> response = fileUploadController.findFile(unknownFilename);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertArrayEquals(FILE_CONTENT, response.getBody());
        verify(fileUploadService).findFile(unknownFilename);
    }

    @Test
    void findFile_WhenIOException_ShouldReturnNotFound() throws IOException {
        // Arrange
        when(fileUploadService.findFile(FILENAME)).thenThrow(new IOException("File not found"));

        // Act
        ResponseEntity<byte[]> response = fileUploadController.findFile(FILENAME);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(fileUploadService).findFile(FILENAME);
    }

    @Test
    void findFile_WhenNullData_ShouldReturnNotFound() throws IOException {
        // Arrange
        when(fileUploadService.findFile(FILENAME)).thenReturn(null);

        // Act
        ResponseEntity<byte[]> response = fileUploadController.findFile(FILENAME);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(fileUploadService).findFile(FILENAME);
    }
}