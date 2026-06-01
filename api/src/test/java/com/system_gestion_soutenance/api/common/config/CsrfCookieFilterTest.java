package com.system_gestion_soutenance.api.common.config;

import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.csrf.CsrfToken;

@ExtendWith(MockitoExtension.class)
class CsrfCookieFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private CsrfToken csrfToken;

    private final CsrfCookieFilter filter = new CsrfCookieFilter();

    @Test
    void whenTokenPresent_forcesTokenGenerationAndProceeds() throws Exception {
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);

        filter.doFilterInternal(request, response, filterChain);

        verify(csrfToken).getToken();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void whenTokenNull_proceedsWithoutError() throws Exception {
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
