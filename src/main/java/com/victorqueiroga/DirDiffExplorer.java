package com.victorqueiroga;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class DirDiffExplorer {

    private Path sourceDir;
    private Path destDir;
    private List<String> differences;
    private long totalFiles;
    private long processedFiles;

    

    public DirDiffExplorer(String sourceDir, String destDir) {
        this.sourceDir = Paths.get(sourceDir);
        this.destDir = Paths.get(destDir);
        this.differences = new ArrayList<>();
    }

    public void compareDirectories() throws IOException {
        if (!Files.exists(sourceDir)) {
            System.err.println("Erro: Diretório de origem não existe.");
            return;
        }
        if (!Files.exists(destDir)) {
            System.err.println("Error: Diretório de destino não existe.");
            return;
        }

        // Counting total files in source directory for progress calculation
        totalFiles = countFiles(sourceDir);
        processedFiles = 0;

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = sourceDir.relativize(file);
                Path destFile = destDir.resolve(relativePath);

                if (!Files.exists(destFile)) {
                    differences.add("Arquivo excluído: " + relativePath);
                } else if (Files.size(file) != Files.size(destFile) || Files.getLastModifiedTime(file).compareTo(Files.getLastModifiedTime(destFile)) != 0) {
                    differences.add("Arquivo excluído: " + relativePath);
                }

                processedFiles++;
                printProgress();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Path relativePath = sourceDir.relativize(dir);
                Path destDirPath = destDir.resolve(relativePath);

                if (!Files.exists(destDirPath)) {
                    differences.add("Diretório excluído: " + relativePath);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        Files.walkFileTree(destDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = destDir.relativize(file);
                Path sourceFile = sourceDir.resolve(relativePath);

                if (!Files.exists(sourceFile)) {
                    differences.add("Arquivo adicionado: " + relativePath);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Path relativePath = destDir.relativize(dir);
                Path sourceDirPath = sourceDir.resolve(relativePath);

                if (!Files.exists(sourceDirPath)) {
                    differences.add("Diretório adicionado: " + relativePath);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        generatePDFReport();
    }

    private long countFiles(Path dir) throws IOException {
        return Files.walk(dir).filter(Files::isRegularFile).count();
    }

    private void printProgress() {
        double progress = (double) processedFiles / totalFiles * 100;
        System.out.printf("Progress: %.2f%%%n", progress);
    }

    private void generatePDFReport() {
        String pdfFileName = "DifferencesReport.pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(25, 700);

                contentStream.showText("Relatório de Diferenças entre Diretórios");
                contentStream.newLine();
                contentStream.newLine();

                for (String difference : differences) {
                    contentStream.showText(difference);
                    contentStream.newLine();
                }
                contentStream.newLine();
                contentStream.newLine();
                contentStream.showText("Total de arquivos processados: " + totalFiles);
                contentStream.newLine();
                contentStream.showText("Total de diferenças encontradas: " + differences.size());

                contentStream.endText();
            }

            document.save(pdfFileName);
            System.out.println("Diferenças salvas em -> " + pdfFileName);

            // Open the generated PDF file
            if (Desktop.isDesktopSupported()) {
                File pdfFile = new File(pdfFileName);
                if (pdfFile.exists()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar o relátorio: " + e.getMessage());
        }
    }

    public Path getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(Path sourceDir) {
        this.sourceDir = sourceDir;
    }

    public Path getDestDir() {
        return destDir;
    }

    public void setDestDir(Path destDir) {
        this.destDir = destDir;
    }

    public List<String> getDifferences() {
        return differences;
    }

    public void setDifferences(List<String> differences) {
        this.differences = differences;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(long totalFiles) {
        this.totalFiles = totalFiles;
    }

    public long getProcessedFiles() {
        return processedFiles;
    }

    public void setProcessedFiles(long processedFiles) {
        this.processedFiles = processedFiles;
    }

   
}
