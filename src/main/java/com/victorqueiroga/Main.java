package com.victorqueiroga;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Seja bem vindo ao ---==== DirDiff Explorer! ====---");
        System.out.println("Criado por ==== Victor Queiroga ==== ");
        System.out.println("Este programa compara dois diretórios e exibe as diferenças encontradas.");
        System.out.println("Por favor, insira os diretórios que deseja comparar.");

        System.out.print("Informe o diretório de origem:");
        String sourceDir = scanner.nextLine();

        System.out.print("Informe o diretório de destino:");
        String destDir = scanner.nextLine();

        try {
            DirDiffExplorer comparator = new DirDiffExplorer(sourceDir, destDir);
            comparator.compareDirectories();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
