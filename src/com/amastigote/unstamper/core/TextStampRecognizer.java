/*
  AUTH | hwding
  DATE | Mar 08 2019
  DESC | textual watermark remover for PDF files
  MAIL | m@amastigote.com
  GITH | github.com/hwding
 */
package com.amastigote.unstamper.core;

import org.apache.pdfbox.pdmodel.font.PDCIDFontType0;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;

class TextStampRecognizer {

    private static boolean recognizeWithFont(
            @NotNull String[] keywords,
            @NotNull byte[] inputText,
            @NotNull Set<PDFont> pdFonts,
            @NotNull boolean useStrict) {
        for (PDFont f : pdFonts) {
            if (Objects.isNull(f)) {
                continue;
            }

            /* do not encode unsupported font */
            if ((f instanceof PDType0Font && ((PDType0Font) f).getDescendantFont() instanceof PDCIDFontType0)
                    || f instanceof PDType3Font) {
                continue;
            }

            StringBuilder builder = new StringBuilder();
            InputStream in = new ByteArrayInputStream(inputText);
            try {
                while (in.available() > 0) {
                    // decode a character
                    int code = f.readCode(in);
                    String unicode = f.toUnicode(code);
                    builder.append(unicode == null ? "" : unicode);
                }
            } catch (Exception ignored) {
                builder = new StringBuilder(generateByteString(inputText));
            }
            for (String k : keywords) {
                if (checkDuplicate(builder.toString(), k, useStrict)) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean recognizePlain(
            @NotNull String[] keywords,
            @NotNull byte[] inputText,
            @NotNull boolean useStrict
    ) {
        for (String k : keywords) {
            if (checkDuplicate(new String(inputText), k, useStrict)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkDuplicate(
            @NotNull String input,
            @NotNull String keyword,
            @NotNull boolean useStrict) {
        if (useStrict) {
            return input.equals(keyword);
        } else {
            return input.contains(keyword);
        }
    }

    static boolean recognize(@NotNull String[] keywords,
                             @NotNull byte[] inputText,
                             @NotNull Set<PDFont> pdFonts,
                             @NotNull boolean useStrict) {
        return recognizePlain(keywords, inputText, useStrict) ||
                recognizeWithFont(keywords, inputText, pdFonts, useStrict);
    }

    private static String generateByteString(@NotNull byte[] bytes) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(b);
        }
        return stringBuilder.toString();
    }
}
