package com.hissah.Utils;

import com.hissah.Exceptions.FileStorageException;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Safe file-name and path helpers for protected local uploads. */
public final class FileNameGenerator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg");

    private FileNameGenerator() {
    }

    public static String generate(String originalFilename) {
        String cleanName = StringUtils.cleanPath(
                originalFilename == null ? "file" : originalFilename
        );

        if (cleanName.contains("..")) {
            throw new FileStorageException("The file name contains an invalid path sequence.");
        }

        String extension = extractExtension(cleanName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("Only PDF, PNG, JPG, and JPEG files are allowed.");
        }

        return UUID.randomUUID() + "." + extension;
    }

    public static Path resolveSafePath(Path uploadRoot, String storedFilename) {
        Path resolved = uploadRoot.resolve(storedFilename).normalize();
        if (!resolved.startsWith(uploadRoot.normalize())) {
            throw new FileStorageException("The requested file path is outside the upload directory.");
        }
        return resolved;
    }

    private static String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new FileStorageException("The uploaded file must have a valid extension.");
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
