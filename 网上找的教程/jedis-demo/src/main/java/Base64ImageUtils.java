import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;

import java.io.File;

import java.io.FileOutputStream;

import java.io.IOException;

import java.net.MalformedURLException;

import java.net.URL;
import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;

import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")

public class Base64ImageUtils {

    /**
     * 将网络图片进行Base64位编码
     *
     * @param imageUrl 图片的url路径，如http://.....xx.jpg
     * @return
     */

    public static String encodeImgageToBase64(URL imageUrl) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理

        ByteArrayOutputStream outputStream = null;

        try {

            BufferedImage bufferedImage = ImageIO.read(imageUrl);

            outputStream = new ByteArrayOutputStream();

            ImageIO.write(bufferedImage, "jpg", outputStream);

        } catch (MalformedURLException e1) {

            e1.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

        // 对字节数组Base64编码

        BASE64Encoder encoder = new BASE64Encoder();

        return encoder.encode(outputStream.toByteArray());// 返回Base64编码过的字节数组字符串

    }

    /**
     * 将本地图片进行Base64位编码
     *
     * @param imageFile 图片的url路径，如F:/.....xx.jpg
     * @return
     */

    public static String encodeImgageToBase64(File imageFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理

        ByteArrayOutputStream outputStream = null;

        try {

            BufferedImage bufferedImage = ImageIO.read(imageFile);

            outputStream = new ByteArrayOutputStream();

            ImageIO.write(bufferedImage, "jpg", outputStream);

        } catch (MalformedURLException e1) {

            e1.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        // 对字节数组Base64编码

        BASE64Encoder encoder = new BASE64Encoder();

        return encoder.encode(outputStream.toByteArray());// 返回Base64编码过的字节数组字符串
    }

    /**
     * 将Base64位编码的图片进行解码，并保存到指定文件夹
     *
     * @param base64 base64编码的图片信息
     * @return
     */

    public static void decodeBase64ToImage(String base64, String path, String imgName) {

        BASE64Decoder decoder = new BASE64Decoder();

        try {

            FileOutputStream write = new FileOutputStream(new File(path

                    + imgName));

            byte[] decoderBytes = decoder.decodeBuffer(base64);

            write.write(decoderBytes);

            write.close();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    public static void main(String[] args) {

        URL url = null;

        try {

            url = new URL("https://cdn.sspai.com/attachment/origin/2014/04/15/69489.jpg?imageView2/2/w/1120/q/90/interlace/1/ignore-error/1");

        } catch (MalformedURLException e) {

            e.printStackTrace();

        }

        String encoderStr = Base64ImageUtils.encodeImgageToBase64(url);
        File file = new File("C:/Users/zzj/Documents/workspace/football.jpg");
        String s = Base64ImageUtils.encodeImgageToBase64(file);
        Base64ImageUtils.decodeBase64ToImage(s,"C:/Users/zzj/Documents/workspace/","football1.jpg");
//        System.out.println(encoderStr);
        Base64ImageUtils.decodeBase64ToImage(encoderStr, "C:/Users/zzj/Documents/workspace/", "football.jpg");
    }


}