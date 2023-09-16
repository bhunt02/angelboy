package utilities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
public class StageConverter {

    public static void main(String[] args) throws IOException {
        File[] directory = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Queue/").listFiles();
        ArrayList<String> identities = new ArrayList<>();

        assert directory != null;
        for (File fi : directory) {
            String n = fi.getName();

            int indexOf = n.indexOf("_TILES.csv");
            if (indexOf != -1) {
                identities.add(n.substring(0,indexOf));
            }
        }

        for (String s: identities) {
            File ts = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Queue/" + s + "_TILES.csv");
            File fs = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Queue/" + s + "_FOREGROUND.csv");
            File ps = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Queue/" + s + "_POI.csv");
            File is = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Queue/" + s + "_INTERACTABLES.csv");
            boolean directoryExists = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Outputs/" + s + "/").mkdir();
            System.out.printf("Directory for %s already exists: %b\n",s,directoryExists);
            File data = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Outputs/" + s + "/" + s + "_data.txt");
            File t = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Outputs/" + s + "/" + s + "_TILES.png");
            File f = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Outputs/" + s + "/" + s + "_FOREGROUND.png");
            File p = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Outputs/" + s + "/" + s + "_POI.png");
            File i = new File("src/stages/DEBUG_LEVELS/Tiled/Conversion Outputs/" + s + "/" + s + "_INTERACTABLES.png");

            int width = -1;
            int height = -1;

            int rows = 0;
            int columns = 0;
            boolean countedLine = false;
            Scanner s0 = new Scanner(ts);
            s0.useDelimiter(",");
            while (s0.hasNextLine()) {
                String ln = s0.nextLine();
                if (!countedLine) {
                    Scanner s1 = new Scanner(ln);
                    s1.useDelimiter(",");
                    while (s1.hasNext()) {
                        s1.next();
                        ++columns;
                    }
                }
                countedLine = true;
                ++rows;
            }
            height = rows;
            width = columns;

            try {
                System.out.printf("Data %s txt already exists: %b\n",s,data.createNewFile());
                System.out.printf("Tile %s CSV already exists: %b\n",s,t.createNewFile());
                System.out.printf("Foreground %s CSV already exists: %b\n",s,f.createNewFile());
                System.out.printf("POI %s CSV already exists: %b\n",s,p.createNewFile());
                System.out.printf("Interactables %s CSV already exists: %b\n",s,i.createNewFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            PrintWriter dataWriter = new PrintWriter(data);
            int worldno = -1;
            if (Character.isDigit(s.charAt(0))) {
                worldno = Character.getNumericValue(s.charAt(0));
                String worldname = switch(worldno) {
                    case 1 -> "Underworld";
                    case 2 -> "Overworld";
                    case 3 -> "Mountain";
                    case 4 -> "Sky Kingdom";
                    case 5 -> "Palace";
                    default -> "Debug";
                };
                dataWriter.println("name "+worldname);
                dataWriter.println("world"+worldno);
                if (Character.isDigit(s.charAt(2))) {
                    dataWriter.println("level"+Character.getNumericValue(s.charAt(2)));
                }
            }
            if (s.contains("ROOM") || s.equals("NEST")) {
                dataWriter.println("stageMode-2");
            } else {
                dataWriter.println("stageMode0");
            }
            dataWriter.close();

            BufferedImage tiles = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            BufferedImage foreground = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            BufferedImage poi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            BufferedImage interactables = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            constructImage(tiles, ts);
            constructImage(foreground, fs);
            constructImage(poi, ps);
            constructImage(interactables, is);

            ImageIO.write(tiles, "png", t);
            ImageIO.write(foreground, "png", f);
            ImageIO.write(poi, "png", p);
            ImageIO.write(interactables, "png", i);

            dataWriter.close();
            s0.close();
            try {
                System.out.printf("Original Tile %s CSV disposed of: %b\n",s,ts.delete());
                System.out.printf("Original Foreground %s CSV disposed of: %b\n",s,fs.delete());
                System.out.printf("Original POI %s CSV disposed of: %b\n",s,ps.delete());
                System.out.printf("Original Interactables %s CSV disposed of: %b\n",s,ps.delete());
            } catch (SecurityException e) {
                continue;
            }
        }
    }

    private static void constructImage(BufferedImage toWrite, File source) {
        int i = 0;
        try {
            Scanner sc = new Scanner(source);
            while (sc.hasNextLine()) {
                String ln = sc.nextLine();
                Scanner reader = new Scanner(ln);
                reader.useDelimiter(",");
                int j = 0;
                while (reader.hasNext()) {
                    String s = reader.next();
                    int num = -1;
                    try {
                        num = Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        num = -1;
                    }
                    if (num != -1) {
                        int value = new Color(num, 0, 0, 255).getRGB();
                        toWrite.setRGB(j, i, value);
                    } else {
                        toWrite.setRGB(j, i, new Color(0, 0, 0, 0).getRGB());
                    }
                    j++;
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
