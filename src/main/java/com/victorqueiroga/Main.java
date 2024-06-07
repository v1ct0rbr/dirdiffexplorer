package com.victorqueiroga;

public class Main {

    
   /*  public static void main(String[] args) {
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
            DirComparator comparator = new DirComparator(sourceDir, destDir);
            comparator.compareDirectories();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    } */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            DirDiffExplorerUI explorerUI = new DirDiffExplorerUI();
            explorerUI.setVisible(true);
        });
    }
}
