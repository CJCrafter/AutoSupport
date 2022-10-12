package me.cjcrafter.autosupport;

import java.util.Scanner;

public class AutoSupport {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        while (true) {
            System.out.println("\tType a message: ");
            String input = scan.nextLine();

            if (!Activator.QUESTION.test(input)) {
                System.out.println("\tInput was not a question!");
                continue;
            }


        }
    }
}
