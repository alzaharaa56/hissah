package com.hissah.Configurations;

import com.hissah.Exceptions.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Creates the protected local upload directory used by the MVP.
 * Only file metadata should be stored in MySQL.
 */
@Configuration
public class FileStorageConfig {

    @Value("${app.file-storage.upload-dir:uploads}")
    private String uploadDirectory;

    @Bean(name = "uploadRootPath")
    public Path uploadRootPath() {
        Path root = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
            return root;
        } catch (IOException exception) {
            throw new FileStorageException(
                    "Unable to create the upload directory: " + root,
                    exception
            );
        }
    }
}
