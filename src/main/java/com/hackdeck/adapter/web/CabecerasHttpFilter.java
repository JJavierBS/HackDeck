package com.hackdeck.adapter.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

/**
 * La pagina se escribe en espanol: sin fijar el idioma de la respuesta, el
 * servidor anuncia el de la JVM (en-US) y contradice al lang del HTML.
 * HSTS solo tiene sentido sobre HTTPS; en el aula se sirve por HTTP y ahi no
 * se manda.
 */
@Component
public class CabecerasHttpFilter extends OncePerRequestFilter {

    static final String HSTS = "max-age=63072000; includeSubDomains";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setLocale(Locale.forLanguageTag("es"));
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", HSTS);
        }
        chain.doFilter(request, response);
    }
}
