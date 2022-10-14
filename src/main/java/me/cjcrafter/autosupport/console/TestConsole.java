package me.cjcrafter.autosupport.console;

import java.net.URL;

public class TestConsole {

    public static void main(String[] args) {
        URL folder = TestConsole.class.getClassLoader().getResource("examples");
        ConsoleImpl console = new ConsoleImpl(folder, System.in);
        System.out.println("Starting up");
        console.start();
    }
}
