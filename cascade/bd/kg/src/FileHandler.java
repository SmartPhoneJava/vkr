import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

abstract public class FileHandler {
    static String getName(String path) {
        int begin = path.indexOf("artyom/")+7, end = path.indexOf("?");
        if (begin == -1 || end == -1) {
            return "";
        }
        return "images/"+path.substring(begin, end);
    }

    static String getAvasName(String path, int userID) {
        String beginning = "artyom/"+userID+"/";
        int begin = path.indexOf(beginning), end = path.indexOf("?");
        System.out.println("readl path:"+path);
        System.out.println("startend:"+begin+" "+end);
        if (begin == -1 || end == -1) {
            return "";
        }
        System.out.println("next");
        return "images/"+path.substring(begin, end);
    }

    static String getType(String path) {
        int begin = path.indexOf(".")+1;
        return path.substring(begin);
    }

    static Boolean pathValid(String path) {
        int occurrence = StringUtils.countMatches(path, ".");
        return occurrence == 1;
    }

    static void downloadUsingStream(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int count=0;
        while((count = bis.read(buffer,0,1024)) != -1)
        {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();
    }

    static Image loadImage(String path, String name) {

        try {
            final String dir = System.getProperty("user.dir");
            String fileName = dir+name;
            BufferedImage img = ImageIO.read(new URL(path));
            File file = new File(fileName);
            // if (!file.exists()) {
            System.out.println("not exist:"+ name);
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            if (!file.createNewFile()) {
                System.out.println("cant create:"+ name);
            } else {
                System.out.println("create:"+ name);
            }
            // } else {
            //    System.out.println("exist:"+ file.getAbsolutePath());
            //}
            String fileType = getType(file.getName());
            ImageIO.write(img, fileType, file);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static Image getAva(String path, int userID) {
        String name = FileHandler.getAvasName(path, userID);
        if (name.equals("")) {
            name = FileHandler.getName(path);
        }
        Image image = FileHandler.loadImage(path, name);
        return image;
    }
}
