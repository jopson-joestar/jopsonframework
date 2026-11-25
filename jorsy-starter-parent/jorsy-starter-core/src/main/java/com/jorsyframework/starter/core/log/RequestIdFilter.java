package com.jorsyframework.starter.core.log;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

public class RequestIdFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";
    private static final String REQUEST_STATE = RequestIdFilter.class.getName() + ".state";
    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final Logger LOG = LoggerFactory.getLogger(RequestIdFilter.class);

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        RequestState state = (RequestState) request.getAttribute(REQUEST_STATE);
        if (state == null) {
            String supplied = request.getHeader(HEADER);
            String id = supplied != null && VALID_ID.matcher(supplied).matches() ? supplied : UUID.randomUUID().toString();
            state = new RequestState(id, System.nanoTime());
            request.setAttribute(REQUEST_STATE, state);
        }
        response.setHeader(HEADER, state.id());
        String previous = MDC.get(MDC_KEY);
        MDC.put(MDC_KEY, state.id());
        try {
            chain.doFilter(request, response);
        } finally {
            try {
                if (!request.isAsyncStarted()) {
                    Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                    LOG.info("requestId={} method={} path={} status={} elapsedMs={}", state.id(), request.getMethod(),
                            route == null ? request.getRequestURI() : route, response.getStatus(),
                            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - state.startNanos()));
                }
            } finally {
                restoreRequestId(previous);
            }
        }
    }

    static void restoreRequestId(String id) {
        if (id == null) {
            MDC.remove(MDC_KEY);
        } else {
            MDC.put(MDC_KEY, id);
        }
    }

    private record RequestState(String id, long startNanos) {}
}
