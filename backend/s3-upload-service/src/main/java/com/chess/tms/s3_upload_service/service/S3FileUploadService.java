package com.chess.tms.s3_upload_service.service; 
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
 
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
}
