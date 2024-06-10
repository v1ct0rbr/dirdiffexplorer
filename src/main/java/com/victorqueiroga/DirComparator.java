package com.victorqueiroga;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.victorqueiroga.utils.MyDateUtils;

public class DirComparator {

    private static final String REPORT_PATH = "relatorios/";
    private static final int MAX_ITENS_PER_PAGE = 16;

    private Path sourceDir;
    private Path destDir;
    private List<String> differences;
    private long totalFiles;
    private long totalArquivosModificados = 0;
    private long totalArquivosExcluidos = 0;
    private long totalArquivosAdicionados = 0;
    private long totalDiretoriosAdicionados = 0;
    private long totalDiretoriosExcluidos = 0;
    private long processedFiles;
    private String filePath;
    private final DirDiffJFrame ui;
    private static DirComparator instance;

    public DirComparator(String sourceDir, String destDir, DirDiffJFrame ui) {
        this.sourceDir = Paths.get(sourceDir);
        this.destDir = Paths.get(destDir);
        this.differences = new ArrayList<>();
        this.ui = ui;
        this.filePath = "";
    }

    // public static DirComparator initialize(String sourceDir, String destDir,
    // DirDiffJFrame ui) {
    // if (instance == null) {
    // instance = new DirComparator(sourceDir, destDir, ui);
    // } else {
    // this.differences = new ArrayList<>();
    // instance.setDestDir(Paths.get(destDir));
    // instance.setSourceDir(Paths.get(sourceDir));
    // }
    // return instance;
    // }

    public static DirComparator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Singleton de comparacao não foi inicializado.");
        }
        return instance;
    }

    public boolean compareDirectories() throws IOException {
        if (!Files.exists(sourceDir)) {
            ui.showError("Erro: Diretório de origem não existe.");
            return false;
        }
        if (!Files.exists(destDir)) {
            ui.showError("Error: Diretório de destino não existe.");

            return false;
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
                    String difereString = "- Arquivo excluído: " + relativePath;
                    differences.add(difereString);
                    ui.updateResult(difereString);
                    totalArquivosExcluidos++;

                } else {
                    long size = Files.size(file);
                    long sizeDest = Files.size(destFile);
                    FileTime lastModified = Files.getLastModifiedTime(file);
                    FileTime lastModifiedDest = Files.getLastModifiedTime(destFile);
                    String diffInfoTemp = " | Tamanho ( " + size + " -> " + sizeDest + " )"
                            + " | Data de modificacao: ("
                            + MyDateUtils.convertFileTimeToLocalDateTimeString(lastModified)
                            + " -> "
                            + MyDateUtils.convertFileTimeToLocalDateTimeString(lastModifiedDest) + ")";

                    // Comparando se o arquivo foi modificado. Se os tamanhos forem diferentes ou a
                    // data de modificação for diferente por uma diferença de tempo > 5 segundos
                    if (size != sizeDest || (Math.abs(lastModified.toMillis() - lastModifiedDest.toMillis()) > 5000
                            || (Math.abs(lastModifiedDest.toMillis() - lastModified.toMillis()) > 5000))) {
                        String difereString = "* Arquivo modificado: " + relativePath + diffInfoTemp;
                        differences.add(difereString);
                        ui.updateResult(difereString);
                        totalArquivosModificados++;
                    }

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
                    String difereString = "- Diretório excluído: " + relativePath;
                    differences.add(difereString);
                    ui.updateResult(difereString);
                    totalDiretoriosExcluidos++;
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
                    String difereString = "+ Arquivo adicionado: " + relativePath;
                    differences.add(difereString);
                    ui.updateResult(difereString);
                    totalArquivosAdicionados++;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Path relativePath = destDir.relativize(dir);
                Path sourceDirPath = sourceDir.resolve(relativePath);

                if (!Files.exists(sourceDirPath)) {
                    String difereString = "+ Diretório adicionado: " + relativePath;
                    differences.add(difereString);
                    ui.updateResult(difereString);
                    totalDiretoriosAdicionados++;
                }

                return FileVisitResult.CONTINUE;
            }
        });

        return generatePDFReport();
    }

    private long countFiles(Path dir) throws IOException {
        return Files.walk(dir).filter(Files::isRegularFile).count();
    }

    private void printProgress() {
        double progress = (double) processedFiles / totalFiles * 100;
        // System.out.printf("Progress: %.2f%%%n", progress);
        ui.updateProgress(progress);
    }

    private boolean generatePDFReport() {
        String userName = System.getProperty("user.name");
        String pdfFileName = "DifferencesReport_"
                + MyDateUtils.formatLocalDateTime(LocalDateTime.now(), MyDateUtils.PATTERN_LOCALDATETIME) + ".pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDType0Font font = PDType0Font.load(document, new File("resources/fonts/NotoSans-Regular.ttf"));

            try {
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(25, 700);

                contentStream.showText("Differences Report - Gerado em "
                        + MyDateUtils.formatLocalDateTime(LocalDateTime.now(),
                                MyDateUtils.PATTERN_LOCALDATETIMEFORMAL));
                contentStream.newLine();
                contentStream.showText("Usuário: " + userName);
                contentStream.newLine();
                contentStream.showText("Origem: " + sourceDir);
                contentStream.newLine();
                contentStream.showText("Destino: " + destDir);
                contentStream.newLine();
                contentStream.newLine();

                long diferencesAddedPerPage = 0;
                for (String difference : differences) {

                    if (diferencesAddedPerPage > (MAX_ITENS_PER_PAGE - 1)) {
                        contentStream.endText(); // Encerra o texto atual antes de fechar o fluxo
                        contentStream.close();
                        PDPage blankPage = new PDPage();
                        document.addPage(blankPage);
                        contentStream = new PDPageContentStream(document, blankPage);
                        contentStream.beginText();
                        contentStream.setFont(font, 12);
                        contentStream.setLeading(14.5f);
                        contentStream.newLineAtOffset(25, 700);

                        diferencesAddedPerPage = 0;
                        ui.clearResult();

                    }
                    addWrappedText(contentStream, difference, 550); // Adjust the width as per your page size
                    contentStream.newLine();
                    diferencesAddedPerPage++;
                }

                contentStream.newLine();
                contentStream.showText("Total de arquivos modificados: " + totalArquivosModificados);
                ui.updateResult("Total de arquivos modificados: " + totalArquivosModificados);
                contentStream.newLine();
                contentStream.showText("Total de arquivos excluídos: " + totalArquivosExcluidos);
                ui.updateResult("Total de arquivos excluídos: " + totalArquivosExcluidos);
                contentStream.newLine();
                contentStream.showText("Total de arquivos adicionados: " + totalArquivosAdicionados);
                ui.updateResult("Total de arquivos adicionados: " + totalArquivosAdicionados);
                contentStream.newLine();
                contentStream.showText("Total de diretórios adicionados: " + totalDiretoriosAdicionados);
                ui.updateResult("Total de diretórios adicionados: " + totalDiretoriosAdicionados);
                contentStream.newLine();
                contentStream.showText("Total de diretórios excluídos: " + totalDiretoriosExcluidos);
                ui.updateResult("Total de diretórios excluídos: " + totalDiretoriosExcluidos);
                contentStream.newLine();
                contentStream.showText("Total de arquivos processados: " + totalFiles);
                ui.updateResult("Total de arquivos processados: " + totalFiles);

                contentStream.endText();
                contentStream.close();
            } catch (IOException e) {
                ui.showError(e.getMessage());
            }

            // Create the REPORT_PATH directory if it doesn't exist
            File reportDir = new File(REPORT_PATH);
            if (!reportDir.exists()) {
                if (reportDir.mkdirs()) {
                    // System.out.println("Diretório REPORT_PATH criado com sucesso.");
                    ui.updateResult("Diretório REPORT_PATH criado com sucesso.");
                } else {
                    ui.showError("Erro ao criar o diretório REPORT_PATH.");
                    // System.err.println("Erro ao criar o diretório REPORT_PATH.");
                    throw new IOException("Não há permissão para criar o diretório de relatórios.");
                }
            }
            filePath = REPORT_PATH + pdfFileName;
            this.ui.setCurrentFile(filePath);
            document.save(filePath);

            ui.updateResult("Diferenças salvas em -> " + filePath);
            // System.out.println("Diferenças salvas em -> " + REPORT_PATH + pdfFileName);

            return true;
        } catch (IOException e) {
            ui.showError("Erro ao criar o relatório: " + e.getMessage());
            return false;
            // System.err.println("Erro ao criar o relátorio: " + e.getMessage());
        }
    }

    private void addWrappedText(PDPageContentStream contentStream, String text, float maxWidth) throws IOException {
        List<String> lines = getWrappedText(text, maxWidth, PDType1Font.HELVETICA_BOLD, 12);
        for (String line : lines) {
            contentStream.showText(line);
            contentStream.newLine();
        }
    }

    private List<String> getWrappedText(String text, float maxWidth, PDType1Font font, float fontSize)
            throws IOException {
        List<String> lines = new ArrayList<>();
        // int lastSpace = -1;
        float spaceWidth = font.getStringWidth(" ") / 1000 * fontSize;
        String[] words = text.split(" ");

        StringBuilder currentLine = new StringBuilder();
        float currentLineWidth = 0;

        for (String word : words) {
            float wordWidth = font.getStringWidth(word) / 1000 * fontSize;
            if (currentLineWidth + wordWidth + spaceWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
                currentLineWidth = wordWidth;
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                    currentLineWidth += spaceWidth;
                }
                currentLine.append(word);
                currentLineWidth += wordWidth;
            }
        }
        lines.add(currentLine.toString());
        return lines;
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
