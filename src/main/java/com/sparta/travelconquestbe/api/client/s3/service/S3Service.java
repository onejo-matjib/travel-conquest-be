package com.sparta.travelconquestbe.api.client.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.sparta.travelconquestbe.common.exception.CustomException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3Service {
  private final AmazonS3 s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  private static final List<String> ALLOWED_EXTENSIONS =
      Arrays.asList("jpg", "jpeg", "png", "gif", "mp4", "csv");
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

  public String uploadFile(MultipartFile file, String uniqueFileName) throws IOException {
    File tempFile = null;
    try {
      String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
      if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
        throw new CustomException(
            "S3#2_001", "허용되지 않는 파일 형식입니다.", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
      }

      if (file.getSize() > MAX_FILE_SIZE) {
        throw new CustomException(
            "S3#3_001", "파일 크기가 허용 범위를 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE);
      }

      // 파일을 로컬 경로에 저장 (임시 파일 생성)
      tempFile = Files.createTempFile("upload-", file.getOriginalFilename()).toFile();
      file.transferTo(tempFile);

      // S3 버킷 업로드
      String key = "routs_uploads/" + uniqueFileName + "_" + file.getOriginalFilename();
      s3Client.putObject(new PutObjectRequest(bucketName, key, tempFile));

      // 업로드된 파일의 URL 반환
      return s3Client.getUrl(bucketName, key).toString();
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      throw new CustomException("S3#1_001", "파일 업로드가 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    } finally {
      // 임시파일 삭제
      tempFile.delete();
    }
  }

  public void deleteFile(List<String> mediaUrls) {
    String bucketUrlPrefix = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/";

    mediaUrls.stream()
        .map(url -> url.replace(bucketUrlPrefix, ""))
        .map(
            key -> {
              try {
                return URLDecoder.decode(key, StandardCharsets.UTF_8);
              } catch (Exception e) {
                throw new CustomException("S3#4_001", "key 값 디코딩이 실패했습니다.", HttpStatus.BAD_REQUEST);
              }
            })
        .forEach(
            key -> {
              s3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
            });
  }

  // 버킷에서 특정 폴더에 있는 목록 가져오기
  public List<String> listFiles(String folderPath) {
    return s3Client.listObjectsV2(bucketName, folderPath).getObjectSummaries().stream()
        .map(S3ObjectSummary::getKey)
        .filter(key -> !key.endsWith("/"))
        .toList();
  }

  // InputStream 으로 가져오기
  public InputStream getFile(String fileKey) {
    return s3Client.getObject(bucketName, fileKey).getObjectContent();
  }

  // S3에서 파일을 임시 파일로 변환하여 리소스 배열로 반환
  public Resource[] getS3ResourcesAsTempFiles(String folderPath) {
    List<String> fileKeys = listFiles(folderPath);
    return fileKeys.stream()
            .map(
                    fileKey -> {
                      try {
                        File tempFile = File.createTempFile("temp-", ".csv");
                        try (InputStream inputStream = getFile(fileKey);
                             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                          byte[] buffer = new byte[8192];
                          int bytesRead;
                          while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                          }
                        }
                        tempFile.deleteOnExit();
                        return new FileSystemResource(tempFile);
                      } catch (IOException e) {
                        throw new RuntimeException("Failed to create temp file from S3", e);
                      }
                    })
            .toArray(Resource[]::new);
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
