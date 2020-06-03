package com.lucky.jacklamb.file.utils;

import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.servlet.Model;
import com.lucky.jacklamb.servlet.StaticResourceManage;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.UUID;


public abstract class FileCopyUtils {

    public static final int BUFFER_SIZE = 4096;


    //---------------------------------------------------------------------
    // Copy methods for java.io.File
    //---------------------------------------------------------------------

    /**
     * Copy the contents of the given input File to the given output File.
     *
     * @param in  the file to copy from
     * @param out the file to copy to
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    public static int copy(File in, File out) throws IOException {
        notNull(in, "No input File specified");
        notNull(out, "No output File specified");
        return copy(Files.newInputStream(in.toPath()), Files.newOutputStream(out.toPath()));
    }

    /**
     * Copy the contents of the given byte array to the given output File.
     *
     * @param in  the byte array to copy from
     * @param out the file to copy to
     * @throws IOException in case of I/O errors
     */
    public static void copy(byte[] in, File out) throws IOException {
        notNull(in, "No input byte array specified");
        notNull(out, "No output File specified");
        copy(new ByteArrayInputStream(in), Files.newOutputStream(out.toPath()));
    }

    /**
     * Copy the contents of the given input File into a new byte array.
     *
     * @param in the file to copy from
     * @return the new byte array that has been copied to
     * @throws IOException in case of I/O errors
     */
    public static byte[] copyToByteArray(File in) throws IOException {
        notNull(in, "No input File specified");
        return copyToByteArray(Files.newInputStream(in.toPath()));
    }


    //---------------------------------------------------------------------
    // Copy methods for java.io.InputStream / java.io.OutputStream
    //---------------------------------------------------------------------

    /**
     * Copy the contents of the given InputStream to the given OutputStream.
     * Closes both streams when done.
     *
     * @param in  the stream to copy from
     * @param out the stream to copy to
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        notNull(in, "No InputStream specified");
        notNull(out, "No OutputStream specified");

        try {
            return copyBase(in, out);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
            try {
                out.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Copy the contents of the given byte array to the given OutputStream.
     * Closes the stream when done.
     *
     * @param in  the byte array to copy from
     * @param out the OutputStream to copy to
     * @throws IOException in case of I/O errors
     */
    public static void copy(byte[] in, OutputStream out) throws IOException {
        notNull(in, "No input byte array specified");
        notNull(out, "No OutputStream specified");

        try {
            out.write(in);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Copy the contents of the given InputStream into a new byte array.
     * Closes the stream when done.
     *
     * @param in the stream to copy from (may be {@code null} or empty)
     * @return the new byte array that has been copied to (possibly empty)
     * @throws IOException in case of I/O errors
     */
    public static byte[] copyToByteArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }


    //---------------------------------------------------------------------
    // Copy methods for java.io.Reader / java.io.Writer
    //---------------------------------------------------------------------

    /**
     * Copy the contents of the given Reader to the given Writer.
     * Closes both when done.
     *
     * @param in  the Reader to copy from
     * @param out the Writer to copy to
     * @return the number of characters copied
     * @throws IOException in case of I/O errors
     */
    public static int copy(Reader in, Writer out) throws IOException {
        notNull(in, "No Reader specified");
        notNull(out, "No Writer specified");

        try {
            int byteCount = 0;
            char[] buffer = new char[BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
            try {
                out.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Copy the contents of the given String to the given output Writer.
     * Closes the writer when done.
     *
     * @param in  the String to copy from
     * @param out the Writer to copy to
     * @throws IOException in case of I/O errors
     */
    public static void copy(String in, Writer out) throws IOException {
        notNull(in, "No input String specified");
        notNull(out, "No Writer specified");

        try {
            out.write(in);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Copy the contents of the given Reader into a String.
     * Closes the reader when done.
     *
     * @param in the reader to copy from (may be {@code null} or empty)
     * @return the String that has been copied to (possibly empty)
     * @throws IOException in case of I/O errors
     */
    public static String copyToString(Reader in) throws IOException {
        if (in == null) {
            return "";
        }

        StringWriter out = new StringWriter();
        copy(in, out);
        return out.toString();
    }

    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Copy the contents of the given InputStream to the given OutputStream.
     * Leaves both streams open when done.
     *
     * @param in  the InputStream to copy from
     * @param out the OutputStream to copy to
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    public static int copyBase(InputStream in, OutputStream out) throws IOException {
        notNull(in, "No InputStream specified");
        notNull(out, "No OutputStream specified");

        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        return byteCount;
    }

    public static void preview(Model model, File in) throws IOException {
        HttpServletResponse resp=model.getResponse();
        if(StaticResourceManage.isStaticResource(resp,in.getName())){
            if(in.exists()){
                byte[] buffer=copyToByteArray(in);
                ServletOutputStream outputStream = resp.getOutputStream();
                copy(buffer,outputStream);
            }
        }else{
            model.error(Code.REFUSED,"未知格式的文件，无法预览！","格式未知的文件: "+in.getName());
        }
    }

    public static void preview(Model model,InputStream in,String fileName) throws IOException {
        HttpServletResponse resp=model.getResponse();
        if(StaticResourceManage.isStaticResource(resp,fileName)) {
            byte[] buffer = copyToByteArray(in);
            ServletOutputStream outputStream = resp.getOutputStream();
            copy(buffer, outputStream);
        }else{
            model.error(Code.REFUSED,"未知格式的文件，无法预览！","格式未知的文件: "+fileName);
        }
    }

    public static void preview(Model model,byte[] in,String fileName) throws IOException {
        HttpServletResponse resp=model.getResponse();
        if(StaticResourceManage.isStaticResource(resp,fileName)) {
            ServletOutputStream outputStream = resp.getOutputStream();
            copy(in, outputStream);
        }else{
            model.error(Code.REFUSED,"未知格式的文件，无法预览！","格式未知的文件: "+fileName);
        }
    }

    public static void download(HttpServletResponse response,File in) throws IOException {
        //设置文件下载头
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(in.getName(),"UTF-8"));
        //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("multipart/form-data");
        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        copy(copyToByteArray(in),out);
    }

    public static void download(HttpServletResponse response,InputStream in,String fileName) throws IOException {
        //设置文件下载头
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8"));
        //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("multipart/form-data");
        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        copy(in,out);
    }

    public static void download(HttpServletResponse response,byte[] in,String fileName) throws IOException {
        //设置文件下载头
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8"));
        //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("multipart/form-data");
        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        copy(in,out);
    }

}



