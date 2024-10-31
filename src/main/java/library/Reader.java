package library;

import java.util.Scanner;

public class Reader
{
    private static final Scanner sc = new Scanner(System.in);

    public static int readInt()
    {
        return sc.nextInt();
    }

    public static String readLine()
    {
        return sc.nextLine().trim();
    }
}
