package org.frameworkset.elasticsearch;

import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.DefaultApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ElasticSearchHelper {
	protected static DefaultApplicationContext context = null;
	public static final String DEFAULT_SEARCH = "elasticSearch";
	protected static ElasticSearch elasticSearchSink = null;
	private static boolean inited;

	private static Method bootMethod;
	static {
		try {
			Class booterClass = Class.forName("org.frameworkset.elasticsearch.boot.ElasticSearchConfigBoot");
			bootMethod = booterClass.getMethod("boot");
		} catch (ClassNotFoundException e) {

		} catch (NoSuchMethodException e) {

		}
	}
	private static Map<String,ElasticSearch> elasticSearchMap = new HashMap<String,ElasticSearch>();
	public ElasticSearchHelper() {
		// TODO Auto-generated constructor stub
	}
	/**
	 *  <property name="elasticsearch.client" value="${elasticsearch.client:restful}">
	 *                 <description> <![CDATA[ 客户端类型:transport，restful ]]></description>
	 *             </property>
	 *
	 *             <property name="elasticUser" value="${elasticUser:}">
	 *                 <description> <![CDATA[ 认证用户 ]]></description>
	 *             </property>
	 *
	 *             <property name="elasticPassword" value="${elasticPassword:}">
	 *                 <description> <![CDATA[ 认证口令 ]]></description>
	 *             </property>
	 *             <!--<property name="elasticsearch.hostNames" value="${elasticsearch.hostNames}">
	 *                 <description> <![CDATA[ 指定序列化处理类，默认为kafka.serializer.DefaultEncoder,即byte[] ]]></description>
	 *             </property>-->
	 *
	 *             <property name="elasticsearch.rest.hostNames" value="${elasticsearch.rest.hostNames:127.0.0.1:9200}">
	 *                 <description> <![CDATA[ rest协议地址 ]]></description>
	 *             </property>
	 *
	 *
	 *             <property name="elasticsearch.dateFormat" value="${elasticsearch.dateFormat:yyyy.MM.dd}">
	 *                 <description> <![CDATA[ 索引日期格式]]></description>
	 *             </property>
	 *             <property name="elasticsearch.timeZone" value="${elasticsearch.timeZone:Asia/Shanghai}">
	 *                 <description> <![CDATA[ 时区信息]]></description>
	 *             </property>
	 *
	 *             <property name="elasticsearch.ttl" value="${elasticsearch.ttl:2d}">
	 *                 <description> <![CDATA[ ms(毫秒) s(秒) m(分钟) h(小时) d(天) w(星期)]]></description>
	 *             </property>
	 *
	 *             <property name="elasticsearch.showTemplate" value="${elasticsearch.showTemplate:false}">
	 *                 <description> <![CDATA[ query dsl脚本日志调试开关，与log info级别日志结合使用]]></description>
	 *             </property>
	 *
	 *             <property name="elasticsearch.httpPool" value="${elasticsearch.httpPool:default}">
	 *                 <description> <![CDATA[ http连接池逻辑名称，在conf/httpclient.xml中配置]]></description>
	 *             </property>
	 *             <property name="elasticsearch.discoverHost" value="${elasticsearch.discoverHost:false}">
	 *                 <description> <![CDATA[ 是否启动节点自动发现功能，默认关闭，开启后每隔10秒探测新加或者移除的es节点，实时更新本地地址清单]]></description>
	 *             </property>
	 */
	public static void booter(String[] elasticsearchServerNames,BaseApplicationContext configContext){
		if(inited )
			return;
		inited = true;
		ElasticSearch elasticSearchSink = null;
		ElasticSearch firstElasticSearch = null;
		Map<String,ElasticSearch> elasticSearchMap = new HashMap<String,ElasticSearch>();
		for(String serverName:elasticsearchServerNames){
			Properties elasticsearchPropes = new Properties();
			elasticsearchPropes.put("elasticsearch.client",
					ElasticSearchHelper._getStringValue(serverName,"elasticsearch.client",configContext,"restful"));
			elasticsearchPropes.put("elasticUser",
					ElasticSearchHelper._getStringValue(serverName,"elasticUser",configContext,""));
			elasticsearchPropes.put("elasticPassword",
					ElasticSearchHelper._getStringValue(serverName,"elasticPassword",configContext,""));
			elasticsearchPropes.put("elasticsearch.rest.hostNames",
					ElasticSearchHelper._getStringValue(serverName,"elasticsearch.rest.hostNames",configContext,"127.0.0.1:9200"));
			elasticsearchPropes.put("elasticsearch.dateFormat",
					ElasticSearchHelper._getStringValue(serverName,"elasticsearch.dateFormat",configContext,"yyyy.MM.dd"));
			elasticsearchPropes.put("elasticsearch.timeZone",
					ElasticSearchHelper._getStringValue(serverName,"elasticsearch.timeZone",configContext,"Asia/Shanghai"));
			elasticsearchPropes.put("elasticsearch.ttl",
					ElasticSearchHelper._getStringValue(serverName,"elasticsearch.ttl",configContext,"2d"));
			elasticsearchPropes.put("elasticsearch.showTemplate",
					ElasticSearchHelper._getStringValue(serverName,"elasticsearch.showTemplate",configContext,"false"));
			elasticsearchPropes.put("elasticsearch.httpPool",
					ElasticSearchHelper._getStringValue(serverName,"elasticsearch.httpPool",configContext,serverName));
			elasticsearchPropes.put("elasticsearch.discoverHost",
					ElasticSearchHelper._getStringValue(serverName,"elasticsearch.discoverHost",configContext,"false"));
			final ElasticSearch elasticSearch = new ElasticSearch();
			if(firstElasticSearch == null)
				firstElasticSearch = elasticSearch;
			elasticSearch.setElasticsearchPropes(elasticsearchPropes);
			elasticSearch.configureWithConfigContext(configContext);
			if (!serverName.equals("default")) {
				elasticSearchMap.put(serverName, elasticSearch);
			} else {
				elasticSearchMap.put(DEFAULT_SEARCH, elasticSearch);
				elasticSearchSink = elasticSearch;
			}


		}
		if(elasticSearchSink == null)
			elasticSearchSink = firstElasticSearch;

		ElasticSearchHelper.elasticSearchSink = elasticSearchSink;

		if(elasticSearchMap.size() > 0) {
			Iterator<Map.Entry<String, ElasticSearch>> entries = elasticSearchMap.entrySet().iterator();
			while(entries.hasNext()){
				Map.Entry<String, ElasticSearch> entry = entries.next();
				final ElasticSearch elasticSearch = entry.getValue();
				elasticSearch.start();
				BaseApplicationContext.addShutdownHook(new Runnable() {
					@Override
					public void run() {
						elasticSearch.stop();
					}
				});
			}
			synchronized (ElasticSearchHelper.elasticSearchMap) {
				ElasticSearchHelper.elasticSearchMap.putAll(elasticSearchMap);
			}

		}
	}

	private static long _getLongValue(String poolName,String propertyName,BaseApplicationContext context,long defaultValue) throws Exception {
		String _value = null;
		if(poolName.equals("default")){
			_value = (String)context.getExternalProperty(propertyName);
			if(_value == null)
				_value = (String)context.getExternalProperty(poolName+"."+propertyName);

		}
		else{
			_value = (String)context.getExternalProperty(poolName+"."+propertyName);
		}
		if(_value == null){
			return defaultValue;
		}
		try {
			long ret = Long.parseLong(_value);
			return ret;
		}
		catch (Exception e){
			throw e;
		}
	}

	private static int _getIntValue(String poolName,String propertyName,BaseApplicationContext context,int defaultValue) throws Exception {
		String _value = null;
		if(poolName.equals("default")){
			_value = (String)context.getExternalProperty(propertyName);
			if(_value == null)
				_value = (String)context.getExternalProperty(poolName+"."+propertyName);

		}
		else{
			_value = (String)context.getExternalProperty(poolName+"."+propertyName);
		}
		if(_value == null){
			return defaultValue;
		}
		try {
			int ret = Integer.parseInt(_value);
			return ret;
		}
		catch (Exception e){
			throw e;
		}
	}
	private static String _getStringValue(String poolName,String propertyName,BaseApplicationContext context,String defaultValue){
		String _value = null;
		if(poolName.equals("default")){
			_value = (String)context.getExternalProperty(propertyName);
			if(_value == null)
				_value = (String)context.getExternalProperty(poolName+"."+propertyName);

		}
		else{
			_value = (String)context.getExternalProperty(poolName+"."+propertyName);
		}
		if(_value == null){
			return defaultValue;
		}
		return _value;
	}

	protected static void init(){
		if(inited )
			return;
		synchronized (elasticSearchMap) {
			if(inited)
				return;
			if (elasticSearchSink == null) {
				ElasticSearch _elasticSearchSink = elasticSearchMap.get(DEFAULT_SEARCH);
				if (_elasticSearchSink == null) {
					context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
					if(!context.isEmptyContext()) {
						_elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
						if (_elasticSearchSink != null) {
							elasticSearchMap.put(DEFAULT_SEARCH, _elasticSearchSink);
							elasticSearchSink = _elasticSearchSink;
						}
					}
				}

			}
			if(context.isEmptyContext()){
				if(bootMethod != null){
					try {
						bootMethod.invoke(null);
					} catch (IllegalAccessException e) {
						throw new ElasticsearchParseException("ElasticSearch load from Boot failed:",e);
					} catch (InvocationTargetException e) {
						throw new ElasticsearchParseException("ElasticSearch load from Boot failed:",e);
					}
				}
			}
			inited = true;

		}
	}

	/**
	 * 获取elasticSearch对应的elasticSearch服务器对象
	 * @param elasticSearch
	 * @return
	 */
	public static ElasticSearch getElasticSearchSink(String elasticSearch){
		init();
		if(elasticSearch == null || elasticSearch.equals("")) {

			return elasticSearchSink;
		}
		ElasticSearch elasticSearchSink = elasticSearchMap.get(elasticSearch);
		if(elasticSearchSink == null) {
			synchronized (elasticSearchMap) {
				elasticSearchSink = elasticSearchMap.get(elasticSearch);
				if(elasticSearchSink != null)
					return elasticSearchSink;
				context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
				elasticSearchSink = context.getTBeanObject(elasticSearch, ElasticSearch.class);
				if (elasticSearchSink != null) {
					elasticSearchMap.put(elasticSearch, elasticSearchSink);
				}
			}
		}
		return elasticSearchSink;
	}
	
	public static ElasticSearch getElasticSearchSink(){
		init();
		return elasticSearchSink;
	}

	/**
	 * 获取直接操作query dsl的rest api接口组件
	 * @return
	 */
	public static ClientInterface getRestClientUtil(){
		init();
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		return elasticSearchSink.getRestClientUtil();
	}

	/**
	 * 获取直接操作query dsl的rest api接口组件,所有的操作直接在elasticSearch对应的es服务器上操作
	 * @param elasticSearch
	 * @return
	 */
	public static ClientInterface getRestClientUtil(String elasticSearch){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getRestClientUtil();
	}

 
	/**
	 * 加载query dsl配置文件，在默认的es服务器上执行所有操作
	 * @param configFile
	 * @return
	 */
	public static ClientInterface getConfigRestClientUtil(String configFile){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		init();
		return elasticSearchSink.getConfigRestClientUtil(configFile);
	}

	/**
	 * 加载query dsl配置文件，在elasticSearch参数对应的es服务器上执行所有操作
	 * @param elasticSearch
	 * @param configFile
	 * @return
	 */
	public static ClientInterface getConfigRestClientUtil(String elasticSearch,String configFile){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		return elasticSearchSink.getConfigRestClientUtil(configFile);
	}

	/**
	 * 管理接口：添加rest服务器
	 * @param hosts
	 */
	public static void addHttpServer(List<String> hosts){

	}

}
