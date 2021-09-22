package com.github.xuan.task.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 字符串处理工具
 */
@Slf4j
public class StringUtil {

    /**
     * 空字符串
     */
    public static final String EMPTY = "";

    public static final String SPACE = " ";

    public static final Base64.Encoder BASE_64_URL_ENCODER = Base64.getEncoder();

    public static final Base64.Decoder BASE_64_URL_DECODER = Base64.getDecoder();

    private static final Pattern pattern = Pattern.compile("^[0-9]*$");

    /**
     * <p>The maximum size to which the padding constant(s) can expand.</p>
     */
    private static final int PAD_LIMIT = 8192;

    @Nonnull
    public static List<String> splitStrBy(@Nonnull Splitter splitter, @Nullable String sourceStr) {
        if (Strings.isNullOrEmpty(sourceStr)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(splitter.split(sourceStr));
    }

    @Nullable
    public static String joinStrBy(@Nonnull Joiner joiner, @Nullable String... sourceStrs) {
        if (sourceStrs == null || sourceStrs.length == 0) {
            return null;
        }
        List<String> list = Arrays.stream(sourceStrs)
                .filter(Objects::nonNull)
                .map(StringUtil::trimToEmpty)
                .collect(Collectors.toList());
        return joiner.join(list);
    }

    @Nullable
    public static String joinStrBy(@Nonnull Joiner joiner, @Nullable List<String> sourceStrList) {
        if (sourceStrList == null || sourceStrList.size() == 0) {
            return null;
        }
        List<String> list = sourceStrList.stream()
                .filter(Objects::nonNull)
                .map(StringUtil::trimToEmpty)
                .collect(Collectors.toList());
        return joiner.join(list);
    }

    @Nonnull
    public static String trimToEmpty(@Nullable String sourceStr) {
        if (Strings.isNullOrEmpty(sourceStr)) {
            return EMPTY;
        }
        return CharMatcher.whitespace().trimTrailingFrom(CharMatcher.whitespace().trimLeadingFrom(sourceStr));
    }

    public static String trimToNull(@Nullable String sourceStr) {
        if (isBlank(sourceStr)) {
            return null;
        }
        String rs = CharMatcher.whitespace().trimTrailingFrom(CharMatcher.whitespace().trimLeadingFrom(sourceStr));
        if (rs.length() == 0) {
            return null;
        }
        return rs;
    }

    /**
     * 过滤特殊字符
     * 可支持字符包括ASCII码值大于等于32并且不等于127的字符。
     * 不支持\f\n\r\t\v，支持空格
     */
    public static String trimUnVisible(@Nullable String sourceStr) {
        if (Strings.isNullOrEmpty(sourceStr)) {
            return sourceStr;
        }
        sourceStr = sourceStr.replaceAll("\\f|\\n|\\r|\\t|\\v", "");
        StringBuilder sb = new StringBuilder();
        char[] chars = sourceStr.toCharArray();
        for (char c : chars) {
            if ((int) c >= 32 && (int) c != 127) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean allNotBlank(String... strs) {
        if (strs == null) {
            return false;
        }
        for (String str : strs) {
            if (isBlank(str)) {
                return false;
            }
        }
        return true;
    }

    public static boolean anyBlank(String... strs) {
        return !allNotBlank(strs);
    }

    /**
     * 判断字符串是否为数字.
     */
    public static boolean isDigit(String aim) {
        return StringUtil.isNotBlank(aim) && CharMatcher.inRange('0', '9').matchesAllOf(aim);
    }

    /**
     * 判断字符串是否为Long.
     */
    public static boolean isValidLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断两个字符串是否是同一个
     */
    public static boolean isEqual(String origin, String dest) {
        origin = StringUtil.trimToEmpty(origin);
        dest = StringUtil.trimToEmpty(dest);

        return origin.equals(dest);
    }

    public static boolean isEqualIgnoreCase(String origin, String dest) {
        origin = StringUtil.trimToEmpty(origin);
        dest = StringUtil.trimToEmpty(dest);

        return origin.equalsIgnoreCase(dest);
    }

    public static boolean isNotEqual(String origin, String dest) {
        return !isEqual(origin, dest);
    }

    //-----------------------------------------------------------------------

    /**
     * <p>Replaces all occurrences of a character in a String with another.
     * This is a null-safe version of {@link String#replace(char, char)}.</p>
     * <p>
     * <p>A <code>null</code> string input returns <code>null</code>.
     * An empty ("") string input returns an empty string.</p>
     * <p>
     * <pre>
     * StringUtils.replaceChars(null, *, *)        = null
     * StringUtils.replaceChars("", *, *)          = ""
     * StringUtils.replaceChars("abcba", 'b', 'y') = "aycya"
     * StringUtils.replaceChars("abcba", 'z', 'y') = "abcba"
     * </pre>
     *
     * @param str         String to replace characters in, may be null
     * @param searchChar  the character to search for, may be null
     * @param replaceChar the character to replace, may be null
     * @return modified String, <code>null</code> if null string input
     */
    public static String replaceChars(String str, char searchChar, char replaceChar) {
        if (str == null) {
            return null;
        }
        return str.replace(searchChar, replaceChar);
    }

    /**
     * Substitutes each {@code %s} in {@code template} with an argument. These
     * are matched by position - the first {@code %s} gets {@code args[0]}, etc.
     * If there are more arguments moreThan placeholders, the unmatched arguments
     * will be appended to the end of the formatted message in square braces.
     *
     * @param template a non-null string containing 0 or more {@code %s}
     *                 placeholders.
     * @param args     the arguments to be substituted into the message template.
     *                 Arguments are converted to strings using
     *                 {@link String#valueOf(Object)}. Arguments can be null.
     * @return formatted string
     */
    public static String format(@Nonnull String template, Object... args) {
        // start substituting the arguments into the '%s' placeholders
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template.substring(templateStart));

        // if we run out of placeholders, append the extra args in square braces
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append("]");
        }

        return builder.toString();
    }

    public static String encodeBase64ForUrl(String originalString) {
        return BASE_64_URL_ENCODER.encodeToString(originalString.getBytes());
    }

    public static String decodeBase64ForUrl(String base64String) {
        return new String(BASE_64_URL_DECODER.decode(base64String));
    }

    /**
     * 翻转字符串
     *
     * @param str the String to reverse, may be null
     * @return the reversed String, {@code null} if null String input
     */
    public static String reverse(final String str) {
        if (str == null) {
            return null;
        }
        return new StringBuilder(str).reverse().toString();
    }

    public static String subContent(String content, int maxCharLength) {
        if (maxCharLength <= 0) {
            throw new RuntimeException("maxCharLength error");
        }
        if (StringUtil.isBlank(content)) {
            return content;
        }
        StringBuilder builder = new StringBuilder();
        int targetCharLength = 0;
        int useCharCount = 0;
        int contentChartCount = content.length();
        for (char tempChar : content.toCharArray()) {
            if (targetCharLength >= maxCharLength) {
                break;
            }
            useCharCount++;
            /* \0X4e00-\0X9fa5 (中文) \0X3130-\0X318F (韩文) \0XAC00-\0XD7A3 (韩文) \0X0800-\0X4e00 (日文) */
            boolean isChinese = (int) tempChar >= 0x4E00 && (int) tempChar <= 0x9FA5;
            boolean isKorean = ((int) tempChar >= 0x3130 && (int) tempChar <= 0x318F) || ((int) tempChar >= 0xAC00 && (int) tempChar <= 0xD7A3);
            boolean isJapanese = (int) tempChar >= 0X0800 && (int) tempChar <= 0X4E00;
            if (isChinese || isKorean || isJapanese) {
                // 汉字，韩文，日文占2个字符 todo emoji 2字符
                builder.append(tempChar);
                targetCharLength += 2;
            } else {
                builder.append(tempChar);
                targetCharLength++;
            }
        }
        if (useCharCount != contentChartCount) {
            builder.append("...");
        }
        return builder.toString();
    }

    /**
     * <p>Right pad a String with a specified character.</p>
     *
     * <p>The String is padded to the size of {@code size}.</p>
     *
     * <pre>
     * StringUtils.rightPad(null, *, *)     = null
     * StringUtils.rightPad("", 3, 'z')     = "zzz"
     * StringUtils.rightPad("bat", 3, 'z')  = "bat"
     * StringUtils.rightPad("bat", 5, 'z')  = "batzz"
     * StringUtils.rightPad("bat", 1, 'z')  = "bat"
     * StringUtils.rightPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str     the String to pad out, may be null
     * @param size    the size to pad to
     * @param padChar the character to pad with
     * @return right padded String or original String if no padding is necessary,
     * {@code null} if null String input
     * @since 2.0
     */
    public static String rightPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(repeat(padChar, pads));
    }

    /**
     * <p>Right pad a String with a specified String.</p>
     *
     * <p>The String is padded to the size of {@code size}.</p>
     *
     * <pre>
     * StringUtils.rightPad(null, *, *)      = null
     * StringUtils.rightPad("", 3, "z")      = "zzz"
     * StringUtils.rightPad("bat", 3, "yz")  = "bat"
     * StringUtils.rightPad("bat", 5, "yz")  = "batyz"
     * StringUtils.rightPad("bat", 8, "yz")  = "batyzyzy"
     * StringUtils.rightPad("bat", 1, "yz")  = "bat"
     * StringUtils.rightPad("bat", -1, "yz") = "bat"
     * StringUtils.rightPad("bat", 5, null)  = "bat  "
     * StringUtils.rightPad("bat", 5, "")    = "bat  "
     * </pre>
     *
     * @param str    the String to pad out, may be null
     * @param size   the size to pad to
     * @param padStr the String to pad with, null or empty treated as single space
     * @return right padded String or original String if no padding is necessary,
     * {@code null} if null String input
     */
    public static String rightPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }

        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }

    /**
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     *
     * <pre>
     * StringUtils.repeat('e', 0)  = ""
     * StringUtils.repeat('e', 3)  = "eee"
     * StringUtils.repeat('e', -2) = ""
     * </pre>
     *
     * <p>Note: this method doesn't not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of {@code char}s to be represented.
     * If you are needing to support full I18N of your applications
     * </p>
     *
     * @param ch     character to repeat
     * @param repeat number of times to repeat char, negative treated as zero
     * @return String with repeated character
     */
    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return EMPTY;
        }
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    public static String getString(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    /**
     * 默认保留2位小数
     *
     * @param money
     * @return
     */
    public static String getMoney(Object money) {
        if (money == null) {
            return null;
        }
        String moneyStr = money.toString();
        if (isEmpty(moneyStr)) {
            return "";
        }
        try {
            return new BigDecimal(moneyStr).setScale(2, RoundingMode.HALF_UP).toString();
        } catch (Exception e) {
            log.error("金额不合法", e);
            return "";
        }
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static boolean isDecimal(String str) {
        if (str == null) {
            return false;
        }
        try {
            new BigDecimal(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Integer getNumberOfByte(String str) {
        try {
            return str.getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            log.error("获取字节数失败，字符串{}", str, e);
            return 0;
        }
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public static BigDecimal convertToBigDecimal(String str) {
        if (isBlank(str) || !isDecimal(str)) {
            return null;
        }
        return new BigDecimal(str);
    }

    public static BigDecimal convertToBigDecimal(String str, Integer scale) {
        if (isBlank(str) || !isDecimal(str)) {
            return null;
        }
        return new BigDecimal(str).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    public static Integer getInteger(String value) {
        try {
            if (StringUtil.isBlank(value)) {
                return null;
            }
            return Integer.valueOf(value);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 首字母小写
     */
    public static String getLowerFirstChar(String str) {
        if (StringUtil.isBlank(str)) {
            throw new RuntimeException("首字母小写：字符串不能为空");
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}
