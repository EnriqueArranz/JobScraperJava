package com.jobscraper.scrapin.demo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public void writeToCsv(List<Job> jobs, String csvFilePath) {
        // Usamos OutputStreamWriter para asegurar que la codificación sea UTF-8
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(csvFilePath), StandardCharsets.UTF_8))) {
            // Escribir encabezado
            writer.write('\ufeff');
            writer.write("Title;Company;Location;Link;Source");
            writer.newLine(); // Salto de línea
            // Escribir datos de cada trabajo
            for (Job job : jobs) {
                writer.write(String.format("%s;%s;%s;%s;%s",
                        escapeSpecialCharacters(job.getTitle()),
                        escapeSpecialCharacters(job.getCompany()),
                        escapeSpecialCharacters(job.getLocation()),
                        escapeSpecialCharacters(job.getLink()),
                        escapeSpecialCharacters(job.getSource())
                ));
                writer.newLine(); // Salto de línea
            }
            System.out.println("Archivo CSV creado exitosamente en: " + csvFilePath);
        } catch (IOException e) {
            System.out.println("Error escribiendo el CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\"", "\"\"");
        if (escapedData.contains(",") || escapedData.contains("\"") || escapedData.contains("\n")) {
            escapedData = "\"" + escapedData + "\"";
        }
        return escapedData;
    }

    public List<String> getLinksFromCSV(String csvFilePath) {
        List<String> links = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                // Saltar la primera línea (encabezados)
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                // Usar un delimitador de punto y coma para separar las columnas
                String[] data = line.split(";");
                // Verificar que hay suficientes columnas y agregar el enlace
                if (data.length > 3) {
                    String link = data[3].replaceAll("\"", ""); // Eliminar comillas
                    links.add(link);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Archivo no encontrado: " + csvFilePath, e);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo: " + csvFilePath, e);
        }
        return links;
    }

}
