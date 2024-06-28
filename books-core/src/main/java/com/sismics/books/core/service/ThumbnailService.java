package com.sismics.books.core.service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.sismics.books.core.util.DirectoryUtil;
import com.sismics.books.core.util.mime.MimeType;
import com.sismics.books.core.util.mime.MimeTypeUtil;

public class ThumbnailService {


    public void downloadThumbnail(String id, String imageUrl) throws Exception {
    
        URLConnection imageConnection = new URL(imageUrl).openConnection();
        imageConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
        imageConnection.setConnectTimeout(10000);
        imageConnection.setReadTimeout(10000);
        try (InputStream inputStream = new BufferedInputStream(imageConnection.getInputStream())) {
            if (MimeTypeUtil.guessMimeType(inputStream) != MimeType.IMAGE_JPEG) {
                throw new Exception("Only JPEG images are supported as thumbnails");
            }
            
            Path imagePath = Paths.get(DirectoryUtil.getBookDirectory().getPath(), id);
            Files.copy(inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
    }
    
}
