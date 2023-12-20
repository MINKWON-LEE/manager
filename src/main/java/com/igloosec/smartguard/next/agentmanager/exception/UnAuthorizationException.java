package com.igloosec.smartguard.next.agentmanager.exception;

public class UnAuthorizationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnAuthorizationException(String message) {
        super(message);
    }
}
