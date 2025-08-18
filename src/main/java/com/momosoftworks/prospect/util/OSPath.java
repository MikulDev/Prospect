package com.momosoftworks.prospect.util;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public record OSPath(String path, String separator)
{
    public static OSPath from(Path path, String separator)
    {
        String nativeSeparator = FileSystems.getDefault().getSeparator();
        if (path == null)
        {   throw new IllegalArgumentException("Path cannot be null");
        }
        return new OSPath(path.toString().replace(nativeSeparator, separator), separator);
    }

    public OSPath resolve(String... other)
    {
        if (other == null || other.length == 0)
        {   throw new IllegalArgumentException("Other paths cannot be null or empty");
        }
        StringBuilder resolvedPath = new StringBuilder(this.path);
        for (String part : other) {
            if (part != null && !part.isEmpty()) {
                resolvedPath.append(separator).append(part);
            }
        }
        return new OSPath(resolvedPath.toString(), this.separator);
    }
}