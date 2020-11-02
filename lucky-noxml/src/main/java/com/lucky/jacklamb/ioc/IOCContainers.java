package com.lucky.jacklamb.ioc;

import com.lucky.jacklamb.annotation.ioc.Autowired;
import com.lucky.jacklamb.annotation.ioc.SSH;
import com.lucky.jacklamb.annotation.ioc.Value;
import com.lucky.jacklamb.exception.AutoInjectionException;
import com.lucky.jacklamb.exception.InjectionPropertiesException;
import com.lucky.jacklamb.exception.NotFindBeanException;
import com.lucky.jacklamb.expression.$Expression;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.scan.ScanFactory;
import com.lucky.jacklamb.servlet.core.Model;
import com.lucky.jacklamb.sqlcore.mapper.LuckyMapper;
import com.lucky.jacklamb.sqlcore.mapper.LuckyService;
import com.lucky.jacklamb.ssh.Remote;
import com.lucky.jacklamb.ssh.SSHClient;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.file.ini.INIConfig;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 扫描所有配置包，将所有的IOC组件都加载到相应的IOC容器中
 * @author DELL
 *
 */
public final class IOCContainers {
	
	private AspectAOP AspectIOC;
	private final static INIConfig ini=new INIConfig();
	private RepositoryIOC repositoryIOC;
	private ServiceIOC serviceIOC;
	private ComponentIOC appIOC;
	private ControllerIOC controllerIOC;
	private BeansIOC beansIOC;

	public BeansIOC getBeansIOC() {
		return beansIOC;
	}

	public void setBeansIOC(BeansIOC beansIOC) {
		this.beansIOC = beansIOC;
	}

	public AspectAOP getAspectIOC() {
		return AspectIOC;
	}

	public void setAspectIOC(AspectAOP AspectIOC) {
		this.AspectIOC = AspectIOC;
	}

	public RepositoryIOC getRepositoryIOC() {
		return repositoryIOC;
	}

	public void setRepositoryIOC(RepositoryIOC repositoryIOC) {
		this.repositoryIOC = repositoryIOC;
	}

	public ServiceIOC getServiceIOC() {
		return serviceIOC;
	}

	public void setServiceIOC(ServiceIOC serviceIOC) {
		this.serviceIOC = serviceIOC;
	}

	public ComponentIOC getAppIOC() {
		return appIOC;
	}

	public void setAppIOC(ComponentIOC appIOC) {
		this.appIOC = appIOC;
	}
	
	public void addComponent(String key,Object value) {
		this.appIOC.addBean(key, value);
	}

	public void removeComponent(String key){
		controllerIOC.getControllerIDS().remove(key);
		controllerIOC.getControllerMap().remove(key);
	}

	public ControllerIOC getControllerIOC() {
		return controllerIOC;
	}

	public void setControllerIOC(ControllerIOC controllerIOC) {
		this.controllerIOC = controllerIOC;
	}
	
	
	/**
	 *  初始化IOC容器
	 *  1.加载配置->2.得到所有增强->3.IOC+AOP->4.DI
	 */
	public void init() {
		
		//得到配置信息
		scanConfigToComponentIOC();
		
		//得到所有的增强
		AspectIOC=AspectAOP.getAspectIOC();
		
		//控制反转+动态代理(IOC+AOP)
		inversionOfControlAndAop();

		//@Bean 初始化由@Bean方法产出的IOC对象，并将他们放入IOC容器
		initBeansIOC();
		
		//依赖注入(DI)
		dependencyInjection();
		
	}
	
	/**
	 * 控制反转
	 */
	public void inversionOfControlAndAop() {
		initComponentIOC();
		initRepositoryIOC();
		initServiceIOC();
		initControllerIOC();
	}

	/**
	 * 依赖注入,执行代理
	 */
	public void dependencyInjection() {
		try {
			injection(appIOC.getBeanMap());
			injection(repositoryIOC.getRepositoryMap());
			injection(serviceIOC.getBeanMap());
			injection(controllerIOC.getControllerMap());
		} catch (IllegalArgumentException e) {
			throw new InjectionPropertiesException("属性注入异常，注入的属性与原属性类型不匹配....");
		} catch (IllegalAccessException e) {
			throw new InjectionPropertiesException("属性注入异常，没有权限访问该属性....");
		}
		
	}

	/**
	 * 得到有关包扫描的配置信息
	 */
	public void scanConfigToComponentIOC() {
		AppConfig.getAppConfig().getScanConfig();
	}
	
	/**
	 * 初始化Component组件
	 */
	public void initComponentIOC(){
		appIOC=new ComponentIOC();
		appIOC.addBean("LUCKY-COMPONENT-IOC",appIOC);
		appIOC.registered(ScanFactory.createScan().getComponentClass("component"));
	}
	
	/**
	 * 初始化Controller组件
	 */
	public void initControllerIOC(){
		controllerIOC=new ControllerIOC();
		controllerIOC.addControllerMap("LUCKY-CONTROLLER-IOC",controllerIOC);
		controllerIOC.registered(ScanFactory.createScan().getComponentClass("controller"));
		controllerIOC.methodHanderSetting();
	}
	
	/**
	 * 初始化Service组件
	 */
	public void initServiceIOC(){
		serviceIOC=new ServiceIOC();
		serviceIOC.addBean("LUCKY-SERVICE-IOC",serviceIOC);
		serviceIOC.registered(ScanFactory.createScan().getComponentClass("service"));
	}
	
	/**
	 * 初始化Repository组件
	 */
	public void initRepositoryIOC(){
		repositoryIOC=new RepositoryIOC();
		repositoryIOC.addRepositoryMap("LUCKY-REPOSITORY-IOC",repositoryIOC);
		repositoryIOC.registered(ScanFactory.createScan().getComponentClass("repository"));
	}

	private void initBeansIOC() {
		beansIOC=new BeansIOC();
		beansIOC.addBean("LUCKY-BEANS-IOC",beansIOC);
		beansIOC.registered(ScanFactory.createScan().getComponentClass("bean"));
	}


	/**
	 * 每次处理请求时为Controller注入Model、Request、Response和Session、Aplication对象属性
	 * @param object
	 * @param model
	 */
	public void autowReqAdnResp(Object object,Model model){
		Class<?> controllerClass=object.getClass();
		Field[] fields= ClassUtils.getAllFields(controllerClass);
		for(Field field:fields) {
			if(Model.class.isAssignableFrom(field.getType())) {
				FieldUtils.setValue(object,field,model);
			}else if(HttpSession.class.isAssignableFrom(field.getType())) {
				FieldUtils.setValue(object,field,model.getSession());
			}else if(ServletRequest.class.isAssignableFrom(field.getType())) {
				FieldUtils.setValue(object,field,model.getRequest());
			}else if(ServletResponse.class.isAssignableFrom(field.getType())) {
				FieldUtils.setValue(object,field,model.getResponse());
			}else if(ServletContext.class.isAssignableFrom(field.getType())) {
				FieldUtils.setValue(object,field,model.getServletContext());
			}else {
				continue;
			}
		}
	}
	
	/**
	 * 组件的属性注入(@Value和@Autowired)
	 * @param componentMap
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void injection(Map<String,Object> componentMap) throws IllegalArgumentException, IllegalAccessException {
		for(Entry<String,Object> entry:componentMap.entrySet()) {
			Object component=entry.getValue();
            injection(component);
		}
	}

	public Set<Class<?>> getWebSocketSet() {
		return new HashSet<Class<?>>(ScanFactory.createScan().getComponentClass("websocket"));
	}

	/**
	 * 依赖注入，为一个对象注入IOC容器中的属性
	 * @param component 某一个对象
	 */
    public static void injection(Object component){
        ApplicationBeans beans=ApplicationBeans.createApplicationBeans();
        Autowired auto;
        Value value;
        SSH ssh;
        Remote remote;
        Class<?> componentClass=component.getClass();
        List<Field> fields= Stream.of(ClassUtils.getAllFields(componentClass))
				.filter(f-> !Modifier.isFinal(f.getModifiers())).collect(Collectors.toList());
        for(Field field:fields) {
            Class<?> fieldClass=field.getType();
            if(field.isAnnotationPresent(Autowired.class)) {
                auto=field.getAnnotation(Autowired.class);
                String auval = auto.value();
                if("".equals(auval)) {
					//类型扫描
					try {
						if(LuckyService.class.isAssignableFrom(componentClass)&&fieldClass== LuckyMapper.class){
							Class<?> aClass = ClassUtils.getGenericType(getCurrClass(componentClass).getGenericSuperclass())[0];
							FieldUtils.setValue(component,field,beans.getBean(aClass));
						}else{
							FieldUtils.setValue(component,field,beans.getBean(fieldClass));
						}
					}catch (NotFindBeanException e){
						throw new AutoInjectionException(getCurrClass(componentClass),field,e);
					}

                }else if(auval.contains("${")&&auval.contains("}")){
                    String key=auval.substring(2,auval.length()-1);
                    if(key.startsWith("S:")){
                    	FieldUtils.setValue(component,field,ini.getObject(fieldClass,key.substring(2)));
                    }else{
						FieldUtils.setValue(component,field, $Expression.translation(auval,fieldClass));
                    }
                }else{
					//id注入
					try {
						FieldUtils.setValue(component,field, beans.getBean(auto.value()));
					}catch (NotFindBeanException e){
						throw new AutoInjectionException(getCurrClass(componentClass),field,e);
					}
                }
            }else if(field.isAnnotationPresent(Value.class)) {
                value=field.getAnnotation(Value.class);
                String[] val = value.value();
                if(val.length==0) {//类型扫描
                	try {
						FieldUtils.setValue(component,field,beans.getBean(fieldClass));
					}catch (NotFindBeanException e){
						throw new AutoInjectionException(getCurrClass(componentClass),field,e);
					}
                }else {
                    if(fieldClass.isArray()) {//基本类型的数组类型
						FieldUtils.setValue(component,field,JavaConversion.strArrToBasicArr(val, fieldClass));
                    }else if(List.class.isAssignableFrom(fieldClass)) {//List类型
						try {
							FieldUtils.setValue(component,field,getList(beans,field,val));
						}catch (NotFindBeanException e){
							if(componentClass.getSimpleName().equals("$$EnhancerByCGLIB$$")){
								throw new AutoInjectionException(componentClass.getSuperclass(),field,e);
							}
							throw new AutoInjectionException(componentClass,field,e);
						}
                    }else if(Set.class.isAssignableFrom(fieldClass)) {//Set类型
                    	try {
							FieldUtils.setValue(component,field,new HashSet<>(getList(beans,field,val)));
						}catch (NotFindBeanException e){
							if(componentClass.getSimpleName().contains("$$EnhancerByCGLIB$$")){
								throw new AutoInjectionException(componentClass.getSuperclass(),field,e);
							}
							throw new AutoInjectionException(componentClass,field,e);
						}
                    }else if(Map.class.isAssignableFrom(fieldClass)) {//Map类型
                        Map<Object,Object> map=new HashMap<>();
                        String[] fx=FieldUtils.getStrGenericType(field);
                        boolean one=fx[0].endsWith("$ref");
                        boolean two=fx[1].endsWith("$ref");
                        if(one&&two) {//K-V都不是基本类型
                            for(String z:val) {
                                String[] kv=z.split(":");
								try {
									map.put(beans.getBean(kv[0]), beans.getBean(kv[1]));
								}catch (NotFindBeanException e) {
									throw new AutoInjectionException(getCurrClass(componentClass), field, e);
								}
                            }
                        }else if(one&&!two) {//V是基本类型
                            for(String z:val) {
                                String[] kv=z.split(":");
								try {
									map.put(beans.getBean(kv[0]), JavaConversion.strToBasic(kv[1], fx[1]));
								}catch (NotFindBeanException e){
									throw new AutoInjectionException(getCurrClass(componentClass),field,e);
								}
                            }
                        }else if(!one&&two) {//K是基本类型
                            for(String z:val) {
                                String[] kv=z.split(":");
								try {
									map.put(JavaConversion.strToBasic(kv[0], fx[0]),beans.getBean(kv[1]));
								}catch (NotFindBeanException e){
									throw new AutoInjectionException(getCurrClass(componentClass),field,e);
								}

                            }
                        }else {//K-V都是基本类型
                            for(String z:val) {
                                String[] kv=z.split(":");
                                map.put(JavaConversion.strToBasic(kv[0], fx[0]), JavaConversion.strToBasic(kv[1], fx[1]));
                            }
                        }
						FieldUtils.setValue(component,field, map);
                    }else {//自定义的基本类型
						FieldUtils.setValue(component,field, JavaConversion.strToBasic(val[0], fieldClass.getSimpleName()));
                    }
                }
            }else if(field.isAnnotationPresent(SSH.class)&& SSHClient.class.isAssignableFrom(field.getType())){
                ssh=field.getAnnotation(SSH.class);
                String section=ssh.value();
                remote=ini.getObject(Remote.class,section);
                FieldUtils.setValue(component,field,new SSHClient(remote));
            }
        }
    }

    private static Class<?> getCurrClass(Class<?> aClass){
    	return aClass.getSimpleName().contains("$$EnhancerByCGLIB$$")?aClass.getSuperclass():aClass;
	}

    private static List<Object> getList(ApplicationBeans beans,Field field,String[] val){
		List<Object> list=new ArrayList<>();
		String fx= FieldUtils.getStrGenericType(field)[0];
		if(fx.endsWith("$ref")) {
			for(String z:val) {
				list.add(beans.getBean(z));
			}
		}else {
			for(String z:val) {
				list.add(JavaConversion.strToBasic(z, fx));
			}
		}
		return list;
	}
}


