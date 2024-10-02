package org.cftoolsuite.client;

import java.util.List;

import org.cftoolsuite.domain.FileMetadata;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "document-service", url = "${document.service.url}")
public interface SanfordClient {

    @PostMapping("/api/files/upload")
    ResponseEntity<FileMetadata> uploadFile(@RequestParam("fileName") MultipartFile file);

    @GetMapping("/api/files")
    ResponseEntity<List<FileMetadata>> getFileMetadata(@RequestParam(value = "fileName", required = false) String fileName);

    @GetMapping("/api/files/search")
    ResponseEntity<List<FileMetadata>> search(@RequestParam("q") String query);

    @GetMapping("/api/files/summarize/{fileName}")
    ResponseEntity<String> summarize(@PathVariable String fileName);

    @GetMapping("/api/files/download/{fileName}")
    ResponseEntity<Resource> downloadFile(@PathVariable String fileName);

    @DeleteMapping("/api/files/{fileName}")
    ResponseEntity<Void> deleteFile(@PathVariable String fileName);
}
