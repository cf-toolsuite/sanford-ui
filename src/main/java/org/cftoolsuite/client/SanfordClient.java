package org.cftoolsuite.client;

import java.util.List;

import org.cftoolsuite.domain.FileMetadata;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "document-service", url = "${document.service.url}")
public interface SanfordClient {

    @PostMapping(value = "/api/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<FileMetadata> uploadFile(@RequestPart("fileName") MultipartFile file);

    @GetMapping(value = "/api/files", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<FileMetadata>> getFileMetadata(@RequestParam(value = "fileName", required = false) String fileName);

    @GetMapping(value = "/api/files/search", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<FileMetadata>> search(@RequestParam("q") String query);

    @GetMapping(value = "/api/files/summarize/{fileName}", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> summarize(@PathVariable String fileName);

    @GetMapping("/api/files/download/{fileName}")
    ResponseEntity<Resource> downloadFile(@PathVariable String fileName);

    @DeleteMapping(value = "/api/files/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> deleteFile(@PathVariable String fileName);
}
