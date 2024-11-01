package com.tear.upgrade.t1tot2upgrade.utils;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class FileHelper {

    private FileHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static String readFromFile(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8);
    }
}
