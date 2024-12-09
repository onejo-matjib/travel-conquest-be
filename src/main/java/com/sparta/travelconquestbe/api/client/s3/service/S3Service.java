package com.sparta.travelconquestbe.api.client.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sparta.travelconquestbe.common.exception.CustomException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3Service {
  private final AmazonS3 s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  // 허용 확장자 리스트
  private static final List<String> ALLOWED_EXTENSIONS =
      Arrays.asList("jpg", "jpeg", "png", "gif", "mp4");
  // 허용 파일 크기
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

  public String uploadFile(MultipartFile file, String uniqueFileName) throws IOException {
    try {
      String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
      if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
        throw new CustomException("S3_002", "허용되지 않는 파일 형식입니다.", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
      }

      if (file.getSize() > MAX_FILE_SIZE) {
        throw new CustomException("S3_003", "파일 크기가 허용 범위를 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE);
      }

      // 파일을 로컬 경로에 저장 (임시 파일 생성)
      File tempFile = Files.createTempFile("upload-", file.getOriginalFilename()).toFile();
      file.transferTo(tempFile);

      // S3 버킷에 업로드
      String key = "routs_uploads/" + uniqueFileName + "_" + file.getOriginalFilename();
      s3Client.putObject(new PutObjectRequest(bucketName, key, tempFile));

      // 업로드된 파일의 URL 반환
      return s3Client.getUrl(bucketName, key).toString();
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      throw new CustomException("S3_001", "파일 업로드가 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // 확장자 추출용 유틸 메소드
  private String getFileExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf(".");
    if (dotIndex == -1) {
      return "";
    }
    return fileName.substring(dotIndex + 1);
  }
}
