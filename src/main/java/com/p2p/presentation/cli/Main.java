package com.p2p.presentation.cli;

import com.p2p.presentation.cli.menu.MainMenu;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        new MainMenu(scanner).tampil();
        scanner.close();
    }
}
