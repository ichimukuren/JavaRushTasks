package com.javarush.task.task30.task3008;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by ichimukuren on 25.02.2018.
 */
public class ConsoleHelper {
   private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));



   public static void writeMessage(String message) {
       System.out.println(message);
   }

   public static String readString() {
       String line = null;
       try {
           line = reader.readLine();
       }
       catch (IOException e) {
           System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
           line = readString();
       }
       return line;
   }
    public static int readInt() {
       int number = 0;
       try {
          number = Integer.parseInt(readString());
       }
       catch (NumberFormatException e) {
           System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
           number = readInt();
       }

       return number;
    }

}
