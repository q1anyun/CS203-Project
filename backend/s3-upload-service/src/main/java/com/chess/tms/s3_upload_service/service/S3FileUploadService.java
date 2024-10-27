package com.chess.tms.s3_upload_service.service; 
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
 
@Service
public class S3FileUploadService {
 
    @Autowired
    private AmazonS3 amazonS3;
 
    @Value("${aws.s3.bucketName}")
    private String bucketName;
 
    public void uploadFile(String key, MultipartFile file) throws IOException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file.getInputStream(), null);
        amazonS3.putObject(putObjectRequest);
    }

     // Method to delete a file from S3
     public void deleteFile(String fileName) throws IOException {
        amazonS3.deleteObject(bucketName, fileName);
    }

    
    // Method to find a file in S3 and return its data as a byte array
    public byte[] findFile(String fileName) throws IOException {
        try {
            S3Object s3Object = amazonS3.getObject(bucketName, fileName);
            try (InputStream inputStream = s3Object.getObjectContent();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                System.out.println("Error: The specified key does not exist.");
            } else {
                System.out.println("AWS S3 error: " + e.getMessage());
            }
            return null; // Return null or handle differently if required
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            throw new IOException("Failed to read from S3", e);
        }
    }
}
    

    

