package com.hackdeck.adapter.rest.security;

import com.hackdeck.adapter.rest.dto.ApiErrorResponse;
import com.hackdeck.application.limits.LimitsProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Tope al tamano del cuerpo de las peticiones de la API.
 *
 * Ni Tomcat ni Spring lo ponen por su cuenta para JSON: max-http-form-post-size
 * solo mira los formularios y max-swallow-size solo el cuerpo que se descarta
 * tras un error. Sin esto, unas pocas peticiones anonimas a /join con decenas
 * de megas cada una bastan para agotar el heap del servidor.
 *
 * No basta con mirar Content-Length: quien ataca manda el cuerpo troceado
 * (Transfer-Encoding: chunked) y no anuncia tamano ninguno. Por eso el cuerpo
 * va ademas envuelto en un flujo que corta en cuanto se pasa de la cuenta.
 */
@Component
@Order(20)
public class RequestSizeLimitFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api/";

    private final long maxBytes;
    private final ObjectMapper objectMapper;

    public RequestSizeLimitFilter(LimitsProperties properties, ObjectMapper objectMapper) {
        this.maxBytes = properties.maxRequestBody().toBytes();
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(API_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Con Content-Length se rechaza sin leer un solo byte del cuerpo.
        if (request.getContentLengthLong() > maxBytes) {
            reject(response);
            return;
        }
        try {
            chain.doFilter(new LimitedBodyRequest(request, maxBytes), response);
        } catch (PayloadTooLargeException e) {
            // Solo llega aqui si alguien lee el cuerpo fuera del controlador;
            // lo normal es que lo traduzca el manejador de errores.
            if (!response.isCommitted()) {
                reject(response);
            }
        }
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                new ApiErrorResponse("cuerpo_demasiado_grande", "El cuerpo de la peticion es demasiado grande")));
    }

    private static final class LimitedBodyRequest extends HttpServletRequestWrapper {

        private final long maxBytes;
        private ServletInputStream stream;
        private BufferedReader reader;

        private LimitedBodyRequest(HttpServletRequest request, long maxBytes) {
            super(request);
            this.maxBytes = maxBytes;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (stream == null) {
                stream = new LimitedStream(super.getInputStream(), maxBytes);
            }
            return stream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (reader == null) {
                String encoding = getCharacterEncoding();
                Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
                reader = new BufferedReader(new InputStreamReader(getInputStream(), charset));
            }
            return reader;
        }
    }

    private static final class LimitedStream extends ServletInputStream {

        private final ServletInputStream delegate;
        private final long maxBytes;
        private long read;

        private LimitedStream(ServletInputStream delegate, long maxBytes) {
            this.delegate = delegate;
            this.maxBytes = maxBytes;
        }

        @Override
        public int read() throws IOException {
            int leido = delegate.read();
            if (leido != -1) {
                count(1);
            }
            return leido;
        }

        @Override
        public int read(byte[] destino, int desde, int cuantos) throws IOException {
            int leidos = delegate.read(destino, desde, cuantos);
            if (leidos > 0) {
                count(leidos);
            }
            return leidos;
        }

        private void count(int leidos) {
            read += leidos;
            if (read > maxBytes) {
                throw new PayloadTooLargeException(maxBytes);
            }
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            delegate.setReadListener(listener);
        }
    }
}
