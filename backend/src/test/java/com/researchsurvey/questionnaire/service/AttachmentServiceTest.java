package com.researchsurvey.questionnaire.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.researchsurvey.questionnaire.entity.SurveySubmission;
import com.researchsurvey.questionnaire.exception.ApiException;
import com.researchsurvey.questionnaire.repository.AttachmentRepository;

class AttachmentServiceTest {
    @TempDir
    Path uploadDirectory;

    @Test
    void rejectsFileWhoseContentDoesNotMatchDeclaredImageType() {
        var submissionService = mock(SurveySubmissionService.class);
        var attachmentRepository = mock(AttachmentRepository.class);
        UUID submissionId = UUID.randomUUID();
        when(submissionService.find(submissionId)).thenReturn(new SurveySubmission(submissionId, "RSQ-TEST", Instant.now()));
        var service = new AttachmentService(submissionService, attachmentRepository, uploadDirectory.toString());
        var fakeImage = new MockMultipartFile("file", "evidence.png", "image/png", "not-an-image".getBytes());

        assertThatThrownBy(() -> service.upload(submissionId, "PRODUCT_PHOTO", fakeImage))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("이미지 형식과 일치하지 않습니다");
    }

    @Test
    void rejectsLegacyOrUnknownAttachmentCategory() {
        var submissionService = mock(SurveySubmissionService.class);
        var attachmentRepository = mock(AttachmentRepository.class);
        UUID submissionId = UUID.randomUUID();
        when(submissionService.find(submissionId)).thenReturn(new SurveySubmission(submissionId, "RSQ-TEST", Instant.now()));
        var service = new AttachmentService(submissionService, attachmentRepository, uploadDirectory.toString());
        byte[] pngHeader = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        var image = new MockMultipartFile("file", "evidence.png", "image/png", pngHeader);

        assertThatThrownBy(() -> service.upload(submissionId, "PRODUCT_EVIDENCE", image))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("허용되지 않는 첨부파일 분류");
    }

    @Test
    void rejectsMoreThanTenFilesInOneCategory() {
        var submissionService = mock(SurveySubmissionService.class);
        var attachmentRepository = mock(AttachmentRepository.class);
        UUID submissionId = UUID.randomUUID();
        when(submissionService.find(submissionId)).thenReturn(new SurveySubmission(submissionId, "RSQ-TEST", Instant.now()));
        when(attachmentRepository.countBySubmission_IdAndCategory(submissionId, "PRODUCT_PHOTO")).thenReturn(10L);
        var service = new AttachmentService(submissionService, attachmentRepository, uploadDirectory.toString());
        byte[] pngHeader = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        var image = new MockMultipartFile("file", "evidence.png", "image/png", pngHeader);

        assertThatThrownBy(() -> service.upload(submissionId, "PRODUCT_PHOTO", image))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("최대 10개");
    }
}
