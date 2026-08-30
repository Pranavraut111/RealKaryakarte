package com.mandal.servlet;

import com.mandal.dto.ApiResponse;
import com.mandal.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/api/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
    maxFileSize = 1024 * 1024 * 10,      // 10 MB
    maxRequestSize = 1024 * 1024 * 15    // 15 MB
)
public class FileUploadServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Ensure user is authenticated
        if (request.getAttribute("userId") == null) {
            JsonUtil.writeError(response, 401, "Unauthorized");
            return;
        }

        // We will store uploads in a persistent dir outside of target/ if possible
        // To make it simple for this project, let's use a fixed directory:
        String uploadFilePath = com.mandal.util.ConfigUtil.get("storage.base.dir", 
                System.getProperty("user.home") + "/mandal_data") + "/uploads";
        File f = new File(uploadFilePath);
        if (!f.exists()) f.mkdirs();

        try {
            Part filePart = request.getPart("file");
            if (filePart == null) {
                JsonUtil.writeError(response, 400, "No file uploaded");
                return;
            }

            String fileName = getFileName(filePart);
            if (fileName == null || fileName.isEmpty()) {
                JsonUtil.writeError(response, 400, "Invalid file");
                return;
            }

            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i);
            }

            String newFileName = UUID.randomUUID().toString() + extension;
            filePart.write(uploadFilePath + File.separator + newFileName);

            // Serve through /api/uploads/
            String fileUrl = "/api/uploads/" + newFileName;
            
            JsonUtil.writeOk(response, ApiResponse.ok("File uploaded successfully", fileUrl));

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "File upload failed: " + e.getMessage());
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
}
