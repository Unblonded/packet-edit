package unblonded.packets;

import java.io.*;
import java.nio.file.*;

public class InjectorBridge {
    public static void runExecutable(String name) {
        try {
            String appDataPath = System.getenv("APPDATA");
            String minecraftFolder = appDataPath + "\\.minecraft\\packetutil";
            String exePath = minecraftFolder + "\\" + name;

            ProcessBuilder processBuilder = new ProcessBuilder(exePath);
            Process process = processBuilder.start();

            // Optionally, read and print the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to exit
            int exitCode = process.waitFor();
            System.out.println("Process exited with code " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String bedrockPath() {
        String appDataPath = System.getenv("APPDATA");
        String minecraftFolder = appDataPath + "\\.minecraft\\packetutil";
        String updPath = minecraftFolder + "\\bedrock.txt";
        return updPath;
    }
}
