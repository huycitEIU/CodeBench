package com.stukit.codebench.domain;

import java.nio.file.Path;

public class TestCase {
    private final String name;
    private final Path inputPath;
    private final Path expectedOutputPath;

    public TestCase(String name, Path inputPath, Path expectedOutputPath) {
        this.name = name;
        this.inputPath = inputPath;
        this.expectedOutputPath = expectedOutputPath;
    }

    public String getName() {
        return name;
    }

    public Path getInputPath() {
        return inputPath;
    }

    public Path getExpectedOutputPath() {
        return expectedOutputPath;
    }
}
