package com.kzhastkou.accountingonboarding.client.service;

import com.kzhastkou.accountingonboarding.client.entity.Client;
import com.kzhastkou.accountingonboarding.client.repository.ClientRepository;
import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public Client createFromQuestionnaire(Questionnaire questionnaire) {
        Objects.requireNonNull(questionnaire, "Questionnaire must not be null");

        if (questionnaire.getInvitation().getStatus() != InvitationStatus.APPROVED) {
            throw new BadRequestException(
                    "Client can only be created from an approved questionnaire."
            );
        }

        if (clientRepository.existsBySourceQuestionnaireId(questionnaire.getId())) {
            throw new BadRequestException(
                    "Client has already been created from this questionnaire."
            );
        }

        Client client = new Client(questionnaire);
        return clientRepository.saveAndFlush(client);
    }
}
