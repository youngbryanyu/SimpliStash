package com.youngbryanyu.simplistash.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

/* Unit tests for memory utils. */
public class MemoryUtilTest {
    /**
     * Test {@link MemoryUtil#printOnHeapMemoryUsage()}.
     */
    @Test
    public void testPrintOnHeapMemoryUsage() {
        /* Set streams */
        PrintStream originalOut = System.out; 
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        /* Call method */
        MemoryUtil.printOnHeapMemoryUsage();
        String output = outContent.toString();

        /* Check assertions */
        assertTrue(output.contains("Max memory:"));
        assertTrue(output.contains("Total memory (MB):"));
        assertTrue(output.contains("Free memory (MB):"));
        assertTrue(output.contains("Used memory (MB):"));
        
        /* Restore streams */
        System.setOut(originalOut); 
    }
}
