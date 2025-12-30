package com.stukit.codebench.domain;

public class TestCase {
    private final String name;
    private final String input;
    private final String expectedOutput;

    public TestCase(String name, String input, String expectedOutput) {
        this.name = name;
        this.input = input;
        this.expectedOutput = expectedOutput;
    }

    public String getName() {
        return name;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }
}
