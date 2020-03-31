package com.lucky.jacklamb.ioc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lucky.jacklamb.annotation.ioc.Repository;
import com.lucky.jacklamb.annotation.orm.mapper.Mapper;
import com.lucky.jacklamb.aop.util.PointRunFactory;
import com.lucky.jacklamb.exception.NotAddIOCComponent;
import com.lucky.jacklamb.exception.NotFindBeanException;
import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.SqlCoreFactory;
import com.lucky.jacklamb.sqlcore.c3p0.DataSource;
import com.lucky.jacklamb.sqlcore.c3p0.ReadIni;
import com.lucky.jacklamb.utils.LuckyUtils;

public class RepositoryIOC extends ComponentFactory {
	
	private static Logger log=Logger.getLogger(RepositoryIOC.class);

	private Map<String, Object> repositoryMap;

	private List<String> repositoryIDS;

	private Map<String, Object> mapperMap;
	
	private Map<String,String> mapperTtypeMap;

	private List<String> mapperIDS;
	
	
	public RepositoryIOC() {
		repositoryMap=new HashMap<>();
		repositoryIDS=new ArrayList<>();
		mapperMap=new HashMap<>();
		mapperTtypeMap=new HashMap<>();
		mapperIDS=new ArrayList<>();
	}

	public Object getMaRepBean(String id) {
		if (containIdByMapper(id))
			return mapperMap.get(id);
		else if (containIdByRepository(id))
			return repositoryMap.get(id);
		else
			throw new NotFindBeanException("在Repository和Mapper(ioc)容器中找不到ID为--" + id + "--的Bean...");
	}

	public boolean containId(String id) {
		return containIdByMapper(id) || containIdByRepository(id);
	}

	public boolean containIdByMapper(String id) {
		return mapperIDS.contains(id);
	}

	public boolean containIdByRepository(String id) {
		return repositoryIDS.contains(id);
	}

	public Map<String, Object> getRepositoryMap() {
		return repositoryMap;
	}

	public void setRepositoryMap(Map<String, Object> repositoryMap) {
		this.repositoryMap = repositoryMap;
	}

	public void addRepositoryMap(String daoId, Object daoObj) {
		if(containId(daoId))
			throw new NotAddIOCComponent("Repository(ioc)容器中已存在ID为--"+daoId+"--的组件，无法重复添加（您可能配置了同名的@Repository组件，这将会导致异常的发生！）......");
		repositoryMap.put(daoId, daoObj);
		addRepositoryIDS(daoId);
	}

	public List<String> getRepositoryIDS() {
		return repositoryIDS;
	}

	public void setRepositoryIDS(List<String> repositoryIDS) {
		this.repositoryIDS = repositoryIDS;
	}

	public void addRepositoryIDS(String repositoryID) {
		repositoryIDS.add(repositoryID);
	}

	public Map<String, String> getMapperTtypeMap() {
		return mapperTtypeMap;
	}

	public Map<String, Object> getMapperMap() {
		return mapperMap;
	}

	public void setMapperMap(Map<String, Object> mapperMap) {
		this.mapperMap = mapperMap;
	}

	public void addMapperMap(String mapperID, Object mapperObj) {
		if(containId(mapperID))
			throw new NotAddIOCComponent("Mapper(ioc)容器中已存在ID为--"+mapperID+"--的组件，无法重复添加......");
		mapperMap.put(mapperID, mapperObj);
		addMapperIDS(mapperID);
		mapperTtypeMap.put(mapperID, mapperObj.getClass().getName());
	}

	public List<String> getMapperIDS() {
		return mapperIDS;
	}

	public void setMapperIDS(List<String> mapperIDS) {
		this.mapperIDS = mapperIDS;
	}

	public void addMapperIDS(String mapperID) {
		mapperIDS.add(mapperID);
	}

	/**
	 * 加载Repository组件
	 * 
	 * @param repositoryClass
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public void initRepositoryIOC(List<Class<?>> repositoryClass) throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		boolean first = true;
		String beanID;
		for (Class<?> repository : repositoryClass) {
			if (repository.isAnnotationPresent(Repository.class)) {
				Repository rep = repository.getAnnotation(Repository.class);
				if (!"".equals(rep.value()))
					beanID=rep.value();
				else
					beanID=LuckyUtils.TableToClass1(repository.getSimpleName());
				Object aspect = PointRunFactory.Aspect(AspectAOP.getAspectIOC().getAspectMap(), "repository", beanID, repository);
				addRepositoryMap(beanID, aspect);
				log.info("@Repository      =>   [id="+beanID+" class="+aspect+"]");
				
			} else if (repository.isAnnotationPresent(Mapper.class)) {
				if (first) {
					List<DataSource> datalist=ReadIni.getAllDataSource();
					for(DataSource data:datalist) {
						SqlCore sqlCore=SqlCoreFactory.createSqlCore(data.getName());
						beanID="lucky#$jacklamb#$&58314@SqlCore-"+data.getName();
						addRepositoryMap(beanID, sqlCore);
						log.info("@Repository      =>   [type=SqlCore id="+beanID+" class="+sqlCore+"]");
					}
					first = false;
				}
				Mapper mapper = repository.getAnnotation(Mapper.class);
				beanID="lucky#$jacklamb#$&58314@SqlCore-"+mapper.dbname();
				SqlCore currSqlCore=(SqlCore) getMaRepBean(beanID);
				if (!"".equals(mapper.id())) {
					Object mapper2 = currSqlCore.getMapper(repository);
					addMapperMap(mapper.id(), mapper2);
					log.info("@Mapper          =>   [type=Mapper id="+mapper.id()+" class="+mapper2.getClass()+"]");
				}else {
					beanID = LuckyUtils.TableToClass1(repository.getSimpleName());
					Object mapper2 = currSqlCore.getMapper(repository);
					addMapperMap(beanID, mapper2);
					log.info("@Mapper          =>   [type=Mapper id="+beanID+" class="+mapper2.getClass()+"]");
				}
			}
		}
	}
}
