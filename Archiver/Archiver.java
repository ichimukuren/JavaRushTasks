package com.javarush.task.task31.task3110;

import com.javarush.task.task31.task3110.command.ExitCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Archiver {


    ///Users/ichimukuren/Documents/arch.rtf
    public static void main(String[] args) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Введите полный путь файла-архива:");
       ZipFileManager manager =  new ZipFileManager(Paths.get(reader.readLine()));
        System.out.println("Введите путь архивируемового файла");
        manager.createZip(Paths.get(reader.readLine()));
        new ExitCommand().execute();


    }
}
