package org.cftoolsuite.client;

import org.cftoolsuite.domain.FileMetadata;
import org.cftoolsuite.domain.chat.AudioResponse;
import org.cftoolsuite.domain.chat.Inquiry;
import org.cftoolsuite.domain.crawl.CrawlRequest;
import org.cftoolsuite.domain.crawl.CrawlResponse;
import org.cftoolsuite.domain.fetch.FetchRequest;
import org.cftoolsuite.domain.fetch.FetchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "sanford-client", url = "${document.service.url}")
public interface SanfordClient {

    @PostMapping(value = "/api/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<FileMetadata> uploadFile(@RequestPart("fileName") MultipartFile file);

    @PostMapping("/api/crawl")
    public ResponseEntity<CrawlResponse> startCrawl(@RequestBody CrawlRequest crawlRequest);

    @PostMapping("/api/fetch")
    public ResponseEntity<FetchResponse> fetchUrls(@RequestBody FetchRequest request);

    @PostMapping(value = "/api/converse", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<AudioResponse> converse(@RequestBody byte[] audioBytes);

    @PostMapping("/api/chat")
    public ResponseEntity<String> chat(@RequestBody Inquiry inquiry);

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
