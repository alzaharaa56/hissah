package com.hissah.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@AllArgsConstructor
public class BidDocumentDownloadDTO {
    private final Path path;
    private final String fileName;
    private final String contentType;
    private final Long fileSize;
}
