package com.chess.tms.s3_upload_service.unit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.chess.tms.s3_upload_service.service.S3FileUploadService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3FileUploadServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private S3FileUploadService s3FileUploadService;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String FILE_KEY = "test-file.jpg";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3FileUploadService, "bucketName", BUCKET_NAME);
    }

    // Upload Tests
    @Test
    void uploadFile_Success() throws IOException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        // Act
        assertDoesNotThrow(() -> s3FileUploadService.uploadFile(FILE_KEY, multipartFile));

        // Assert
        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3).putObject(putObjectRequestCaptor.capture());

        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
        assertEquals(BUCKET_NAME, capturedRequest.getBucketName());
        assertEquals(FILE_KEY, capturedRequest.getKey());
    }

    @Test
    void uploadFile_WhenIOException_ShouldThrowException() throws IOException {
        // Arrange
        when(multipartFile.getInputStream()).thenThrow(new IOException("Failed to read file"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class,
                () -> s3FileUploadService.uploadFile(FILE_KEY, multipartFile));
        assertEquals("Failed to read file", exception.getMessage());

        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void uploadFile_WhenAmazonS3Exception_ShouldThrowException() throws IOException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        doThrow(new AmazonS3Exception("S3 error")).when(amazonS3).putObject(any(PutObjectRequest.class));

        // Act & Assert
        assertThrows(AmazonS3Exception.class,
                () -> s3FileUploadService.uploadFile(FILE_KEY, multipartFile));
    }

    // Delete Tests
    @Test
    void deleteFile_Success() {
        // Act
        assertDoesNotThrow(() -> s3FileUploadService.deleteFile(FILE_KEY));

        // Assert
        verify(amazonS3).deleteObject(BUCKET_NAME, FILE_KEY);
    }

    @Test
    void deleteFile_WhenAmazonS3Exception_ShouldThrowException() {
        // Arrange
        doThrow(new AmazonS3Exception("Delete failed")).when(amazonS3)
                .deleteObject(BUCKET_NAME, FILE_KEY);

        // Act & Assert
        assertThrows(AmazonS3Exception.class,
                () -> s3FileUploadService.deleteFile(FILE_KEY));
    }

    // Find Tests
    @Test
    void findFile_Success() throws IOException {
        // Arrange
        byte[] expectedData = "test data".getBytes();
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream s3ObjectInputStream = new S3ObjectInputStream(
                new ByteArrayInputStream(expectedData),
                null);

        when(amazonS3.getObject(BUCKET_NAME, FILE_KEY)).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);

        // Act
        byte[] result = s3FileUploadService.findFile(FILE_KEY);

        // Assert
        assertArrayEquals(expectedData, result);
        verify(amazonS3).getObject(BUCKET_NAME, FILE_KEY);
    }

    @Test
    void findFile_WhenFileNotFound_ShouldReturnNull() throws IOException {
        // Arrange
        AmazonS3Exception notFoundException = new AmazonS3Exception("Not found");
        notFoundException.setStatusCode(404);
        when(amazonS3.getObject(BUCKET_NAME, FILE_KEY)).thenThrow(notFoundException);

        // Act
        byte[] result = s3FileUploadService.findFile(FILE_KEY);

        // Assert
        assertNull(result);
        verify(amazonS3).getObject(BUCKET_NAME, FILE_KEY);
    }

    @Test
    void findFile_WhenS3ErrorOccurs_ShouldReturnNull() throws IOException {
        // Arrange
        AmazonS3Exception s3Exception = new AmazonS3Exception("S3 error");
        s3Exception.setStatusCode(500);
        when(amazonS3.getObject(BUCKET_NAME, FILE_KEY)).thenThrow(s3Exception);

        // Act
        byte[] result = s3FileUploadService.findFile(FILE_KEY);

        // Assert
        assertNull(result);
        verify(amazonS3).getObject(BUCKET_NAME, FILE_KEY);
    }

    @Test
    void findFile_WhenNullPointerException_ShouldThrowWrappedIOException() throws IOException {
        // Arrange
        S3Object s3Object = mock(S3Object.class);
        NullPointerException npe = new NullPointerException("Null reference");

        when(amazonS3.getObject(BUCKET_NAME, FILE_KEY)).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenThrow(npe);

        // Act & Assert
        IOException exception = assertThrows(IOException.class,
                () -> s3FileUploadService.findFile(FILE_KEY));

        // Verify exception properties
        assertEquals("Failed to read from S3", exception.getMessage());
        assertEquals(npe, exception.getCause());

        verify(amazonS3).getObject(BUCKET_NAME, FILE_KEY);
    }

}