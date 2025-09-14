package com.example.file.service;

import com.example.file.entity.FileMetadata;
import com.example.file.repository.FileMetadataRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 文件服务
 */
@Service
@Transactional
public class FileService {
    
    @Autowired
    private FileMetadataRepository fileMetadataRepository;
    
    @Value("${file.upload.path:/tmp/uploads/}")
    private String uploadPath;
    
    @Value("${file.upload.max-size:10MB}")
    private String maxSize;
    
    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx}")
    private String allowedTypes;
    
    private final Tika tika = new Tika();
    
    private static final List<String> IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    
    /**
     * 上传文件
     */
    public FileMetadata uploadFile(MultipartFile file, Long userId, String businessType) throws IOException {
        // 验证文件
        validateFile(file);
        
        // 创建上传目录
        String datePath = LocalDate.now().toString().replace("-", "/");
        String fullUploadPath = uploadPath + datePath + "/";
        createDirectoryIfNotExists(fullUploadPath);
        
        // 生成存储文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String storedName = UUID.randomUUID().toString() + "." + fileExtension;
        String filePath = fullUploadPath + storedName;
        
        // 保存文件
        Path targetPath = Paths.get(filePath);
        Files.copy(file.getInputStream(), targetPath);
        
        // 计算文件MD5
        String md5Hash = calculateMD5(filePath);
        
        // 检查是否已存在相同文件
        Optional<FileMetadata> existingFile = fileMetadataRepository.findByMd5HashAndStatus(md5Hash, 1);
        if (existingFile.isPresent()) {
            // 删除刚上传的重复文件
            Files.deleteIfExists(targetPath);
            return existingFile.get();
        }
        
        // 创建文件元数据
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setOriginalName(originalFilename);
        fileMetadata.setStoredName(storedName);
        fileMetadata.setFilePath(filePath);
        fileMetadata.setFileSize(file.getSize());
        fileMetadata.setContentType(file.getContentType());
        fileMetadata.setFileExtension(fileExtension);
        fileMetadata.setMd5Hash(md5Hash);
        fileMetadata.setUploadUserId(userId);
        fileMetadata.setBusinessType(businessType);
        
        // 处理图片
        if (isImageFile(fileExtension)) {
            processImage(fileMetadata, filePath);
        }
        
        return fileMetadataRepository.save(fileMetadata);
    }
    
    /**
     * 根据ID获取文件
     */
    public Optional<FileMetadata> getFileById(Long id) {
        return fileMetadataRepository.findByIdAndStatus(id, 1);
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(Long id, Long userId) throws IOException {
        FileMetadata fileMetadata = fileMetadataRepository.findByIdAndStatus(id, 1)
            .orElseThrow(() -> new RuntimeException("文件不存在"));
        
        // 检查权限（简化处理，实际项目中需要更复杂的权限控制）
        if (!fileMetadata.getUploadUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此文件");
        }
        
        // 标记为删除
        fileMetadata.setStatus(0);
        fileMetadata.setUpdateTime(LocalDateTime.now());
        fileMetadataRepository.save(fileMetadata);
        
        // 删除物理文件
        Files.deleteIfExists(Paths.get(fileMetadata.getFilePath()));
        if (fileMetadata.getThumbnailPath() != null) {
            Files.deleteIfExists(Paths.get(fileMetadata.getThumbnailPath()));
        }
    }
    
    /**
     * 获取用户文件列表
     */
    public List<FileMetadata> getUserFiles(Long userId, String businessType) {
        if (StringUtils.hasText(businessType)) {
            return fileMetadataRepository.findByUploadUserIdAndBusinessTypeAndStatusOrderByCreateTimeDesc(
                userId, businessType, 1);
        } else {
            return fileMetadataRepository.findByUploadUserIdAndStatusOrderByCreateTimeDesc(userId, 1);
        }
    }
    
    /**
     * 批量上传文件
     */
    public List<FileMetadata> batchUploadFiles(MultipartFile[] files, Long userId, String businessType) throws IOException {
        List<FileMetadata> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                FileMetadata fileMetadata = uploadFile(file, userId, businessType);
                results.add(fileMetadata);
            } catch (Exception e) {
                // 记录失败的文件，但继续处理其他文件
                System.err.println("文件上传失败: " + file.getOriginalFilename() + ", 错误: " + e.getMessage());
            }
        }
        
        return results;
    }
    
    /**
     * 批量删除文件
     */
    public void batchDeleteFiles(List<Long> fileIds, Long userId) throws IOException {
        for (Long fileId : fileIds) {
            try {
                deleteFile(fileId, userId);
            } catch (Exception e) {
                System.err.println("文件删除失败: " + fileId + ", 错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取文件下载URL
     */
    public String getDownloadUrl(Long fileId, Long userId) {
        FileMetadata fileMetadata = fileMetadataRepository.findByIdAndStatus(fileId, 1)
            .orElseThrow(() -> new RuntimeException("文件不存在"));
        
        // 检查权限
        if (!fileMetadata.getUploadUserId().equals(userId)) {
            throw new RuntimeException("无权限访问此文件");
        }
        
        // 生成临时下载链接（这里简化处理，实际项目中可以生成带签名的临时URL）
        return "/api/file/download/" + fileId;
    }
    
    /**
     * 获取文件统计信息
     */
    public FileStatistics getFileStatistics(Long userId) {
        List<FileMetadata> userFiles = getUserFiles(userId, null);
        
        long totalFiles = userFiles.size();
        long totalSize = userFiles.stream().mapToLong(FileMetadata::getFileSize).sum();
        long imageCount = userFiles.stream().filter(f -> Boolean.TRUE.equals(f.getIsImage())).count();
        long documentCount = totalFiles - imageCount;
        
        return new FileStatistics(totalFiles, totalSize, imageCount, documentCount);
    }
    
    /**
     * 搜索文件
     */
    public List<FileMetadata> searchFiles(Long userId, String keyword, String fileType) {
        // 这里简化处理，实际项目中可以使用更复杂的搜索逻辑
        List<FileMetadata> allFiles = getUserFiles(userId, null);
        
        return allFiles.stream()
            .filter(file -> {
                boolean matchKeyword = keyword == null || 
                    file.getOriginalName().toLowerCase().contains(keyword.toLowerCase());
                boolean matchType = fileType == null || 
                    file.getFileExtension().equalsIgnoreCase(fileType);
                return matchKeyword && matchType;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 压缩图片
     */
    public FileMetadata compressImage(Long fileId, Long userId, float quality) throws IOException {
        FileMetadata originalFile = fileMetadataRepository.findByIdAndStatus(fileId, 1)
            .orElseThrow(() -> new RuntimeException("文件不存在"));
        
        if (!originalFile.getUploadUserId().equals(userId)) {
            throw new RuntimeException("无权限操作此文件");
        }
        
        if (!Boolean.TRUE.equals(originalFile.getIsImage())) {
            throw new RuntimeException("只能压缩图片文件");
        }
        
        String originalPath = originalFile.getFilePath();
        String compressedPath = originalPath.replace(".", "_compressed.");
        
        // 压缩图片
        Thumbnails.of(originalPath)
            .scale(1.0)
            .outputQuality(quality)
            .toFile(compressedPath);
        
        // 创建新的文件记录
        File compressedFile = new File(compressedPath);
        FileMetadata compressedMetadata = new FileMetadata();
        compressedMetadata.setOriginalName("compressed_" + originalFile.getOriginalName());
        compressedMetadata.setStoredName("compressed_" + originalFile.getStoredName());
        compressedMetadata.setFilePath(compressedPath);
        compressedMetadata.setFileSize(compressedFile.length());
        compressedMetadata.setContentType(originalFile.getContentType());
        compressedMetadata.setFileExtension(originalFile.getFileExtension());
        compressedMetadata.setMd5Hash(calculateMD5(compressedPath));
        compressedMetadata.setUploadUserId(userId);
        compressedMetadata.setBusinessType(originalFile.getBusinessType());
        compressedMetadata.setIsImage(true);
        
        // 读取压缩后的图片尺寸
        BufferedImage image = ImageIO.read(compressedFile);
        if (image != null) {
            compressedMetadata.setWidth(image.getWidth());
            compressedMetadata.setHeight(image.getHeight());
        }
        
        return fileMetadataRepository.save(compressedMetadata);
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 检查文件大小
        long maxSizeBytes = parseSize(maxSize);
        if (file.getSize() > maxSizeBytes) {
            throw new RuntimeException("文件大小超过限制: " + maxSize);
        }
        
        // 检查文件类型
        String fileExtension = getFileExtension(file.getOriginalFilename());
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        if (!allowedTypeList.contains(fileExtension.toLowerCase())) {
            throw new RuntimeException("不支持的文件类型: " + fileExtension);
        }
    }
    
    /**
     * 处理图片文件
     */
    private void processImage(FileMetadata fileMetadata, String filePath) throws IOException {
        fileMetadata.setIsImage(true);
        
        // 读取图片尺寸
        BufferedImage image = ImageIO.read(new File(filePath));
        if (image != null) {
            fileMetadata.setWidth(image.getWidth());
            fileMetadata.setHeight(image.getHeight());
            
            // 生成缩略图
            String thumbnailPath = generateThumbnail(filePath);
            fileMetadata.setThumbnailPath(thumbnailPath);
        }
    }
    
    /**
     * 生成缩略图
     */
    private String generateThumbnail(String originalPath) throws IOException {
        String thumbnailPath = originalPath.replace(".", "_thumb.");
        
        Thumbnails.of(originalPath)
            .size(200, 200)
            .keepAspectRatio(true)
            .toFile(thumbnailPath);
        
        return thumbnailPath;
    }
    
    /**
     * 计算文件MD5
     */
    private String calculateMD5(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return DigestUtils.md5DigestAsHex(fis);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String extension) {
        return IMAGE_TYPES.contains(extension.toLowerCase());
    }
    
    /**
     * 创建目录
     */
    private void createDirectoryIfNotExists(String path) throws IOException {
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }
    
    /**
     * 解析文件大小
     */
    private long parseSize(String size) {
        size = size.toUpperCase();
        if (size.endsWith("KB")) {
            return Long.parseLong(size.replace("KB", "")) * 1024;
        } else if (size.endsWith("MB")) {
            return Long.parseLong(size.replace("MB", "")) * 1024 * 1024;
        } else if (size.endsWith("GB")) {
            return Long.parseLong(size.replace("GB", "")) * 1024 * 1024 * 1024;
        }
        return Long.parseLong(size);
    }
    
    /**
     * 文件统计信息
     */
    public static class FileStatistics {
        private long totalFiles;
        private long totalSize;
        private long imageCount;
        private long documentCount;
        
        public FileStatistics(long totalFiles, long totalSize, long imageCount, long documentCount) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.imageCount = imageCount;
            this.documentCount = documentCount;
        }
        
        // Getters and Setters
        public long getTotalFiles() {
            return totalFiles;
        }
        
        public void setTotalFiles(long totalFiles) {
            this.totalFiles = totalFiles;
        }
        
        public long getTotalSize() {
            return totalSize;
        }
        
        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }
        
        public long getImageCount() {
            return imageCount;
        }
        
        public void setImageCount(long imageCount) {
            this.imageCount = imageCount;
        }
        
        public long getDocumentCount() {
            return documentCount;
        }
        
        public void setDocumentCount(long documentCount) {
            this.documentCount = documentCount;
        }
    }
}
