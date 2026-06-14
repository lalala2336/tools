package com.xm.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class ToolsApplication {

    /**
     * Force UTF-8 on all encoding-sensitive channels BEFORE Spring starts.
     * <p>
     * On Windows, the JVM default charset is typically GBK (windows-936).
     * MCP protocol uses JSON-RPC over stdio, and JSON is always UTF-8.
     * If System.in is decoded as GBK, all Chinese characters are garbled
     * before they even reach the @Tool methods.
     * <p>
     * {@code -Dfile.encoding=UTF-8} in the launch command is the PRIMARY fix.
     * This static block is a best-effort fallback for stdout/stderr.
     */
    static {
        // https://openjdk.org/jeps/400 — file.encoding is read once at boot
        // on newer JDKs; set it anyway so downstream code picks it up.
        System.setProperty("file.encoding", "UTF-8");

        if (!Charset.defaultCharset().equals(StandardCharsets.UTF_8)) {
            // Wrap stdout/stderr so log messages and MCP JSON-RPC
            // responses are written in UTF-8 regardless of console codepage.
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ToolsApplication.class, args);
    }
}
