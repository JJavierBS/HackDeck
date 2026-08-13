package com.cyberdeck.adapter.rest.security;

import com.cyberdeck.application.port.out.AccessTokenPort;
import com.cyberdeck.domain.model.ParticipantSession;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public final class ParticipantSessionArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AccessTokenPort accessTokenPort;

    public ParticipantSessionArgumentResolver(AccessTokenPort accessTokenPort) {
        this.accessTokenPort = accessTokenPort;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return ParticipantSession.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        String header = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new UnauthenticatedException("Falta la cabecera Authorization: Bearer <token>");
        }
        return accessTokenPort.verify(header.substring(BEARER_PREFIX.length()))
                .orElseThrow(() -> new UnauthenticatedException("Token invalido o caducado"));
    }
}
