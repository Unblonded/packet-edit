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

    public static void extractFiles(String fileName) {
        String appDataPath = System.getenv("APPDATA");
        String minecraftFolder = appDataPath + "\\.minecraft\\packetutil";

        File directory = new File(minecraftFolder);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (InputStream inputStream = InjectorBridge.class.getResourceAsStream("/" + fileName)) {
            if (inputStream == null) {
                System.out.println("Resource not found in JAR: " + fileName);
                return;
            }

            Files.copy(inputStream, Paths.get(minecraftFolder, fileName), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File extracted to: " + minecraftFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
