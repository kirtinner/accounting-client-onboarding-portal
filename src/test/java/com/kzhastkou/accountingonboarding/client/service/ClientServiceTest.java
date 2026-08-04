package com.kzhastkou.accountingonboarding.client.service;

import com.kzhastkou.accountingonboarding.client.entity.Client;
import com.kzhastkou.accountingonboarding.client.repository.ClientRepository;
import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import com.kzhastkou.accountingonboarding.questionnaire.dto.PublicQuestionnaireRequest;
import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService service;

    @Test
    void createFromApprovedQuestionnaireSucceeds() {
        Questionnaire questionnaire = approvedQuestionnaire();
        when(clientRepository.existsBySourceQuestionnaireId(1L)).thenReturn(false);
        when(clientRepository.saveAndFlush(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Client client = service.createFromQuestionnaire(questionnaire);

        assertEquals(ClientType.INDIVIDUAL, client.getClientType());
        assertSame(questionnaire, client.getSourceQuestionnaire());
        verify(clientRepository).existsBySourceQuestionnaireId(1L);
        verify(clientRepository).saveAndFlush(any(Client.class));
    }

    @Test
    void duplicateDetectedByPreCheckFails() {
        Questionnaire questionnaire = approvedQuestionnaire();
        when(clientRepository.existsBySourceQuestionnaireId(1L)).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.createFromQuestionnaire(questionnaire)
        );

        assertEquals("Client has already been created from this questionnaire.", exception.getMessage());
    }

    @Test
    void duplicateDetectedByUniqueConstraintRaceFailsAsBadRequest() {
        Questionnaire questionnaire = approvedQuestionnaire();
        when(clientRepository.existsBySourceQuestionnaireId(1L)).thenReturn(false);
        when(clientRepository.saveAndFlush(any(Client.class))).thenThrow(
                dataIntegrityViolation("clients_source_questionnaire_id_key")
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.createFromQuestionnaire(questionnaire)
        );

        assertEquals("Client has already been created from this questionnaire.", exception.getMessage());
    }

    @Test
    void unrelatedDataIntegrityViolationPropagatesUnchanged() {
        Questionnaire questionnaire = approvedQuestionnaire();
        DataIntegrityViolationException dataIntegrityViolationException =
                dataIntegrityViolation("some_other_constraint");
        when(clientRepository.existsBySourceQuestionnaireId(1L)).thenReturn(false);
        when(clientRepository.saveAndFlush(any(Client.class))).thenThrow(dataIntegrityViolationException);

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> service.createFromQuestionnaire(questionnaire)
        );

        assertSame(dataIntegrityViolationException, thrown);
    }

    private DataIntegrityViolationException dataIntegrityViolation(String constraintName) {
        return new DataIntegrityViolationException(
                "constraint violation",
                new ConstraintViolationException(
                        "constraint violation",
                        new SQLException("constraint violation"),
                        constraintName
                )
        );
    }

    private Questionnaire approvedQuestionnaire() {
        Instant now = Instant.now();
        OnboardingInvitation invitation = new OnboardingInvitation(
                UUID.randomUUID(),
                "Alex Smith",
                "alex@example.com",
                ClientType.INDIVIDUAL,
                now,
                null
        );
        invitation.markSent(now, Duration.ofDays(10));
        invitation.markSubmitted(now);
        invitation.markApproved(now);

        Questionnaire questionnaire = new Questionnaire(invitation, request(), now);
        ReflectionTestUtils.setField(questionnaire, "id", 1L);
        questionnaire.submit(now);
        return questionnaire;
    }

    private PublicQuestionnaireRequest request() {
        return new PublicQuestionnaireRequest(
                "Alex",
                null,
                "Smith",
                LocalDate.of(1990, 1, 1),
                "alex.client@example.com",
                "0400000000",
                "1 Collins Street",
                null,
                "Melbourne",
                "VIC",
                "3000",
                "Australia",
                true
        );
    }
}
