package main;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

public class Utilities {

    /**
     * Filters file to be put into/omitted from array by their extension
     *
     * @param name of file
     *
     */
    public static boolean accept(String name) {
        int l = name.length();
        return (name.substring(l - 4, l).equals(".png") || name.substring(l - 4, l).equals(".txt"));
    }

    /**
     * Compiles a list of files from the given directory, filtered according to accept() method
     *
     * @param dir Directory to start from
     * @return compiled filtered list of files in the directory and its subdirectories
     */
    public static ArrayList<File> getList(File dir) {
        File[] list = dir.listFiles();
        ArrayList<File> compiled = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            if (list[i].isDirectory()) {
                compiled.addAll(getList(list[i]));
            } else {
                if (accept(list[i].getName())) {
                    compiled.add(list[i]);
                }
            }
        }
        return compiled;
    }

    /**
     *  Converts a number (in pixel percentage) to scale with screen width
     *
     *  @param n: percentage of screen (in pixels)
     *  @return (int) (n*scale)
     */
    public static int scale(int n) {
        double scale = (Main.screen_Width / 1504.0);
        return (int) (n * scale);
    }
}