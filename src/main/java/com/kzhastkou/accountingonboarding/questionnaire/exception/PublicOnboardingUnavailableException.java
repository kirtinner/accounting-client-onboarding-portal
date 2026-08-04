package com.kzhastkou.accountingonboarding.questionnaire.exception;

public class PublicOnboardingUnavailableException extends RuntimeException {

    private final String code;

    public PublicOnboardingUnavailableException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
