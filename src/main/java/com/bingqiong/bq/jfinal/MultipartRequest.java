package com.bingqiong.bq.jfinal;

import com.bingqiong.bq.conf.BqConf;
import com.jfinal.kit.PathKit;
import com.jfinal.upload.UploadFile;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;

import javax.naming.SizeLimitExceededException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.*;


/**
 * MultipartRequest.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MultipartRequest extends HttpServletRequestWrapper {

    private static String saveDirectory = PathKit.getWebRootPath();
    private static int maxPostSize;
    private static String encoding;

    private List<UploadFile> uploadFiles;

    private ServletFileUpload upload;

    private Map<String, String> params = new HashMap<>();
    private boolean sizeLimitExceeded;

    static void init(String saveDirectory, int maxPostSize, String encoding) {
        MultipartRequest.saveDirectory = saveDirectory;
        MultipartRequest.maxPostSize = maxPostSize;
        MultipartRequest.encoding = encoding;
    }

    public MultipartRequest(HttpServletRequest request, String saveDirectory,
                            int maxPostSize, String encoding) {
        super(request);
        wrapMultipartRequest(request, saveDirectory, maxPostSize, encoding);
    }

    public MultipartRequest(HttpServletRequest request, String saveDirectory,
                            int maxPostSize) {
        super(request);
        wrapMultipartRequest(request, saveDirectory, maxPostSize, encoding);
    }

    public MultipartRequest(HttpServletRequest request, String saveDirectory) {
        super(request);
        wrapMultipartRequest(request, saveDirectory, maxPostSize, encoding);
    }

    public MultipartRequest(HttpServletRequest request) {
        super(request);
        wrapMultipartRequest(request, saveDirectory, maxPostSize, encoding);
    }

    /**
     * 添加对相对路径的支持 1: 以 "/" 开头或者以 "x:开头的目录被认为是绝对路径 2: 其它路径被认为是相对路径, 需要
     * JFinalConfig.uploadedFileSaveDirectory 结合
     */
    private String handleSaveDirectory(String saveDirectory) {
        if (saveDirectory.startsWith("/") || saveDirectory.indexOf(":") == 1)
            return saveDirectory;
        else
            return MultipartRequest.saveDirectory + saveDirectory;
    }

    private void wrapMultipartRequest(HttpServletRequest request,
                                      String saveDirectory, int maxPostSize, String encoding) {

        saveDirectory = handleSaveDirectory(saveDirectory);

        File dir = new File(saveDirectory);
        if (!dir.exists())
            if (!dir.mkdirs())
                throw new RuntimeException("Directory " + saveDirectory
                        + " not exists and can not create directory.");

        if (params == null)
            params = new HashMap<>();

        DiskFileItemFactory factory = new DiskFileItemFactory();
        upload = new ServletFileUpload(factory);
        uploadFiles = new ArrayList<>();

        FileItemIterator iter;

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String paramName = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField()) {
                    String value = Streams.asString(stream);
                    params.put(paramName, value);
                } else {
                    String originalfileName = item.getName();
                    String uploadedFileName = System.currentTimeMillis()
                            + originalfileName;
                    String contentType = item.getContentType();

                    String filePath = saveDirectory + uploadedFileName;
                    File file = new File(filePath);
                    bis = new BufferedInputStream(stream);
                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    copy(bis, bos);

                    UploadFile uploadFile = new UploadFile(paramName,
                            saveDirectory, uploadedFileName, originalfileName,
                            contentType);
                    if (isSafeFile(uploadFile))
                        uploadFiles.add(uploadFile);
                }
            }
        } catch (FileUploadException | IOException e) {
            e.printStackTrace();
        } catch (SizeLimitExceededException e) {
            this.sizeLimitExceeded = true;
        } finally {
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(bos);
        }
    }

    public void copy(InputStream in, OutputStream out) throws IOException,
            SizeLimitExceededException {
        int contentLength = 0;
        byte[] buf = new byte[8192];
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
            contentLength += len;
            if (contentLength > BqConf.REQUEST_MAX_CONTENT_LENGHT)
                throw new SizeLimitExceededException();
        }
    }

    public boolean isSizeLimitExceeded() {
        return sizeLimitExceeded;
    }

    private boolean isSafeFile(UploadFile uploadFile) {
        if (uploadFile.getFileName().toLowerCase().endsWith(".jsp")) {
            uploadFile.getFile().delete();
            return false;
        }
        return true;
    }

    public List<UploadFile> getFiles() {
        return uploadFiles;
    }

    /**
     * Methods to replace HttpServletRequest methods
     */
    public Enumeration getParameterNames() {
        if (params == null)
            return null;
        return new Enumeration<String>() {
            private Iterator<String> keySetIter = params.keySet().iterator();

            @Override
            public String nextElement() {
                return keySetIter.next();
            }

            @Override
            public boolean hasMoreElements() {
                return keySetIter.hasNext();
            }
        };
    }

    public String getParameter(String name) {
        return params.get(name);
    }

    public String[] getParameterValues(String name) {
        return new String[]{params.get(name)};
    }

    public Map getParameterMap() {
        Map map = new HashMap();
        Enumeration enumm = getParameterNames();
        while (enumm.hasMoreElements()) {
            String name = (String) enumm.nextElement();
            map.put(name, getParameterValues(name));
        }
        return map;
    }
}