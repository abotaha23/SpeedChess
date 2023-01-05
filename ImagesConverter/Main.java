import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;


public class Main {
	
    public static void main(String [] args) throws IOException {
        BufferedImage img = ImageIO.read(new File("Colors/palette.png"));
        CreateResFile("InputFile/" , "res" , img );
    }


    public static void CreateResFile(String input_folder , String outName , BufferedImage palette ) throws IOException{
        File all_files = new File(input_folder);
        if (!all_files.exists())
            return;

        FileWriter fw = new FileWriter(new File("OutputFile/" + outName + ".asm"));
        FileWriter ifw = new FileWriter(new File("OutputFile/" + outName + ".inc"));



        fw.write("; Auto Generated File by Java Code\n");
        fw.write("; Shouldn't be edited by hand     \n");
        fw.write("; Created At : " + new Date().toString() + " \n");

        ifw.write("; Auto Generated File by Java Code\n");
        ifw.write("; Shouldn't be edited by hand     \n");
        ifw.write("; You should use \"INCLUDE [THIS_FILE]\" in your source code file \n");
        ifw.write("; Created At : " + new Date().toString() + " \n");

        fw.write("\n\n");
        ifw.write("\n\n");

        File[] files = all_files.listFiles();

        if (files != null) {

            System.out.println("Generating Header... ");

            ifw.write(";Variables : \n");
            fw.write("PUBLIC ");
            for (int i = 0; i < files.length; i++) {
                if (isImageName(files[i].getName())){
                    ifw.write("EXTRN " + files[i].getName().split("\\.")[0] + ":BYTE\n");
                    fw.write(files[i].getName().split("\\.")[0]);
                    if (i < files.length - 1 && isImageName(files[i+1].getName()))
                        fw.write(", ");
                }
            }

            fw.write("\n");
            fw.write("\n");

            fw.write(".MODEL LARGE\n");
            fw.write(".STACK 64\n");
            fw.write("\n");
            fw.write(".DATA \n");
            fw.write("\n");

            for (File file : files) {
                if (isImageName(file.getName())) {
                    System.out.println("Writing Image: " + file.getName());
                    WriteHexImage(fw, file, 40, 160, palette );
                    fw.write("\n");
                }
            }

            fw.write("\n");
            fw.write("\n");
            fw.write("; No code in this file .. only data\n");
            fw.write(".CODE \n");
            fw.write("\n");
        }

        fw.write("END");
        fw.close();
        ifw.close();

        System.out.println("Done.");
    }
	
	
    private static void WriteHexImage(FileWriter fw, File image, int header_width, int line_size , BufferedImage palette) throws IOException {
        BufferedImage img = ImageIO.read(image);

        String name = image.getName().split("\\.")[0]; //name without extension
													   //not the best way to do it , but it works 90% of the time
													   //so lets just say its 100% of the time and call it a day xD

        fw.write(";Size: " + img.getWidth() + " x " + img.getHeight() + " \n");
        fw.write(name);
        for (int i = 0;i < header_width - name.length();i++)
            fw.write(' ');

        fw.write("db    ");

        int l = 0;
        for (int y = 0;y < img.getHeight();y++){
            for (int x = 0;x < img.getWidth();x++){
                int pixel = img.getRGB(x , y);
                int mx = 0,my = 0;
                double t , val = Double.MAX_VALUE;
                for (int dx = 0;dx < palette.getWidth();dx++){
                    for (int dy = 0;dy < palette.getWidth();dy++){
                        t = DistanceRGB(pixel , palette.getRGB(dx , dy));
                        if (t < val){
                            val = t;
                            mx = dx;
                            my = dy;
                        }
                    }
                }

                int num = mx + my * 16;
                int n2 = num >> 4 & 0x0f;
                num = num & 0x0f;
                String write = "";
                if (n2 >= 10)
                    write = "0";
                write += Integer.toHexString(n2) + Integer.toHexString(num) + "h";
                fw.write(write);
                l++;

                if (x == img.getWidth() - 1 && y == img.getHeight() - 1)
                    continue; //no need to write a new line or place a ','

                if (l % line_size == 0) {
                    fw.write("\n");
                    for (int i = 0;i < header_width;i++)
                        fw.write(' ');
                    fw.write("db    ");
                }else{
                    fw.write(",");
                }
            }
        }

        fw.write("\n");
    }

    public static boolean isImageName(String n){
        n = n.toLowerCase();
        return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
    }

    public static double DistanceRGB(int c0 , int c1){ //Calculates the distance between 2 colors (ARGB , treating them as Vectors)
        int a0 = (c0 >> 24) & 0xff;
        int r0 = (c0 >> 16) & 0xff ;
        int b0 = (c0 >> 8)  & 0xff ;
        int g0 = c0       & 0xff ;

        int a1 = (c1 >> 24) & 0xff;
        int r1 = (c1 >> 16) & 0xff ;
        int b1 = (c1 >> 8)  & 0xff ;
        int g1 = c1       & 0xff ;

        if (a0 == 0 && a1 == 0) //special empty color [0ffh] hard coded in graphics.asm
            return -1;

        int dr = r0 - r1;
        int db = b0 - b1;
        int dg = g0 - g1;

        return Math.sqrt(dr * dr + db * db + dg * dg);
    }

    public static void PrintHexByte(int i){
        int i2 = i;
        i = i & 0x0f;
        i2 = (i2 & 0xf0) >> 4;
        System.out.print(Integer.toHexString(i2) + Integer.toHexString(i));
    }
	
	public static void ConvertImage(String fName , BufferedImage palette , int ow , int oh) throws IOException{
        File fInput = new File(fName);
        BufferedImage img = ImageIO.read(new File(fName));
        BufferedImage scaled = new BufferedImage(ow , oh , BufferedImage.TYPE_INT_BGR);
        BufferedImage output = new BufferedImage(ow , oh , BufferedImage.TYPE_INT_BGR);

        scaled.getGraphics().drawImage(img , 0 , 0 , ow , oh , null);

        FileWriter f = new FileWriter("OutputFile/" + fInput.getName() + ".txt");

        //row by row
        //int buffer[][] = new int[palette.getWidth()][palette.getHeight()];
        int l = 0;
        f.write("          DB ");
        for (int y = 0;y < oh;y++){
            for (int x = 0;x < ow;x++){
                int pixel = scaled.getRGB(x , y);
                int mx = 0,my = 0;
                double t , val = Double.MAX_VALUE;
                for (int dx = 0;dx < palette.getWidth();dx++){
                    for (int dy = 0;dy < palette.getWidth();dy++){
                        t = DistanceRGB(pixel , palette.getRGB(dx , dy));
                        if (t <= val){
                            val = t;
                            mx = dx;
                            my = dy;
                        }
                    }
                }

                System.out.println(Integer.toHexString(pixel) + " * " + Integer.toHexString(palette.getRGB(mx , my)));

                output.setRGB(x , y , palette.getRGB(mx , my));
                int num = mx + my * 16;
                int n2 = num >> 4 & 0x0f;
                num = num & 0x0f;
                String write = "";
                if (n2 >= 10)
                    write = "0";
                write += Integer.toHexString(n2) + Integer.toHexString(num) + "h";
                f.write(write);
                l++;
                if (l % 160 == 0) {
                    f.write("\n");
                    f.write("          DB ");
                }else{
                    f.write(",");
                }
            }
        }

        ImageIO.write(output , "PNG" , new File("OutputFile/" + fInput.getName()));
        f.close();
    }
}
