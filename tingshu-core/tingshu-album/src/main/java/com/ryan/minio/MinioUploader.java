package com.ryan.minio;

import io.minio.*;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
@EnableConfigurationProperties(MinioProperties.class) // 写上这个注释后，在MinioProperties类上就不需要标注 @Component注解了
public class MinioUploader {

    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private MinioClient minioClient;

    /**
     * 这里有个细节：
     * 如果自动注入的顺序是：
     *     @Autowired
     *     private MinioClient minioClient;
     *     @Autowired
     *     private MinioProperties minioProperties;
     *  那么在启动项目是，这里会报空指针异常
     *  因为Spring会先扫描到MinioClient这个Bean，
     *  然后就扫描MinioClient这个Bean的代码实现
     *  之后就扫描到:MinioClient.builder()
     *                         .endpoint(minioProperties.getEndPoint())...
     *  这个部分，而这个时候Spring还没扫描到MinioProperties，即MinioProperties还没有在Spring容器中
     *  所以就会报空指针异常
     */

    @SneakyThrows //
    @Bean
    public MinioClient minioClient() {
        // Create a minioClient with the MinIO server playground, its access key and secret key.
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(minioProperties.getEndPoint())
                        .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                        .build();

        // Make 'tingshu' bucket if not exist. 创建一个桶
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
        } else {
            System.out.println("桶"+ minioProperties.getBucketName() +"已经存在");
        }
        return minioClient;
    }

    public String uploadFile(MultipartFile file) throws Exception {
        // 设置存储对象的名称
        String prefix = UUID.randomUUID().toString().replaceAll("-", "");
        String originalFilename = file.getOriginalFilename();
        String suffix = FilenameUtils.getExtension(originalFilename);
        String fileName = prefix + "." + suffix;
        // 上传文件
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProperties.getBucketName() )
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
        // 返回存储对象的URL 例如：http://192.168.174.142:9000/tingshu/ryan-0.jpg
        String retUrl = minioProperties.getEndPoint() + "/" + minioProperties.getBucketName() + "/" + fileName;
        return retUrl;
    }
}
