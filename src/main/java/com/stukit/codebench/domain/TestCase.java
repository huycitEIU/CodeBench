package com.stukit.codebench.domain;

import java.nio.file.Path;

/**
 * Record lưu thông tin Test Case.
 * Tự động sinh constructor, accessor (name(), inputPath()...), equals, hashCode, toString.
 */
public record TestCase(String name, Path inputPath, Path expectedOutputPath) {
}