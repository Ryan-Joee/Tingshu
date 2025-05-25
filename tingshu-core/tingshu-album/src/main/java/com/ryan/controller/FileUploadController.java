package com.ryan.controller;

import com.ryan.minio.MinioUploader;
import com.ryan.result.RetVal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author ryan
 * @since 2025-04-26
 */
@Tag(name = "上传管理接口")
@RestController
@RequestMapping(value = "/api/album")
public class FileUploadController {

    @Autowired
    private MinioUploader minioUploader;

    @Operation(summary = "文件上传")
    @PostMapping("fileUpload")
    public RetVal fileUpload(MultipartFile file) throws Exception {
        String retUrl = minioUploader.uploadFile(file);
        return RetVal.ok(retUrl);
    }
}
