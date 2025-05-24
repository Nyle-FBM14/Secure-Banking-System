/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bankserver;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author tariq
 */
public class Audit {
    private File textFile;
    private String fileName;

    // Method to initialize a text file in the project directory
    public void initializeTextFile() {
        this.fileName = "audit";
        
        try {
            // Get the current working directory
            String projectDir = System.getProperty("user.dir");

            // Create the file object in the project directory
            this.textFile = new File(projectDir, fileName);

            // Create a new file if it doesn't exist
            if (textFile.createNewFile()) {
                System.out.println("Text file created: " + textFile.getAbsolutePath());
            } else {
                System.out.println("Text file already exists.");
            }
        } catch (IOException e) {
            System.err.println("Error occurred while initializing text file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Method to add a line of text to the text file
    public void appendLineToFile(String line) {
        if (textFile == null) {
            System.err.println("Audit file is not initialized. Call initializeTextFile() first.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(textFile, true))) {
            writer.write(line);
            writer.newLine(); // Add a new line after each line of text
            System.out.println("Line added to text file: " + line);
        } catch (IOException e) {
            System.err.println("Error occurred while appending to text file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String getfileName(){
        return fileName;
    }
    /*
    public static void main(String[] args) {
        // Create an instance of TextFileManager
        Audit manager = new Audit();

        // Initialize the text file (it will be created in the project directory)
        manager.initializeTextFile();

        // Add lines to the text file
        manager.appendLineToFile("Hello, this is line 1.");
        manager.appendLineToFile("This is line 2.");
        manager.appendLineToFile("Final line.");

        System.out.println("Text file update complete.");
    }
    */
}
