package com.example.file.repository;

import com.example.file.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文件元数据数据访问层
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    
    /**
     * 根据MD5和状态查找文件
     */
    Optional<FileMetadata> findByMd5HashAndStatus(String md5Hash, Integer status);
    
    /**
     * 根据ID和状态查找文件
     */
    Optional<FileMetadata> findByIdAndStatus(Long id, Integer status);
    
    /**
     * 根据用户ID和状态查找文件列表
     */
    List<FileMetadata> findByUploadUserIdAndStatusOrderByCreateTimeDesc(Long uploadUserId, Integer status);
    
    /**
     * 根据用户ID、业务类型和状态查找文件列表
     */
    List<FileMetadata> findByUploadUserIdAndBusinessTypeAndStatusOrderByCreateTimeDesc(
        Long uploadUserId, String businessType, Integer status);
    
    /**
     * 根据业务类型和状态查找文件列表
     */
    List<FileMetadata> findByBusinessTypeAndStatusOrderByCreateTimeDesc(String businessType, Integer status);
}