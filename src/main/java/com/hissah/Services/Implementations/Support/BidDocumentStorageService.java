package com.hissah.Services.Implementations.Support;

import com.hissah.DTO.Request.BidRequestDTO;
import com.hissah.DTO.Response.BidDocumentDownloadDTO;
import com.hissah.Entities.Bid;
import com.hissah.Entities.BidDocument;
import com.hissah.Exceptions.FileStorageException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.BidDocumentRepository;
import com.hissah.Utilities.FileNameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BidDocumentStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final int MAX_DOCUMENTS = 5;

    private final BidDocumentRepository bidDocumentRepository;

    @Qualifier("uploadRootPath")
    private final Path uploadRootPath;

    public void store(Bid bid, BidRequestDTO request) {
        List<MultipartFile> files = request.getDocuments() == null
                ? List.of()
                : request.getDocuments();

        if (files.isEmpty()) {
            return;
        }

        if (request.getDocumentTypes() == null
                || request.getDocumentTypes().size() != files.size()) {
            throw new FileStorageException(
                    "Each uploaded document must have a matching document type."
            );
        }

        long existing = bidDocumentRepository.countByBidIdAndActiveTrue(bid.getId());
        if (existing + files.size() > MAX_DOCUMENTS) {
            throw new FileStorageException(
                    "A bid can contain a maximum of " + MAX_DOCUMENTS + " documents."
            );
        }

        Path bidDirectory = safeBidDirectory(bid.getId());
        List<Path> createdFiles = new ArrayList<>();

        try {
            Files.createDirectories(bidDirectory);

            for (int index = 0; index < files.size(); index++) {
                MultipartFile file = files.get(index);
                validateFile(file);

                String originalName = StringUtils.cleanPath(
                        file.getOriginalFilename() == null
                                ? "document"
                                : file.getOriginalFilename()
                );
                String storedName = FileNameGenerator.generate(originalName);
                Path target = FileNameGenerator.resolveSafePath(
                        bidDirectory,
                        storedName
                );

                try (InputStream input = file.getInputStream()) {
                    Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
                }
                createdFiles.add(target);

                BidDocument document = new BidDocument();
                document.setFileName(originalName);
                document.setStoredFileName(storedName);
                document.setFilePath(
                        uploadRootPath.relativize(target)
                                .toString()
                                .replace("\\", "/")
                );
                document.setContentType(file.getContentType());
                document.setFileSize(file.getSize());
                document.setDocumentType(request.getDocumentTypes().get(index));
                document.setBid(bid);

                bidDocumentRepository.save(document);
                bid.addDocument(document);
            }
        } catch (IOException exception) {
            createdFiles.forEach(this::deleteQuietly);
            throw new FileStorageException(
                    "Failed to store one or more bid documents.",
                    exception
            );
        } catch (RuntimeException exception) {
            createdFiles.forEach(this::deleteQuietly);
            throw exception;
        }
    }

    public BidDocumentDownloadDTO prepareDownload(Long documentId) {
        BidDocument document = bidDocumentRepository
                .findByIdAndActiveTrue(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bid document not found with id: " + documentId
                ));

        Path storedPath = uploadRootPath
                .resolve(document.getFilePath())
                .normalize();

        if (!storedPath.startsWith(uploadRootPath.normalize())) {
            throw new FileStorageException("Invalid bid document path.");
        }
        if (!Files.isRegularFile(storedPath)) {
            throw new ResourceNotFoundException(
                    "The stored bid document file is missing."
            );
        }

        return new BidDocumentDownloadDTO(
                storedPath,
                document.getFileName(),
                document.getContentType(),
                document.getFileSize()
        );
    }

    public BidDocument getDocument(Long documentId) {
        return bidDocumentRepository.findByIdAndActiveTrue(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bid document not found with id: " + documentId
                ));
    }

    private Path safeBidDirectory(Long bidId) {
        Path directory = uploadRootPath
                .resolve("bids")
                .resolve(String.valueOf(bidId))
                .normalize();

        if (!directory.startsWith(uploadRootPath.normalize())) {
            throw new FileStorageException("Invalid bid document directory.");
        }
        return directory;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("An uploaded bid document is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException(
                    "Each bid document must not exceed 5 MB."
            );
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Keep the original storage exception.
        }
    }
}
