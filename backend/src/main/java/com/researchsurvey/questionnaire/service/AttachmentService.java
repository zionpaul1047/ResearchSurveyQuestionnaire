package com.researchsurvey.questionnaire.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.researchsurvey.questionnaire.dto.AttachmentResponse;
import com.researchsurvey.questionnaire.entity.Attachment;
import com.researchsurvey.questionnaire.exception.ApiException;
import com.researchsurvey.questionnaire.repository.AttachmentRepository;

@Service
public class AttachmentService {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final long MAX_FILES_PER_CATEGORY = 10;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_CATEGORIES = Set.of("PURCHASE_EVIDENCE", "PRODUCT_PHOTO");
    private static final Map<String, String> EXTENSIONS = Map.of("image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");

    private final SurveySubmissionService submissionService;
    private final AttachmentRepository attachmentRepository;
    private final Path uploadRoot;

    public AttachmentService(SurveySubmissionService submissionService, AttachmentRepository attachmentRepository, @Value("${survey.upload-directory}") String uploadDirectory) {
        this.submissionService = submissionService;
        this.attachmentRepository = attachmentRepository;
        this.uploadRoot = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @Transactional
    public AttachmentResponse upload(UUID submissionId, String category, MultipartFile file) {
        var submission = submissionService.find(submissionId);
        submissionService.ensureDraft(submission);
        validate(file);
        if (!ALLOWED_CATEGORIES.contains(category)) throw new ApiException(HttpStatus.BAD_REQUEST, "허용되지 않는 첨부파일 분류입니다.");
        if (attachmentRepository.countBySubmission_IdAndCategory(submissionId, category) >= MAX_FILES_PER_CATEGORY) throw new ApiException(HttpStatus.BAD_REQUEST, "첨부파일은 분류별 최대 10개까지 업로드할 수 있습니다.");

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        if (originalName.contains("..")) throw new ApiException(HttpStatus.BAD_REQUEST, "허용되지 않는 파일명입니다.");
        if (originalName.length() > 255) throw new ApiException(HttpStatus.BAD_REQUEST, "파일명은 255자 이하만 사용할 수 있습니다.");
        String contentType = file.getContentType();
        String storedName = UUID.randomUUID() + EXTENSIONS.get(contentType);
        Path directory = uploadRoot.resolve(submissionId.toString()).normalize();
        Path target = directory.resolve(storedName).normalize();
        if (!target.startsWith(uploadRoot)) throw new ApiException(HttpStatus.BAD_REQUEST, "허용되지 않는 저장 경로입니다.");

        try {
            byte[] bytes = file.getBytes();
            validateSignature(contentType, bytes);
            Files.createDirectories(directory);
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            Attachment attachment = new Attachment(submission, category, originalName, storedName, uploadRoot.relativize(target).toString(), contentType, bytes.length, hash, Instant.now());
            return AttachmentResponse.from(attachmentRepository.saveAndFlush(attachment));
        } catch (IOException | NoSuchAlgorithmException exception) {
            try { Files.deleteIfExists(target); } catch (IOException ignored) {}
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다.");
        } catch (ApiException exception) {
            try { Files.deleteIfExists(target); } catch (IOException ignored) {}
            throw exception;
        } catch (RuntimeException exception) {
            try { Files.deleteIfExists(target); } catch (IOException ignored) {}
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 정보를 저장하지 못했습니다.");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "업로드할 파일을 선택해 주세요.");
        if (file.getSize() > MAX_FILE_SIZE) throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "파일은 10MB 이하만 업로드할 수 있습니다.");
        if (!ALLOWED_TYPES.contains(file.getContentType())) throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "JPG, PNG, WEBP 이미지만 업로드할 수 있습니다.");
    }

    private void validateSignature(String contentType, byte[] bytes) {
        boolean valid = switch (contentType) {
            case "image/jpeg" -> bytes.length >= 3 && unsigned(bytes[0]) == 0xFF && unsigned(bytes[1]) == 0xD8 && unsigned(bytes[2]) == 0xFF;
            case "image/png" -> bytes.length >= 8
                    && unsigned(bytes[0]) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                    && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A;
            case "image/webp" -> bytes.length >= 12
                    && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                    && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
            default -> false;
        };
        if (!valid) throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "파일 내용이 JPG, PNG 또는 WEBP 이미지 형식과 일치하지 않습니다.");
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }
}
