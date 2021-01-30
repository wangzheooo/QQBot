package com.example.wizardbot.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.axis.encoding.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>QRCodeUtil</p>
 *
 * @author lvhaosir6
 * @version 1.0.0
 * @date 2020/7/8
 */
@Slf4j
@UtilityClass
public class QRCodeUtil {
    private static final Logger logger = LoggerFactory.getLogger(QRCodeUtil.class);

    private int width = 300;
    private int height = 300;

    public static Map generateQRCodeImage(String text) {
        Map resultMap = new HashMap();
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            // 生成流
            MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig();
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix, matrixToImageConfig);

            //输出流
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", stream);
            String base64 = Base64.encode(stream.toByteArray());

            logger.info("generateQRCodeImage,success");
            resultMap.put("status", "success");
            resultMap.put("msg", "generateQRCodeImage,success");
            resultMap.put("result", base64);
            return resultMap;
        } catch (WriterException e) {
            logger.info("generateQRCodeImage,WriterException");
            resultMap.put("status", "fail");
            resultMap.put("msg", "generateQRCodeImage,WriterException");
        } catch (IOException e) {
            logger.info("generateQRCodeImage,IOException");
            resultMap.put("status", "fail");
            resultMap.put("msg", "generateQRCodeImage,IOException");
        }
        return resultMap;
    }
}