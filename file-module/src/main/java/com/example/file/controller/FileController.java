package com.example.file.controller;

import com.example.common.web.ApiResponse;
import com.example.file.entity.FileMetadata;
import com.example.file.service.FileService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 文件管理控制器
 */
@Api(tags = "文件管理")
@RestController
@RequestMapping("/file")
public class FileController {
    
    @Autowired
    private FileService fileService;

    @ApiOperation(value = "上传文件", notes = "通过 MultipartFile 上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "上传文件", required = true, dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "businessType", value = "业务类型", required = false, dataType = "string", paramType = "form")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileMetadata> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "businessType", defaultValue = "common") String businessType,
            HttpServletRequest request) {
        
        try {
            // 从JWT认证过滤器设置的请求属性中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ApiResponse.error("用户认证信息无效");
            }
            
            FileMetadata fileMetadata = fileService.uploadFile(file, userId, businessType);
            return ApiResponse.success("上传成功", fileMetadata);
        } catch (IOException e) {
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("批量上传文件")
    @PostMapping("/upload/batch")
    public ApiResponse<List<FileMetadata>> uploadFiles(
            @ApiParam("文件列表") @RequestParam("files") MultipartFile[] files,
            @ApiParam("业务类型") @RequestParam(value = "businessType", defaultValue = "common") String businessType) {
        
        try {
            Long userId = 1L;
            List<FileMetadata> results = new java.util.ArrayList<>();
            
            for (MultipartFile file : files) {
                FileMetadata fileMetadata = fileService.uploadFile(file, userId, businessType);
                results.add(fileMetadata);
            }
            
            return ApiResponse.success("批量上传成功", results);
        } catch (IOException e) {
            return ApiResponse.error("批量上传失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("下载文件")
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            FileMetadata fileMetadata = fileService.getFileById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
            
            Resource resource = new FileSystemResource(fileMetadata.getFilePath());
            if (!resource.exists()) {
                throw new RuntimeException("文件不存在");
            }
            
            String contentType = Files.probeContentType(Paths.get(fileMetadata.getFilePath()));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            String encodedFilename = URLEncoder.encode(fileMetadata.getOriginalName(), StandardCharsets.UTF_8.toString());
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @ApiOperation("预览文件")
    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        try {
            FileMetadata fileMetadata = fileService.getFileById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
            
            Resource resource = new FileSystemResource(fileMetadata.getFilePath());
            if (!resource.exists()) {
                throw new RuntimeException("文件不存在");
            }
            
            String contentType = fileMetadata.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @ApiOperation("获取缩略图")
    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable Long id) {
        try {
            FileMetadata fileMetadata = fileService.getFileById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
            
            if (!fileMetadata.getIsImage() || fileMetadata.getThumbnailPath() == null) {
                throw new RuntimeException("缩略图不存在");
            }
            
            Resource resource = new FileSystemResource(fileMetadata.getThumbnailPath());
            if (!resource.exists()) {
                throw new RuntimeException("缩略图文件不存在");
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
                
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @ApiOperation("删除文件")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteFile(@PathVariable Long id) {
        try {
            Long userId = 1L; // 应该从认证信息中获取
            fileService.deleteFile(id, userId);
            return ApiResponse.success("删除成功");
        } catch (IOException e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取文件信息")
    @GetMapping("/{id}")
    public ApiResponse<FileMetadata> getFileInfo(@PathVariable Long id) {
        FileMetadata fileMetadata = fileService.getFileById(id)
            .orElseThrow(() -> new RuntimeException("文件不存在"));
        return ApiResponse.success(fileMetadata);
    }
    
    @ApiOperation("获取用户文件列表")
    @GetMapping("/list")
    public ApiResponse<List<FileMetadata>> getUserFiles(
            @ApiParam("业务类型") @RequestParam(required = false) String businessType,
            HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        List<FileMetadata> files = fileService.getUserFiles(userId, businessType);
        return ApiResponse.success(files);
    }
    
    @ApiOperation("批量删除文件")
    @DeleteMapping("/batch")
    public ApiResponse<String> batchDeleteFiles(
            @RequestBody @Valid BatchDeleteRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            fileService.batchDeleteFiles(request.getFileIds(), userId);
            return ApiResponse.success("批量删除成功");
        } catch (IOException e) {
            return ApiResponse.error("批量删除失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("搜索文件")
    @GetMapping("/search")
    public ApiResponse<List<FileMetadata>> searchFiles(
            @ApiParam("关键字") @RequestParam(required = false) String keyword,
            @ApiParam("文件类型") @RequestParam(required = false) String fileType,
            HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        List<FileMetadata> files = fileService.searchFiles(userId, keyword, fileType);
        return ApiResponse.success(files);
    }
    
    @ApiOperation("获取文件统计信息")
    @GetMapping("/statistics")
    public ApiResponse<FileService.FileStatistics> getFileStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        FileService.FileStatistics statistics = fileService.getFileStatistics(userId);
        return ApiResponse.success(statistics);
    }
    
    @ApiOperation("压缩图片")
    @PostMapping("/compress/{id}")
    public ApiResponse<FileMetadata> compressImage(
            @PathVariable Long id,
            @ApiParam("压缩质量(0.1-1.0)") @RequestParam(defaultValue = "0.8") float quality,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            FileMetadata compressedFile = fileService.compressImage(id, userId, quality);
            return ApiResponse.success("图片压缩成功", compressedFile);
        } catch (IOException e) {
            return ApiResponse.error("图片压缩失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取文件下载链接")
    @GetMapping("/download-url/{id}")
    public ApiResponse<String> getDownloadUrl(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String downloadUrl = fileService.getDownloadUrl(id, userId);
        return ApiResponse.success(downloadUrl);
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userIdHeader)) {
            return Long.valueOf(userIdHeader);
        }
        
        // 如果没有从网关传递的用户ID，返回默认值（开发环境）
        return 1L;
    }
    
    // 内部类定义
    public static class BatchDeleteRequest {
        @NotEmpty(message = "文件ID列表不能为空")
        private List<Long> fileIds;
        
        public List<Long> getFileIds() {
            return fileIds;
        }
        
        public void setFileIds(List<Long> fileIds) {
            this.fileIds = fileIds;
        }
    }
}
