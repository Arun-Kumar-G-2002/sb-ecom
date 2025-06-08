package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    /**
     * This method uploads an image to the specified path.
     * It generates a unique filename using UUID and saves the image file.
     *
     * @param path  The directory path where the image will be uploaded.
     * @param image The MultipartFile representing the image to be uploaded.
     * @return The name of the uploaded file.
     * @throws IOException If an I/O error occurs during file upload.
     */

    @Override
    public String uploadImage(String path, MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename();
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFilename.substring(originalFilename.lastIndexOf(".")));
        String filePath = path + File.separator + fileName;

        File folder = new File(path);
        if(!folder.exists()) {
            folder.mkdir();
        }

        Files.copy(image.getInputStream(), Paths.get(filePath));
        return fileName;
    }
}
