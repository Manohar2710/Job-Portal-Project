package com.learning.common.util;

/**
 * Utility methods for masking sensitive data before writing to logs.
 *
 * <p>All methods are stateless and can be called directly as static helpers.
 * Keep this class free of Spring dependencies so it is usable in any module.</p>
 */
public final class LogMaskingUtils {

    private LogMaskingUtils() {
        // utility class — no instances
    }

    /**
     * Partially masks an email address so it is safe to include in log output.
     *
     * <p>Only the local-part characters after the first character are replaced
     * with asterisks.  The {@code @} sign and domain are preserved so the log
     * entry still identifies the account without exposing the full address.</p>
     *
     * <pre>
     *   manohar@example.com  →  m*****@example.com
     *   ab@example.com       →  a@example.com
     *   a@example.com        →  a@example.com   (too short, returned as-is)
     *   null / blank         →  "[REDACTED]"
     * </pre>
     *
     * @param email the raw email address; may be {@code null}
     * @return a masked representation safe for logging
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "[REDACTED]";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            // Single-character local-part or no '@' found — return as-is to avoid
            // producing a misleading masked string.
            return email;
        }
        return email.charAt(0) + "*".repeat(at - 1) + email.substring(at);
    }
}
