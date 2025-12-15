package com.miracle;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RunTests {

    public static void runAllTests(){
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  RUNNING TEST SUITE");
        System.out.println("=".repeat(50));
        System.out.println();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mvn", "test");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println();
            System.out.println("=".repeat(50));
            if (exitCode == 0) {
                System.out.println("  ✓ All tests completed successfully!");
            } else {
                System.out.println("  ✗ Some tests failed. Check output above.");
            }
            System.out.println("=".repeat(50));

        } catch (Exception e) {
            System.out.println("Error running tests: " + e.getMessage());
            System.out.println();
            System.out.println("To run tests manually, execute:");
            System.out.println("  mvn test");
        }
    }



}