package com.lucky.jacklamb.mapping;

import com.lucky.jacklamb.annotation.mvc.Download;
import com.lucky.jacklamb.annotation.mvc.RequestParam;
import com.lucky.jacklamb.annotation.mvc.RestParam;
import com.lucky.jacklamb.annotation.mvc.Upload;
import com.lucky.jacklamb.aop.util.ASMUtil;
import com.lucky.jacklamb.exception.FileSizeCrossingException;
import com.lucky.jacklamb.exception.FileTypeIllegalException;
import com.lucky.jacklamb.exception.NotFindRequestException;
import com.lucky.jacklamb.file.MultipartFile;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.servlet.Model;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.LuckyUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class AnnotationOperation {
	
	private static Logger log=Logger.getLogger(AnnotationOperation.class);

	/**
	 * 根据参数得到具体的MultipartFile
	 * 
	 * @param model  Model对象
	 * @param formName  formName 表单上<-input type="file"->的"name"属性值
	 * @return 返回MultipartFile对象
	 * @throws IOException
	 * @throws ServletException
	 */
	private MultipartFile uploadMutipar(Model model, String formName) throws IOException, ServletException {
		Part part = model.getRequest().getPart(formName);
		String projectPath = model.getRealPath("");
		return new MultipartFile(part, projectPath);
	}

	/**
	 * 基于MultipartFile的多文件上传
	 * 
	 * @param model Model对象
	 * @param method 将要执行的Controller方法
	 * @return 由Controller方法参数名和其对应的值所组成的Map(针对MultipartFile)
	 * @throws IOException
	 * @throws ServletException
	 */
	private Map<String, MultipartFile> moreUploadMutipar(Model model, Method method)
			throws IOException, ServletException {
		Map<String, MultipartFile> map = new HashMap<>();
		Parameter[] parameters = method.getParameters();
		String[] paramNames=ASMUtil.getMethodParamNames(method);
		try {
			String paramName;
			for(int i=0;i<parameters.length;i++) {
				if (MultipartFile.class.isAssignableFrom(parameters[i].getType())) {
					paramName=getParamName(parameters[i],paramNames[i]);
					map.put(paramName, uploadMutipar(model, paramName));
				}
			}
		}catch (NullPointerException e) {
			List<String> paramlist=new ArrayList<>();
			for(int i=0;i<parameters.length;i++) {
				if (MultipartFile.class.isAssignableFrom(parameters[i].getType())) {
					paramlist.add(getParamName(parameters[i],paramNames[i]));
				}
			}
			setMultipartFileMap(model,map,paramlist);
		}
		return map;
	}
	
	/**
	 * MultipartFile的多文件上传,基于Apache [commons-fileupload-1.3.1.jar  commons-io-2.4.jar]
	 * @param model Model对象
	 * @param resultsMap 参数名与MultipartFile对象组成的map
	 * @param paramlist MultipartFile类型参数名组成的集合
	 */
	public void setMultipartFileMap(Model model,Map<String, MultipartFile> resultsMap,List<String> paramlist) {
		try{
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("UTF-8"); 
			if(!ServletFileUpload.isMultipartContent(model.getRequest())){
				return;
			}
			List<FileItem> list = upload.parseRequest(model.getRequest());
			String field="";
			for(FileItem item : list){
				if(!item.isFormField()){
					field=item.getFieldName();
					String filename=item.getName();
					if(paramlist.contains(field)) {
						InputStream in = item.getInputStream();
						String suffix=filename.substring(filename.lastIndexOf("."));
						MultipartFile mfp=new MultipartFile(in,model.getRealPath("/"),suffix);
						resultsMap.put(field, mfp);
					}
				}else {
					if(!model.parameterMapContainsKey(item.getFieldName())) {
						String[] values= {new String(item.get(),"UTF-8")};
						model.addParameter(item.getFieldName(),values);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * --@Upload注解方式的多文件上传-基于Servlet3的Part对象和@MultipartConfig注解的实现
	 * 
	 * @param model  Model对象
	 * @param formName 表单上<-input type="file"->的"name"属性值
	 * @param path 要上传到服务器的哪个文件夹？
	 * @param type 允许上传的文件类型
	 * @param maxSize 允许上传文件的最大大小
	 * @return 上传后服务器上的文件名
	 * @throws IOException
	 * @throws ServletException
	 */
	private String upload(Model model, String formName, String path, String type, int maxSize)
			throws IOException, ServletException {
		Part part = model.getRequest().getPart(formName);
		String disposition = part.getHeader("Content-Disposition");
		if (disposition.contains(".")) {
			// 获得文件后缀名
			String suffix = disposition.substring(disposition.lastIndexOf("."));
			if (!"".equals(type)) {
				if (!type.toLowerCase().contains(suffix.toLowerCase())) {
					throw new FileTypeIllegalException("上传的文件格式" + suffix + "不合法！合法的文件格式为：" + type);
				}
			}
			String filename = UUID.randomUUID().toString()+ suffix;
			InputStream is = part.getInputStream();
			FileInputStream fis = (FileInputStream) is;
			if (maxSize != 0) {
				int size = fis.available();
				int filesize = size / 1024;
				if (filesize > maxSize) {
					throw new FileSizeCrossingException("上传文件的大小(" + filesize + "kb)超出设置的最大值" + maxSize + "kb");
				}
			}
			String serverpath = model.getRealPath(path);
			File file = new File(serverpath);
			if (!file.isDirectory()) {
				file.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(serverpath + "/" + filename);
			byte[] bty = new byte[1024];
			int length = 0;
			while ((length = is.read(bty)) != -1) {
				fos.write(bty, 0, length);
			}
			fos.close();
			is.close();
			return filename;
		} else {
			throw new FileTypeIllegalException("上传的文件格式不正确，系统无法识别！file："+disposition);
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
	private Map<String, String> moreUpload(Model model, Method method) throws IOException, ServletException{
		Map<String, String> fileMap = new HashMap<String, String>();
		if (method.isAnnotationPresent(Upload.class)) {
			Upload upload = method.getAnnotation(Upload.class);
			String[] files = upload.names();
			String[] savePaths = upload.filePath();
			String types = upload.type();
			int maxSize = upload.maxSize();
			try {
				if (savePaths.length == 1) {
					for (String str : files) {
						fileMap.put(str, upload(model, str, savePaths[0], types, maxSize));
					}
				} else {
					int x = 0;
					for (String str : files) {
						fileMap.put(str, upload(model, str, savePaths[x++], types, maxSize));
					}
				}	
			}catch(NullPointerException | IllegalStateException e) {
				Map<String, String> fieldAndFolder=new HashMap<>();
				if(savePaths.length == 1) {
					for(String file:files)
						fieldAndFolder.put(file, savePaths[0]);
				}else {
					for(int i=0;i<savePaths.length;i++)
						fieldAndFolder.put(files[i], savePaths[i]);
				}
				upload(model,fileMap,fieldAndFolder,types,maxSize);
			}

		}
		return fileMap;
	}
	
	/**
	 * --@Upload注解方式的多文件上传-基于Apache [commons-fileupload-1.3.1.jar  commons-io-2.4.jar]
	 * 适配内嵌tomcat的文件上传与下载操作
	 * @param model  Model对象
	 * @param resultsMap 文件名与表单name属性所组成的Map
	 * @param fieldAndFolder name与folder组成的Map
	 * @param type 允许上传的文件类型
	 * @param maxSize 允许上传文件的最大大小
	 */
	public void upload(Model model,Map<String, String> resultsMap,Map<String,String> fieldAndFolder,String type, int maxSize) {
		String savePath = model.getRealPath("/");
		try{
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("UTF-8"); 
			if(!ServletFileUpload.isMultipartContent(model.getRequest())){
				return;
			}
			List<FileItem> list = upload.parseRequest(model.getRequest());
			String field="";
			for(FileItem item : list){
				if(!item.isFormField()){
					field=item.getFieldName();
					if(fieldAndFolder.containsKey(field)) {
						String filename = item.getName();
						String suffix=filename.substring(filename.lastIndexOf("."));
						if(!"".equals(type)&&!type.toLowerCase().contains(suffix.toLowerCase()))
								throw new FileTypeIllegalException("上传的文件格式" + suffix + "不合法！合法的文件格式为：" + type);
						String pathSave=fieldAndFolder.get(field);
						File file = new File(savePath+pathSave);
						filename = UUID.randomUUID().toString()+suffix;
						InputStream in = item.getInputStream();
						if (maxSize != 0) {
							int size = in.available();
							int filesize = size / 1024;
							if (filesize > maxSize) {
								throw new FileSizeCrossingException("上传文件的大小(" + filesize + "kb)超出设置的最大值" + maxSize + "kb");
							}
						}
						
						if (!file.isDirectory()) {
							file.mkdirs();
						}
						
						if(filename==null || filename.trim().equals("")){
							continue;
						}

						FileOutputStream out = new FileOutputStream(savePath+pathSave + "/" + filename);
						byte buffer[] = new byte[1024];
						int len = 0;
						while((len=in.read(buffer))>0){
							out.write(buffer, 0, len);
							out.flush();
						}
						in.close();
						out.close();
						item.delete();
						resultsMap.put(field, filename);
					}
				}else {
					if(!model.parameterMapContainsKey(item.getFieldName())) {
						String[] values= {new String(item.get(),"UTF-8")};
						model.addParameter(item.getFieldName(),values);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * 返回Controller方法参数名与参数值所组成的Map(针对Pojo类型的参数)
	 * 
	 * @param model  Model对象
	 * @param method 将要执行的Controller方法
	 * @param uploadMap 上传后的文件名与表单name属性所组成的Map
	 * @return Controller方法参数名与参数值所组成的Map(针对Pojo类型的参数)
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws ServletException
	 */
	private Map<String, Object> pojoParam(Model model, Method method, Map<String, String> uploadMap)
			throws InstantiationException, IllegalAccessException{
		Map<String, Object> map = new HashMap<>();
		Parameter[] parameters = method.getParameters();
		String[] paramNames=ASMUtil.getMethodParamNames(method);
		int i=0;
		for (Parameter param : parameters) {
			
			if (!MultipartFile.class.isAssignableFrom(param.getType()) 
					&& param.getType().getClassLoader() != null
					&& !ServletRequest.class.isAssignableFrom(param.getType())
					&& !ServletResponse.class.isAssignableFrom(param.getType())
					&& !HttpSession.class.isAssignableFrom(param.getType())
					&& !ServletContext.class.isAssignableFrom(param.getType())
					&& !Model.class.isAssignableFrom(param.getType()) && canInjection(param.getType(),model)) {
				Class<?> pojoclzz = param.getType();
				Object pojo = pojoclzz.newInstance();
				Field[] fields = pojoclzz.getDeclaredFields();
				createObject(model, pojo);
				for (Field fi : fields) {
					fi.setAccessible(true);
					Object fi_obj = fi.get(pojo);
					// pojo中含有@Upload返回的文件名
					if (uploadMap.containsKey(fi.getName()) && fi_obj == null)
						fi.set(pojo, uploadMap.get(fi.getName()));
				}
				map.put(getParamName(param,paramNames[i]), pojo);
			}
			i++;
		}
		return map;

	}

	/**
	 * 文件下载操作@Download
	 * 
	 * @param model Model对象
	 * @param method 将要执行的Controller方法
	 * @throws IOException
	 */
	public void download(Model model, Method method) throws IOException {
		Download dl = method.getAnnotation(Download.class);
		InputStream fis = null;
		String downName = null;
		String path="";
		if(!"".equals(dl.path())) {
			path=dl.path();
		}else if(!"".equals(dl.docPath())){
			path=model.getRealPath("")+dl.docPath();
		}else if(!"".equals(dl.url())){
			downName=dl.url();
			downName=downName.substring(downName.lastIndexOf("."));
			downName=UUID.randomUUID().toString()+downName;
			HttpURLConnection httpurlcon=(HttpURLConnection)new URL(dl.url()).openConnection();
			httpurlcon.connect();
			fis=httpurlcon.getInputStream();
		}else {
			String fileName = dl.name();
			String filePath = dl.folder();
			String file;
			if(model.parameterMapContainsKey(fileName))// 客户端传递的需要下载的文件名
				file = model.getRequestPrarmeter(fileName);
			else if(model.restMapContainsKey(fileName))
				file = model.getRestParam(fileName);
			else
				throw new RuntimeException("找不到必要属性\""+fileName+"\"");
			path = model.getRealPath(filePath) + file; // 默认认为文件在当前项目的根目录
		}
		if(fis==null) {
			File f=new File(path);
			if(!f.exists())
				throw new RuntimeException("找不到文件,无法完成下载操作！"+path);
			fis = new FileInputStream(f);
			downName=f.getName();
		}
		model.getResponse().setCharacterEncoding("utf-8");
		model.getResponse().setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(downName,"UTF-8"));
		ServletOutputStream out = model.getResponse().getOutputStream();
		byte[] bt = new byte[1024];
		int length;
		while ((length = fis.read(bt)) != -1) {
			out.write(bt, 0, length);
			out.flush();
		}
		out.close();
		fis.close();
	}

	/**
	 * 得到将要执行的Controller方法的参数列表的值
	 * 
	 * @param model Model对象
	 * @param method 将要执行的Controller方法
	 * @return 将要执行的Controller方法的参数列表
	 * @throws IOException
	 * @throws ServletException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Object getControllerMethodParam(Model model, Method method)
			throws IOException, ServletException, InstantiationException, IllegalAccessException {
		String[] paramNames=ASMUtil.getMethodParamNames(method);
		Parameter[] parameters = method.getParameters();
		Object[] args = new Object[parameters.length];
		StringBuilder sb =new StringBuilder("[ URL-PARAMS ]\n");
		Map<String, String> uploadMap = moreUpload(model, method);
		sb.append(uploadMap.isEmpty()?"":"@Upload-Params       : "+uploadMap.toString()+"\n");
		Map<String, MultipartFile> multiUploadMap = moreUploadMutipar(model, method);
		sb.append(multiUploadMap.isEmpty()?"":"MultipartFile-Params : "+multiUploadMap.toString()+"\n");
		Map<String, Object> pojoMap = pojoParam(model, method, uploadMap);
		sb.append(pojoMap.isEmpty()?"":"Pojo-Params          : "+pojoMap.toString()+"\n").append("URL-Params           : \n");
		String paramName;
		for (int i = 0; i < parameters.length; i++) {
			paramName=getParamName(parameters[i],paramNames[i]);
			if (uploadMap.containsKey(paramName)
					&& String.class.isAssignableFrom(parameters[i].getType())) {
				args[i] = uploadMap.get(paramName);
			} else if (multiUploadMap.containsKey(paramName)
					&& MultipartFile.class.isAssignableFrom(parameters[i].getType())) {
				args[i] = multiUploadMap.get(paramName);
			} else if (pojoMap.containsKey(paramName)) {
				args[i] = pojoMap.get(paramName);
			} else if (ServletRequest.class.isAssignableFrom(parameters[i].getType())) {
				args[i] = model.getRequest();
			} else if (HttpSession.class.isAssignableFrom(parameters[i].getType())) {
				args[i] = model.getSession();
			} else if (ServletResponse.class.isAssignableFrom(parameters[i].getType())) {
				args[i] = model.getResponse();
			}else if (ServletContext.class.isAssignableFrom(parameters[i].getType())) {
				args[i] = model.getServletContext();
			} else if (Model.class.isAssignableFrom(parameters[i].getType())) {
				args[i] = model;
			} else if (parameters[i].isAnnotationPresent(RestParam.class)) {
				RestParam rp = parameters[i].getAnnotation(RestParam.class);
				String restKey = rp.value();
				if (!model.restMapContainsKey(restKey))
					throw new NotFindRequestException("缺少请求参数：" + restKey+",错误位置："+method);
				args[i] = JavaConversion.strToBasic(model.getRestMap().get(restKey), parameters[i].getType());
				sb.append("[Rest-Java] "+restKey+"="+args[i]+"\n");
			} else {
				String defparam = getRequeatParamDefValue(parameters[i]);
				if (parameters[i].getType().isArray() && parameters[i].getType().getClassLoader() == null) {
					if (model.parameterMapContainsKey(paramName)) {
						args[i] = model.getArray(paramName, parameters[i].getType());
						sb.append("[URL-Array] "+paramName+"="+args[i]+"\n");
					} else {
						if (defparam == null)
							throw new NotFindRequestException("缺少请求参数：" + paramName+",错误位置："+method);
						if("null".equals(defparam)) {
							args[i]=null;
							sb.append("[Default-Array] "+paramName+"="+args[i]+"\n");							
						}else {
							args[i] = ApplicationBeans.createApplicationBeans().getBean(defparam);
							sb.append("[Default-Array] "+paramName+"="+args[i]+"\n");
						}
					}
				} else {
					if (model.parameterMapContainsKey(paramName)) {
						args[i] = model.getArray(paramName, parameters[i].getType())[0];
						sb.append("[URL-Java] "+paramName+"="+args[i]+"\n");
					} else if (model.restMapContainsKey(paramName)) {
						args[i] = model.getRestParam(paramName, parameters[i].getType());
						sb.append("[Rest-Java] "+paramName+"="+args[i]+"\n");
					} else {
						if (defparam == null)
							throw new NotFindRequestException("缺少请求参数：" + paramName+",错误位置："+method);
						if("null".equals(defparam)) {
							args[i]=null;
							sb.append("[Default-Java] "+paramName+"="+args[i]+"\n");
						}else if (parameters[i].getType().getClassLoader() == null) {
							args[i] = JavaConversion.strToBasic(defparam, parameters[i].getType());
							sb.append("[Default-Java] "+paramName+"="+args[i]+"\n");
						} else {
							args[i] = ApplicationBeans.createApplicationBeans().getBean(defparam);
							sb.append("[Default-Java] "+paramName+"="+args[i]);
						}
					}
				}
			}
		}
		log.debug(sb.toString());
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
					if (model.getArray(fie.getName(), fie.getType()) != null) {
						fie.set(pojo, model.getArray(fie.getName(), fie.getType())[0]);
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
	 * 得到一个参数的标记参数名
	 * 
	 * @param param
	 * @return
	 */
	private String getParamName(Parameter param,String paramName) {
		if (param.isAnnotationPresent(RequestParam.class)) {
			RequestParam rp = param.getAnnotation(RequestParam.class);
			return rp.value();
		} else {
			return paramName;
		}
	}

	/**
	 * 得到RequestParam注解中def的值
	 * @param param
	 * @return
	 */
	private String getRequeatParamDefValue(Parameter param) {
		if (param.isAnnotationPresent(RequestParam.class)) {
			RequestParam rp = param.getAnnotation(RequestParam.class);
			String defValue = rp.def();
			if ("LCL*#*$FK%_58314@XFL_*#*LCL".equals(defValue))
				return null;
			return defValue;
		} else {
			return null;
		}
	}
	
	/**
	 * 判断本次请求的url参数是否可以赋值给该pojoClass对应对象的属性
	 * @param pojoClass
	 * @param model
	 * @return
	 */
	private boolean canInjection(Class<?> pojoClass,Model model) {
		Field[] pojoFields=pojoClass.getDeclaredFields();
		for(Field field:pojoFields) {
			if(model.parameterMapContainsKey(field.getName())||model.restMapContainsKey(field.getName()))
				return true;
		}
		return false;
	}
}
