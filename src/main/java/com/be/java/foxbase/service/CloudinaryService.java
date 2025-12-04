package com.be.java.foxbase.service;

import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.exception.FileSizeExceededException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // Upload ảnh bìa
    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_FILE);
        }

        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());

        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "foxbase/book-cover",
                        "public_id", baseName,
                        "use_filename", true,
                        "unique_filename", false,
                        "overwrite", true
                )
        );
    }

    // Upload file PDF
    public Map<String, Object> uploadPdf(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.MISSING_FILE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());

        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "folder", "foxbase/books",
                        "public_id", baseName,
                        "format", "pdf",
                        "use_filename", true,
                        "unique_filename", false,
                        "overwrite", true
                )
        );
    }
}
