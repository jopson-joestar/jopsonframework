package com.jorsyframework.starter.core.mybatis.log;

import java.util.Locale;
import java.util.regex.Pattern;

import com.jorsyframework.starter.core.log.RequestIdFilter;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.slf4j.MDC;

public final class JorsySqlFormat implements MessageFormattingStrategy {
    private static final Pattern OPERATION = Pattern.compile("^(SELECT|INSERT|UPDATE|DELETE)\\b", Pattern.CASE_INSENSITIVE);
    // Only identifiers, placeholders and simple SQL punctuation. Quotes, comments, numeric literals and multiple statements fall back to the summary.
    private static final Pattern TEMPLATE = Pattern.compile("(?:[a-zA-Z_][a-zA-Z_0-9]*+|[\\s.,()*=<>?])++");
    private static final Pattern LITERAL = Pattern.compile("\\b(?:NULL|TRUE|FALSE)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category,
                                String prepared, String sql, String url) {
        String template = prepared == null ? "" : prepared.strip();
        var operation = OPERATION.matcher(template);
        String type = operation.find() ? operation.group(1).toUpperCase(Locale.ROOT) : "OTHER";
        String requestId = MDC.get(RequestIdFilter.MDC_KEY);
        String summary = "requestId=" + (requestId == null ? "-" : requestId) + " sqlType=" + type + " elapsedMs=" + elapsed;
        if (type.equals("OTHER") || template.length() > 4096 || !template.contains("?")
                || !TEMPLATE.matcher(template).matches() || LITERAL.matcher(template).find()) {
            return summary;
        }
        return summary + " sql=" + WHITESPACE.matcher(template).replaceAll(" ");
    }
}
