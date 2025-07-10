package com.filestreamer;

/**
 * Classe de entrada alternativa para a aplicação
 */
public class App 
{
    public static void main( String[] args )
    {
        // Redireciona para a aplicação Spring Boot principal
        com.filestreamer.spreadsheetgenerator.SpreadsheetGeneratorApplication.main(args);
    }
}
