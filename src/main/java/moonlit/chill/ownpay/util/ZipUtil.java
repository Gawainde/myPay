package moonlit.chill.ownpay.util;

import lombok.extern.slf4j.Slf4j;
import moonlit.chill.ownpay.exception.PayException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;

/**
 * @author Gawaind
 * @date 2024/11/20 14:14
 */
@Slf4j
public class ZipUtil {

    public static void unzip(File zipFile, String filePath) {
        try (ZipArchiveInputStream inputStream = getZipFile(zipFile)) {
            ZipArchiveEntry entry = null;
            while ((entry = inputStream.getNextZipEntry()) != null) {
                if (entry.isDirectory()) {
                    File directory = new File(filePath, entry.getName());
                    directory.mkdirs();
                } else {
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(Files.newOutputStream(new File(filePath, entry.getName()).toPath()));
                        //输出文件路径信息
                        log.info(entry.getName());
                        IOUtils.copy(inputStream, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }


        } catch (Exception e) {
            log.info("解析账单文件失败  " + e.toString());
            throw new PayException("解析账单文件异常" + e.getMessage());
        }
    }

    private static ZipArchiveInputStream getZipFile(File zipFile) throws Exception {
        return new ZipArchiveInputStream(Files.newInputStream(zipFile.toPath()), "GBK");
    }

    public static void makdirs(String filePath) {
        File pathFile = new File(filePath);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
    }

    public static void delAllFile(String path) {
        File file = new File(path);
        String[] tempList = file.list();
        File temp;
        if (tempList != null) {
            for (String s : tempList) {
                if (path.endsWith(File.separator)) {
                    temp = new File(path + s);
                } else {
                    temp = new File(path + File.separator + s);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
                if (temp.isDirectory()) {
                    //先删除文件夹里面的文件
                    delAllFile(path + "/" + s);
                }
            }
        }
    }
}
