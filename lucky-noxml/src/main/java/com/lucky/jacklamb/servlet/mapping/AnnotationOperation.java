package com.lucky.jacklamb.servlet.mapping;

import com.lucky.jacklamb.annotation.ioc.Controller;
import com.lucky.jacklamb.annotation.mvc.*;
import com.lucky.jacklamb.cglib.ASMUtil;
import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.exception.*;
import com.lucky.jacklamb.file.MultipartFile;
import com.lucky.jacklamb.file.utils.FileUtils;
import com.lucky.jacklamb.httpclient.HttpClientCall;
import com.lucky.jacklamb.httpclient.callcontroller.Api;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;
import com.lucky.jacklamb.md5.MD5Utils;
import com.lucky.jacklamb.rest.LSON;
import com.lucky.jacklamb.rest.LXML;
import com.lucky.jacklamb.servlet.core.Model;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.base.LuckyUtils;
import com.lucky.jacklamb.utils.regula.Regular;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class AnnotationOperation {

    private static final Logger log = LogManager.getLogger(AnnotationOperation.class);

    private static final WebConfig webCfg= AppConfig.getAppConfig().getWebConfig();
    
    private static final LSON lson=new LSON();

    private static final LXML lxml=new LXML();
    /**
     * 基于MultipartFile的多文件上传
     *
     * @param model  Model对象
     * @param method 将要执行的Controller方法
     * @return 由Controller方法参数名和其对应的值所组成的Map(针对MultipartFile)
     * @throws IOException
     * @throws ServletException
     */
    private void moreMultipartFil(Model model, Method method)
            throws IOException, FileUploadException, FileSizeCrossingException, RequestFileSizeCrossingException {
        Parameter[] parameters = method.getParameters();
        String[] paramNames = ASMUtil.getMethodParamNames(method);
        List<String> paramlist = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            if (MultipartFile.class == parameters[i].getType() || MultipartFile[].class == parameters[i].getType()) {
                paramlist.add(Mapping.getParamName(parameters[i], paramNames[i]));
            }
        }
        setMultipartFileMap(model);
    }

    /**
     * MultipartFile的多文件上传,基于Apache [commons-fileupload-1.3.1.jar  commons-io-2.4.jar]
     *
     * @param model Model对象
     */
    public void setMultipartFileMap(Model model) throws FileUploadException, IOException, FileSizeCrossingException, RequestFileSizeCrossingException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");
        if (!FileUploadBase.isMultipartContent(new ServletRequestContext(model.getRequest()))){
            return;
        }
        List<FileItem> list = upload.parseRequest(model.getRequest());
        //同名分组
        Map<String, List<FileItem>> sameNameFileItemMap = list.stream().collect(Collectors.groupingBy(FileItem::getFieldName));
        Set<String> fieldNames = sameNameFileItemMap.keySet();
        List<FileItem> fileItemList;
        MultipartFile[] multipartFiles;
        for (String fn : fieldNames) {
            fileItemList = sameNameFileItemMap.get(fn);
            boolean isFile = false;
            multipartFiles = new MultipartFile[fileItemList.size()];
            int fileIndex = 0;
            for (FileItem item : fileItemList) {
                if (!item.isFormField()) {
                    String filename = item.getName();
                    isFile = true;
                    InputStream in = item.getInputStream();
                    if(in.available()/1024>webCfg.getMultipartMaxFileSize()) {
                        throw new FileSizeCrossingException("单个文件超过最大上传限制："+webCfg.getMultipartMaxFileSize()+"kb");
                    }
                    MultipartFile mfp = new MultipartFile(in,model,filename);
                    multipartFiles[fileIndex] = mfp;
                    fileIndex++;
                } else {
                    if (!model.parameterMapContainsKey(item.getFieldName())) {
                        String[] values = {new String(item.get(), "UTF-8")};
                        model.addParameter(fn, values);
                    }
                }
            }
            if (isFile){
                double totalSize=0;
                for (MultipartFile mu : multipartFiles) {
                    totalSize+=mu.getFileSize();
                }
                if(totalSize/1024>webCfg.getMultipartMaxRequestSize()){
                    throw new RequestFileSizeCrossingException("总文件超过最大上传限制："+webCfg.getMultipartMaxRequestSize()+"kb");
                }else{
                    model.addMultipartFile(fn, multipartFiles);
                }
            }
        }
    }

    /**
     * 批量文件上传@Upload注解方式
     *
     * @param model  Model对象
     * @param method 将要执行的Controller方法
     * @return 上传后的文件名与表单name属性所组成的Map
     * @throws IOException
     * @throws ServletException
     */
    private void moreUpload(Model model, Method method) throws IOException, FileTypeIllegalException, FileSizeCrossingException, FileUploadException, RequestFileSizeCrossingException {
        if (method.isAnnotationPresent(Upload.class)) {
            Upload upload = method.getAnnotation(Upload.class);
            String[] files = upload.names();
            String[] savePaths = upload.filePath();
            String types = upload.type();
            long maxSize = upload.maxSize();
            long totalSize=upload.totalSize();
            Map<String, String> fieldAndFolder = new HashMap<>();
            if (savePaths.length == 1) {
                for (String file : files) {
                    fieldAndFolder.put(file, savePaths[0]);
                }
            } else {
                for (int i = 0; i < savePaths.length; i++) {
                    fieldAndFolder.put(files[i], savePaths[i]);
                }
            }
            upload(model, fieldAndFolder, types, maxSize,totalSize);
        }
    }

    /**
     * --@Upload注解方式的多文件上传-基于Apache [commons-fileupload-1.3.1.jar  commons-io-2.4.jar]
     * 适配内嵌tomcat的文件上传与下载操作
     *
     * @param model          Model对象
     * @param fieldAndFolder name与folder组成的Map
     * @param type           允许上传的文件类型
     * @param fileSize        允许上传文件的最大大小
     */
    public void upload(Model model, Map<String, String> fieldAndFolder, String type, long fileSize,long totalSize) throws FileTypeIllegalException, IOException, FileSizeCrossingException, FileUploadException, RequestFileSizeCrossingException {
        String savePath = model.getRealPath("/");
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");
        if (!ServletFileUpload.isMultipartContent(model.getRequest())) {
            return;
        }
        List<FileItem> list = upload.parseRequest(model.getRequest());
        //同名分组
        Map<String, List<FileItem>> sameNameFileItemMap = list.stream().collect(Collectors.groupingBy(FileItem::getFieldName));
        Set<String> fieldNames = sameNameFileItemMap.keySet();
        List<FileItem> fileItemList;

        File[] uploadNames;
        for (String fn : fieldNames) {
            fileItemList = sameNameFileItemMap.get(fn);
            boolean isFile = false;
            uploadNames = new File[fileItemList.size()];
            List<UploadCopy> uploadCopyList=new ArrayList<>();
            int fileIndex = 0;
            for (FileItem item : fileItemList) {
                if (!item.isFormField()) {
                    if (fieldAndFolder.containsKey(fn)) {
                        isFile = true;
                        String filename = item.getName();
                        String suffix = filename.substring(filename.lastIndexOf("."));
                        String NoSuffix = filename.substring(0, filename.lastIndexOf("."));
                        if (!"".equals(type) && !type.toLowerCase().contains(suffix.toLowerCase())) {
                            throw new FileTypeIllegalException("上传的文件格式" + suffix + "不合法！合法的文件格式为：" + type);
                        }
                        String pathSave = fieldAndFolder.get(fn);
                        String filePath;
                        if (pathSave.startsWith("abs:")) {//绝对路径写法
                            filePath=pathSave.substring(4);
                        } else {//相对路径写法
                            filePath=savePath + pathSave;
                        }
                        filename = NoSuffix + "_" + new Date().getTime() + "_" + LuckyUtils.getRandomNumber() + suffix;
                        if (filename == null || "".equals(filename.trim())) {
                            continue;
                        }
                        filePath=filePath.endsWith(File.separator)?filePath.substring(0,filePath.length()-1):filePath;
                        filePath=filePath+File.separator + filename;
                        InputStream in = item.getInputStream();
                        fileSize=fileSize==0?webCfg.getMultipartMaxFileSize():fileSize;
                        int size = in.available();
                        int filesize = size / 1024;
                        if (filesize > fileSize) {
                            throw new FileSizeCrossingException("单个上传文件的大小超出最大上传限制：" + fileSize + "kb");
                        }
                        uploadCopyList.add(new UploadCopy(in,new File(filePath)));
                        item.delete();
                        uploadNames[fileIndex] = new File(filePath);
                        fileIndex++;
                    }
                } else {
                    if (!model.parameterMapContainsKey(item.getFieldName())) {
                        String[] values = {new String(item.get(), "UTF-8")};
                        model.addParameter(item.getFieldName(), values);
                    }
                }
            }
            if (isFile){
                double msxSize=UploadCopy.getTotalSize(uploadCopyList);
                totalSize=totalSize==0?webCfg.getMultipartMaxRequestSize():totalSize;
                if(msxSize/1024>totalSize){
                    throw new RequestFileSizeCrossingException("总文件超过最大上传限制："+webCfg.getMultipartMaxRequestSize()+"kb");
                }else{
                    for (UploadCopy uploadCopy : uploadCopyList) {
                        uploadCopy.copy();
                    }
                    model.addUploadFile(fn, uploadNames);
                }

            }
        }
    }

    /**
     * 返回Controller方法参数名与参数值所组成的Map(针对Pojo类型的参数)
     *
     * @param model  Model对象
     * @param method 将要执行的Controller方法
     * @return Controller方法参数名与参数值所组成的Map(针对Pojo类型的参数)
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws ServletException
     */
    private Map<String, Object> pojoParam(Model model, Method method)
            throws InstantiationException, IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        String[] paramNames = ASMUtil.getMethodParamNames(method);
        int i = 0;
        for (Parameter param : parameters) {
            if (MultipartFile.class != param.getType() && MultipartFile[].class != param.getType()
                    && param.getType().getClassLoader() != null
                    && !ServletRequest.class.isAssignableFrom(param.getType())
                    && !ServletResponse.class.isAssignableFrom(param.getType())
                    && !HttpSession.class.isAssignableFrom(param.getType())
                    && !ServletContext.class.isAssignableFrom(param.getType())
                    && !Model.class.isAssignableFrom(param.getType()) && canInjection(param.getType(), model)) {
                Class<?> pojoclzz = param.getType();
                Object pojo = pojoclzz.newInstance();
                Field[] fields = pojoclzz.getDeclaredFields();
                createObject(model, pojo);
                for (Field fi : fields) {
                    fi.setAccessible(true);
                    Object fi_obj = fi.get(pojo);
                    // pojo中含有@Upload返回的文件名
                    if (model.uploadFileMapContainsKey(fi.getName())) {
                        File[] uploadFiles = model.getUploadFileArray(fi.getName());
                        if (fi.getType() == String[].class) {
                            String[] uploadFileNames = new String[uploadFiles.length];
                            for (int x = 0; x < uploadFiles.length; i++) {
                                uploadFileNames[x] = uploadFiles[x].getName();
                            }
                            fi.set(pojo, uploadFiles);
                        } else if (fi.getType() == String.class) {
                            fi.set(pojo, uploadFiles[0].getName());
                        } else if (fi.getType() == File.class) {
                            fi.set(pojo, uploadFiles[0]);
                        } else if (fi.getType() == File[].class) {
                            fi.set(pojo, uploadFiles);
                        }
                    }

                }
                map.put(Mapping.getParamName(param, paramNames[i]), pojo);
            }
            i++;
        }
        return map;

    }

    /**
     * 文件下载操作@Download
     *
     * @param model  Model对象
     * @param method 将要执行的Controller方法
     * @throws IOException
     */
    public void download(Model model, Method method) throws IOException, URISyntaxException {
        Download dl = method.getAnnotation(Download.class);
        InputStream fis = null;
        String downName = null;
        String path = "";
        if (!"".equals(dl.path())) {
            path = dl.path();
        } else if (!"".equals(dl.docPath())) {
            path = model.getRealPath("") + dl.docPath();
        } else if (!"".equals(dl.url())) {
            String url = dl.url();
            byte[] buffer = HttpClientCall.getCallByte(url, new HashMap<>());
            String fileName = model.getResponse().getHeader("Content-Disposition");
            if (fileName == null) {
                fileName = "lucky_" + LuckyUtils.getRandomNumber() + url.substring(url.lastIndexOf("."));
            } else {
                fileName.replaceAll("attachment;filename=", "").trim();
            }
            FileUtils.download(model.getResponse(), buffer, fileName);
            return;
        } else {
            String fileName = dl.name();
            String filePath = dl.library();
            String file;
            if (model.parameterMapContainsKey(fileName)) {
                file = model.getRequestParameter(fileName);// 客户端传递的需要下载的文件名
            } else if (model.restMapContainsKey(fileName)) {
                file = model.getRestParam(fileName);
            } else {
                model.error(Code.REFUSED,"找不到文件下载接口的必要参数 \""+fileName + "\"","缺少接口参数..");
                return;
            }
            if (filePath.startsWith("abs:")) {
                path = filePath.substring(4) + file;//绝对路径写法
            } else if (filePath.startsWith("http:")) {//暴露一个网络上的文件库
                String url = filePath + file;
                byte[] buffer = HttpClientCall.getCallByte(url, new HashMap<>());
                FileUtils.download(model.getResponse(), buffer, file);
                return;
            } else {
                path = model.getRealPath(filePath) + file; // 默认认为文件在当前项目的docBase目录
            }
        }
        if (fis == null) {
            File f = new File(path);
            if (!f.exists()){
                model.error(Code.NOTFOUND,"在服务器上没有发现您想要下载的资源"+f.getName(),"没有对应的资源。");
                return;
            }

            fis = new FileInputStream(f);
            downName = f.getName();
        }
        FileUtils.download(model.getResponse(), fis, downName);
    }

    /**
     * 得到将要执行的Controller方法的参数列表的值
     *
     * @param model  Model对象
     * @param method 将要执行的Controller方法
     * @return 将要执行的Controller方法的参数列表
     * @throws IOException
     * @throws ServletException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object[] getControllerMethodParam(Model model, Class<?> controllerClass, Method method)
            throws IOException, InstantiationException, IllegalAccessException, FileTypeIllegalException, FileSizeCrossingException, FileUploadException, URISyntaxException, IllegalParameterException, RequestFileSizeCrossingException {
        //获取当前Controller方法参数列表所有的参数名
        String[] paramNames = ASMUtil.getMethodParamNames(method);
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        StringBuilder sb = new StringBuilder("[ URL-PARAMS ]\n");

        //得到@Upload文件操作执行后的String类型参数(文件名)
        moreUpload(model, method);
        if (model.getUploadFileMap().isEmpty()) {
            moreMultipartFil(model, method);   //得到类型为MultipartFile的参数
        }

        //得到参数列表中的所有pojo类型参数
        Map<String, Object> pojoMap = pojoParam(model, method);
        sb.append(pojoMap.isEmpty() ? "" : "Pojo-Params          : " + pojoMap.toString() + "\n").append("URL-Params           : \n");
        String paramName;

        //ControllerMethod参数赋值
        for (int i = 0; i < parameters.length; i++) {
            paramName = Mapping.getParamName(parameters[i], paramNames[i]);
            if (model.uploadFileMapContainsKey(paramName)) {//文件上传操作参数设置--@Upload
                File[] uploadFiles = model.getUploadFileArray(paramName);
                if (String.class == parameters[i].getType()) {
                    args[i] = uploadFiles[0].getName();
                } else if (String[].class == parameters[i].getType()) {
                    String[] uploadFileNames = new String[uploadFiles.length];
                    for (int x = 0; x < uploadFiles.length; x++) {
                        uploadFileNames[x] = uploadFiles[x].getName();
                    }
                    args[i] = uploadFileNames;
                } else if (File.class == parameters[i].getType()) {
                    args[i] = uploadFiles[0];
                } else if (File[].class == parameters[i].getType()) {
                    args[i] = uploadFiles;
                }
                continue;
            } else if (model.multipartFileMapContainsKey(paramName)) {//文件上传操作参数设置--MultipartFile
                MultipartFile[] multipartFiles = model.getMultipartFileArray(paramName);
                if (MultipartFile.class == parameters[i].getType()) {
                    args[i] = multipartFiles[0];
                } else if (MultipartFile[].class == parameters[i].getType()) {
                    args[i] = multipartFiles;
                }
                continue;
            } else if (parameters[i].isAnnotationPresent(CallResult.class) || parameters[i].isAnnotationPresent(CallBody.class)) {
                args[i] = httpClientParam(controllerClass, method, pojoMap, parameters[i], model, parameters, paramNames, Mapping.getParamName(parameters[i], paramNames[i]));
                continue;
            } else if (pojoMap.containsKey(paramName)) {
                args[i] = pojoMap.get(paramName);
                continue;
            } else if (ServletRequest.class.isAssignableFrom(parameters[i].getType())) {
                args[i] = model.getRequest();
                continue;
            } else if (HttpSession.class.isAssignableFrom(parameters[i].getType())) {
                args[i] = model.getSession();
                continue;
            } else if (ServletResponse.class.isAssignableFrom(parameters[i].getType())) {
                args[i] = model.getResponse();
                continue;
            } else if (ServletContext.class.isAssignableFrom(parameters[i].getType())) {
                args[i] = model.getServletContext();
                continue;
            } else if (Model.class.isAssignableFrom(parameters[i].getType())) {
                args[i] = model;
                continue;
            } else if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                RequestBody requestBody=parameters[i].getAnnotation(RequestBody.class);
                String paramValue;
                if(model.getParameterSize()==1){
                    paramValue=model.getDefaultParameterValue();
                }else{
                    paramValue = model.getRequestParameter(paramNames[i]);
                }
                if(requestBody.value()==Rest.JSON){
                    args[i] = lson.fromJson(parameters[i].getType(),paramValue);
                }else if (requestBody.value()==Rest.XML){
                    args[i]=lxml.fromXml(paramValue);
                }else{
                    continue;
                }
                continue;
            } else if (parameters[i].isAnnotationPresent(RestParam.class)) {
                RestParam rp = parameters[i].getAnnotation(RestParam.class);
                String restKey = rp.value();
                if (!model.restMapContainsKey(restKey)) {
                    throw new NotFindRequestException("缺少Rest请求参数：#{" + restKey + "} ,错误位置：" + method);
                }
                args[i] = JavaConversion.strToBasic(model.getRestMap().get(restKey), parameters[i].getType());
                sb.append("[Rest-Java] " + restKey + "=" + args[i] + "\n");
                continue;
            } else if (!parameters[i].isAnnotationPresent(CallResult.class) && !parameters[i].isAnnotationPresent(CallBody.class)) {
                String defparam = getRequeatParamDefValue(parameters[i]);
                if (parameters[i].getType().isArray() && parameters[i].getType().getClassLoader() == null) {
                    if (model.parameterMapContainsKey(paramName)) {
                        args[i] = JavaConversion.strToBasic(model.getRequestParameter(paramName),parameters[i].getType());
                        sb.append("[URL-Array] " + paramName + "=" + args[i] + "\n");
                        continue;
                    } else {
                        if (defparam == null) {
                            throw new NotFindRequestException("缺少请求参数：" + paramName + ",错误位置：" + method);
                        }
                        if ("null".equals(defparam)) {
                            args[i] = null;
                            sb.append("[Default-Array] " + paramName + "=" + args[i] + "\n");
                            continue;
                        } else {
                            args[i] = ApplicationBeans.createApplicationBeans().getBean(defparam);
                            sb.append("[Default-Array] " + paramName + "=" + args[i] + "\n");
                            continue;
                        }
                    }
                } else {
                    if (model.parameterMapContainsKey(paramName)) {
                        args[i] = JavaConversion.strToBasic(model.getRequestParameter(paramName),parameters[i].getType());
                        sb.append("[URL-Java] " + paramName + "=" + args[i] + "\n");
                        continue;
                    } else if (model.restMapContainsKey(paramName)) {
                        args[i] = model.getRestParam(paramName, parameters[i].getType());
                        sb.append("[Rest-Java] " + paramName + "=" + args[i] + "\n");
                        continue;
                    } else {
                        if (defparam == null) {
                            args[i] = null;
                        }
                        if ("null".equals(defparam)) {
                            args[i] = null;
                            sb.append("[Default-Java] " + paramName + "=" + args[i] + "\n");
                            continue;
                        } else if (parameters[i].getType().getClassLoader() == null) {
                            args[i] = JavaConversion.strToBasic(defparam, parameters[i].getType());
                            sb.append("[Default-Java] " + paramName + "=" + args[i] + "\n");
                            continue;
                        } else {
                            args[i] = ApplicationBeans.createApplicationBeans().getBean(defparam);
                            sb.append("[Default-Java] " + paramName + "=" + args[i]);
                            continue;
                        }
                    }
                }
            }
        }
        log.debug(sb.toString());

        //MD5算法加密Controller方法参数,以及格式校验
        Check check;
        MD5 md5;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(Check.class)) {
                check = parameters[i].getAnnotation(Check.class);
                if (!Regular.check(args[i].toString(), check.value())) {
                    throw new IllegalParameterException(model, paramNames[i], args[i].toString(), check.value());
                }
            }
            if (parameters[i].isAnnotationPresent(MD5.class)) {
                md5 = parameters[i].getAnnotation(MD5.class);
                args[i]=MD5Utils.md5(args[i].toString(),md5.salt(),md5.cycle(),md5.capital());
            }
        }
        return args;
    }

    /**
     * 为Controller方法中的pojo属性注入request域或RestMap中对应的值
     *
     * @param model
     * @param pojo
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object createObject(Model model, Object pojo) throws InstantiationException, IllegalAccessException {
        Field[] fields = pojo.getClass().getDeclaredFields();
        for (Field fie : fields) {
            fie.setAccessible(true);
            if (LuckyUtils.isJavaClass(fie.getType())) {
                if (fie.getType().isArray()) {
                    fie.set(pojo, model.getArray(fie.getName(), fie.getType()));
                } else {
                    if (model.parameterMapContainsKey(fie.getName())) {
                        fie.set(pojo, JavaConversion.strToBasic(model.getRequestParameter(fie.getName()), fie.getType()));
                    }
                    if (model.getRestMap().containsKey(fie.getName())) {
                        fie.set(pojo, model.getRestParam(fie.getName(), fie.getType()));
                    }
                }
            } else {
                Object object = fie.getType().newInstance();
                object = createObject(model, object);
                fie.set(pojo, object);
            }
        }
        return pojo;
    }


    /**
     * 得到RequestParam注解中def的值
     *
     * @param param
     * @return
     */
    private String getRequeatParamDefValue(Parameter param) {
        if (param.isAnnotationPresent(RequestParam.class)) {
            RequestParam rp = param.getAnnotation(RequestParam.class);
            String defValue = rp.def();
            if ("LCL*#*$FK%_58314@XFL_*#*LCL".equals(defValue)) {
                return null;
            }
            return defValue;
        } else {
            return null;
        }
    }

    /**
     * 判断本次请求的url参数是否可以赋值给该pojoClass对应对象的属性
     *
     * @param pojoClass
     * @param model
     * @return
     */
    private boolean canInjection(Class<?> pojoClass, Model model) {
        Field[] pojoFields = pojoClass.getDeclaredFields();
        for (Field field : pojoFields) {
            if (model.parameterMapContainsKey(field.getName()) || model.restMapContainsKey(field.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 得到调用远程接口的返回结果
     *
     * @param controllerClass 当前Controller的Class对象
     * @param method          当前Controller方法
     * @param pojoMap         pojoMap
     * @param currParameter   当前参数的Parameter
     * @param model           Model对象
     * @param parameters      Parameter数组
     * @param paramNames      方法参数名数组
     * @param noParam         接受参数
     * @return
     * @throws IOException
     */
    private Object httpClientParam(Class<?> controllerClass, Method method, Map<String, Object> pojoMap,
                                   Parameter currParameter, Model model, Parameter[] parameters,
                                   String[] paramNames, String noParam) throws IOException, IllegalAccessException, URISyntaxException {
        String callResult;
        String api = getCallApi(controllerClass, method);
        Map<String, Object> requestMap = getHttpClientRequestParam(method, model, pojoMap, parameters, paramNames, noParam);
        callResult = HttpClientCall.call(api, model.getRequestMethod(), requestMap);
        return callRestAndBody(currParameter, callResult);
    }

    /**
     * 得到远程服务的Url地址
     *
     * @param controllerClass 当前ControllerClass
     * @param method          当前的ControllerMethod
     * @return callapi
     */
    private String getCallApi(Class<?> controllerClass, Method method) {

        String controllerApi = Api.getApi(controllerClass.getAnnotation(Controller.class).callapi());
        String mappingValue = Mapping.getMappingDetails(method).value;

        //方法上不存在@CallApi注解，或者存在@CallApi注解但是value()为"",
        //此时使用ControllerApi与@xxxMapping的value()拼接得到完整的url
        if (!method.isAnnotationPresent(CallApi.class) || "".equals(method.getAnnotation(CallApi.class).value())) {
            if ("".equals(controllerApi)) {
                throw new NotFoundCallUrlException("找不到可使用的远程服务地址，错误的远程服务方法：" + method);
            }
            if (!controllerApi.endsWith("/")) {
                controllerApi += "/";
            }
            if (mappingValue.startsWith("/")) {
                mappingValue = mappingValue.substring(1);
            }
            return controllerApi + mappingValue;
        }
        CallApi callApi = method.getAnnotation(CallApi.class);
        String methodCallApi = callApi.value();

        //@CallApi注解中的value()为一个完整的url
        if (methodCallApi.startsWith("${") || methodCallApi.startsWith("http://") || methodCallApi.startsWith("https://")) {
            return Api.getApi(methodCallApi);
        }

        //@CallApi注解中的value()不是一个完整的Url，需要与ControllerApi进行拼接
        if ("".equals(controllerApi)) {
            throw new NotFoundCallUrlException("找不到可使用的远程服务地址，错误的远程服务方法：" + method);
        }
        if (!controllerApi.endsWith("/")) {
            controllerApi += "/";
        }
        if (methodCallApi.startsWith("/")) {
            methodCallApi = methodCallApi.substring(1);
        }
        return controllerApi + methodCallApi;
    }

    /**
     * 处理远程服务返回的数据，如果是CallBody则封装为JavaObject，为CallResult则返回字符串类型结果
     *
     * @param currParameter 当前方法对应的Parameter
     * @param strResult     远程服务响应的String类型结果
     * @return
     */
    private Object callRestAndBody(Parameter currParameter, String strResult) {
        if (currParameter.isAnnotationPresent(CallBody.class)) {
            return lson.fromJson(currParameter.getType(), strResult);
        }
        return JavaConversion.strToBasic(strResult, currParameter.getType());
    }

    /**
     * 得到访问远程服务需要的参数
     *
     * @param method     当前Controller方法
     * @param model      Model对象
     * @param parameters 参数列表对应的Parameter数组
     * @param paramNames 参数列表的参数名
     * @param noParam    接受响应结果的参数名
     * @return
     */
    private Map<String, Object> getHttpClientRequestParam(Method method, Model model, Map<String, Object> pojoMap,
                                                          Parameter[] parameters, String[] paramNames, String noParam) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();

        //获得参数列表中基本类型的接口参数
        String currParam;
        Class<?> paramClass;
        for (int i = 0; i < parameters.length; i++) {
            paramClass = parameters[i].getType();
            if (paramClass.getClassLoader() == null && !Collection.class.isAssignableFrom(paramClass) && !Map.class.isAssignableFrom(paramClass)) {
                currParam = Mapping.getParamName(parameters[i], paramNames[i]);
                if (!noParam.equals(currParam)) {
                    if (model.restMapContainsKey(currParam)) {
                        map.put(currParam, model.getRestParam(currParam));
                    } else if (model.parameterMapContainsKey(currParam)) {
                        map.put(currParam, model.getRequestParameter(currParam));
                    } else if (getRequeatParamDefValue(parameters[i]) != null) {
                        map.put(currParam, getRequeatParamDefValue(parameters[i]));
                    } else {
                        throw new NotFindRequestException("缺少请求参数：" + currParam + ",错误位置：" + method);
                    }
                }
            }
        }

        //获取参数列表中pojo类型中的接口参数
        Object pojo;
        Field[] fields;
        Object fieldValue;
        for (String key : pojoMap.keySet()) {
            pojo = pojoMap.get(key);
            fields = pojo.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                fieldValue = field.get(pojo);
                if (fieldValue != null) {
                    map.put(field.getName(), fieldValue.toString());
                }
            }
        }
        return map;
    }
}

class UploadCopy{
    private InputStream in;
    private File out;

    public UploadCopy(InputStream in, File out) {
        this.in = in;
        this.out = out;
    }

    public void copy() throws IOException {
        if(!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        FileUtils.copy(in,new BufferedOutputStream(new FileOutputStream(out)));
    }

    public int getFileSize() throws IOException {
        return in.available();
    }

    public static double getTotalSize(List<UploadCopy> list) throws IOException {
        long t=0;
        for (UploadCopy uploadCopy : list) {
           t+=uploadCopy.getFileSize();
        }
        return t;
    }
}
