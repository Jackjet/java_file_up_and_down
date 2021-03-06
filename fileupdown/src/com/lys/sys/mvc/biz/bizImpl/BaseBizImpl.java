package com.lys.sys.mvc.biz.bizImpl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.lys.sys.log.Log;
import com.lys.sys.mvc.biz.BaseBiz;
import com.lys.sys.mvc.dao.BaseDao;
import com.lys.sys.utils.OtherUtil;
import com.lys.sys.utils.StringUtils;
import com.lys.sys.utils.db.BizTransUtil;
import com.lys.sys.utils.pagination.PageBean;


/**
 * 支持sql直接操作数据库
 * @author shuang
 * @param <T>
 * @param 
 */
@Service("BaseBizImpl")
public class BaseBizImpl implements BaseBiz  {
	@Resource(name = "BaseDaoImpl")
	public BaseDao baseDao;
	/**
	 * 操作对象
	 * 添加-----单表单条数据-----向数据库添加一条数据，普通插入，不能获取到自增的主键值
	 * @param obj 传入有数据的实体对象,实体对象必须有@MyTable注解
	 * @return true or false 
	 * @使用示例：Test test=new Test();  addTRANS(test) ;
	 */
	public Boolean addTRANS(Object obj) {
		try {
			return baseDao.insert(obj)==1;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 操作对象
	 * 添加-----单表单条数据-----向数据库添加一条数据，普通插入，不能获取到自增的主键值
	 * @param obj 传入有数据的实体对象,实体对象必须有@MyTable注解 pkName值，主键类型只支持 Integer/Long/String三种类型
	 * @param isReturnKey 是否返回主键，并存入实体中，（ 对于有自增主键的实体）
	 * @return 保存true or false 
	 * @使用示例：Test test=new Test(); addTRANS(test,true) ; 如果为true  但是test 中不能含有主键值
	 */
	public Boolean addTRANS(Object obj,Boolean isReturnKey){
		try {
			if(isReturnKey){
				baseDao.insertAndReturnKey(obj) ;
				return true ;//如果没有异常返回true
			}else{
				return baseDao.insert(obj)==1;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 操作对象
	 * 添加-----单表多条数据-----向数据库添加多条数据，普通插入，不能获取到自增的主键值==批处理
	 * @param objs 传入有数据的  List集合实体对象
	 * @param obj 传入表对应的 实体对象 类型，并实体有@MyTable 注解的 tableName值
	 * @基于SimpleJdbc 
	 * @使用示例：List<Test> tsets=new ArrayList<Test>();  addsTRANS(tsets,Test.class) ;
	 */
	public Boolean addsTRANS(List<?> objs,Class<?> obj){
		try {
			baseDao.insertBatch(objs, obj);
			return true;//如果没有异常返回true
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 操作执行SQL
	 * 添加-----
	 * @param sql 执行的语句
	 * @param params 可变参数
	 * @return 添加成功后的数据自增主键值
	 * @说明：如果操作的表没有自增主键，那么则抛出异常
	 */
	public Number addTRANS(String sql, Object... params) {
		try {
			return baseDao.insertAndReturnKey(sql, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 操作执行SQL
	 * 删除/更新/添加
	 * @param sql 执行的语句（可以是预编译的sql）
	 * @param params 可变参数
	 * @return 影响条数 
	 * @使用示例：String sql=" update test set name=?,birthday=? where id=? ";  executeTRANS(sql,name值,birthday值,id值);或  execute(sql,new Object[]{xx,xx,xx});
	 */
	public Integer executeTRANS(String sql, Object... params) {
		try {
			return baseDao.execute(sql, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 操作批量的SQL执行----带有事务。
	 * 详细使用请参考BizTransUtil 对象中的声明。
	 */
	public Boolean executesTRANS(List<BizTransUtil> list) {
		if(list!=null&&list.size()>0){
			int a=0;//记录执行BizTransUtil的个数
			try {
				for(BizTransUtil biztrans : list){
					Integer executenum=-1;
					//如果是自定义sql
					if(StringUtils.hasText(biztrans.getExecute_sql())){
						executenum=baseDao.execute(biztrans.getExecute_sql(), biztrans.getExecute_params());
					}
					//如果是新增
					else if(OtherUtil.INSERT.equals(biztrans.getExecuteType())){
						if(biztrans.getAdd_obj()!=null){
							executenum=baseDao.insert(biztrans.getAdd_obj());//单个保存
						}else if(biztrans.getAdd_list()!=null&&biztrans.getAdd_class()!=null){
							baseDao.insertBatch(biztrans.getAdd_list(), biztrans.getAdd_class());//批量保存
							executenum=biztrans.getAdd_list().size();
						}
					}
					//如果是删除
					else if(OtherUtil.DELETE.equals(biztrans.getExecuteType())){
						if(biztrans.getDel_id()!=null&&biztrans.getDel_obj()!=null){
							executenum=baseDao.delete(biztrans.getDel_id(), biztrans.getDel_obj());//单个删除
						}else if(biztrans.getDel_ids()!=null&&biztrans.getDel_obj()!=null){
							executenum=baseDao.deletes(biztrans.getDel_ids(), biztrans.getDel_obj());//批量删除
						}else if(biztrans.getDel_map()!=null&&biztrans.getDel_obj()!=null){
							executenum=baseDao.delete(biztrans.getDel_map(), biztrans.getDel_obj());//按条件删除
						}
					}
					//如果是修改
					else if(OtherUtil.UPDATE.equals(biztrans.getExecuteType())){
						if(biztrans.getUpd_map()!=null&&biztrans.getUpd_obj()!=null){
							executenum=baseDao.update(biztrans.getUpd_map(),biztrans.getUpd_obj());//按map内容进行 根据主键ID条件 进行更新
						}
					}
					//如果开启了执行结果对比
					if(biztrans.getIscase()){
						if(executenum<biztrans.getWhen()){
							throw new RuntimeException(biztrans.getErorrmsg());
						}
					}
					if(executenum<0){
						throw new RuntimeException("在第"+a+"BizTransUtil对象进行执行的时候出现问题，请仔细检查。");
					}
					biztrans.setReturnvalue(executenum);
					biztrans.setReturnType(OtherUtil.RETURN_1);
					a++;
				}
				if(a==list.size()){
					return true;
				}else{
					return false;
				}
			} catch (Exception e) {
				Log.in.error("执行List<BizTransUtil>集合中的第："+a+"个对象出现错误，请仔细检查。");
				Log.in.error("对象是：list.get("+a+")==："+list.get(a).toString());
				Log.in.error("错误原因："+e.getMessage());
//				e.printStackTrace();//输出异常
				throw new RuntimeException(e.getMessage());
			}
		}else{
			return false;
		}
	}
	/**
	 * 操作执行SQL
	 * 删除-----根据主键ID删除
	 * @param id   主键ID 值
	 * @param obj 传入表对应的 实体对象 类型，并实体有@MyTable 注解的 pkName值
	 * @return 影响行数
	 * @使用示例 delete(188,Test.calss);
	 */
	public Boolean deleteTRANS(Object id,Class<?> obj){
		try {
			if(baseDao.delete(id, obj)==1){
				return true;//则删除正常，影响条数为1， 因为 是主键
			}else{
				throw new RuntimeException("删除失败!");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 操作执行SQL---该方法待测试
	 * 删除---根据主键ID删除
	 * @param ids   主键ID 值数组
	 * @param obj 传入表对应的 实体对象 类型，并实体有@MyTable 注解的 pkName值
	 * @return 影响行数
	 * @使用示例 deleteTRANS(new Object[]{x,x,x,x},Test.calss); 该删除方法原理为构建一条删除SQL 语句
	 */
	public Boolean deleteTRANS(Object[] ids,Class<?> obj){
		try {
			if(baseDao.deletes(ids, obj)>0){//如果以后两个操作同时删除一些数据，则后者显示为删除失败但是又的确被删除 了
				return true;//则删除正常，影响条数为大于1， 因为 是主键
			}else{
				throw new RuntimeException("删除失败!");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 操作执行SQL
	 * 删除---根据多个条件删除
	 * @param map   条件的字段(key)和对应的值(value)
	 * @param obj 传入表对应的 实体对象 类型，并实体有@MyTable 注解的 tableName值
	 * @return 影响行数
	 */
	public Integer deleteTRANS(Map<String, Object> map,Class<?> obj){
		try {
			return baseDao.delete(map, obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 操作执行SQL
	 * 更新
	 * @param map 要修改的字段(key)和对应的值(value)，里面必须包含主键key和value
	 * @param obj 要修改的实体类型，或获取实体对象里面的@MyTable注解值
	 * @return 影响条数
	 * @使用示例：Map map=new HashMap();map.put("id", 189);map.put("name","改后了");  updateTRANS(map,Test.calss); 
	 */
	public Integer updateTRANS(Map<String, Object> map,Class<?> obj){
		try {
			return baseDao.update(map, obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @return 查询一行、单列 Int类型数据，并返回int型结果
	 * @说明：如果查询不到，或者查询多行， 则抛出异常
	 */
	public Integer queryForInt( String sql){
		return baseDao.queryForInt(sql);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args  参数数组
	 * @return 查询一行、单列 Int类型数据，并返回int型结果
	 * @说明：如果查询不到，或者查询多行， 则抛出异常
	 */
	public Integer queryForInt( String sql,Object[] args){
		return baseDao.queryForInt(sql,args);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @param argTypes 对映的参数类型数据
	 * @return 查询一行、单列 Int类型数据，并返回int型结果
	 * @说明：如果查询不到，或者查询多行， 则抛出异常
	 */
	public Integer queryForInt(String sql, Object[] args, int[] argTypes){
		return baseDao.queryForInt(sql, args, argTypes);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param requiredType 指定返回数据的类型
	 * @return 查询一行、单列任何类型的数据，返回结果参数指定的类型
	 * @说明：如果查询不到，或者查询多行， 则抛出异常
	 */
	public Object queryForObject(String sql,Class<?> requiredType ){
		return baseDao.queryForObject(sql,requiredType);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @param requiredType 指定返回数据的类型
	 * @return 查询一行、单列任何类型的数据，返回结果参数指定的类型
	 * @说明：如果查询不到，或者查询多行， 则抛出异常
	 */
	public Object queryForObject(String sql,Object[] args,Class<?> requiredType ){
		return baseDao.queryForObject(sql,args,requiredType);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param params 参数数组
	 * @param argTypes 对映的参数类型数组
	 * @param requiredType 指定返回数据的类型
	 * @return 查询一行、单列任何类型的数据，返回结果参数指定的类型
	 * @说明：如果查询不到，或者查询多行， 则抛出异常
	 */
	public Object queryForObject(String sql,Object[] args, int[] argTypes,Class<?> requiredType ){
		return baseDao.queryForObject(sql, args, argTypes, requiredType);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @return 查询一行、多列数据并将该行数据转换为Map返回
	 * @说明：如果查询不到，或者查询多行， 则抛出异常
	 */
	public Map<String, Object> queryForMap(String sql){
		return baseDao.queryForMap(sql);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @return 查询一行、多列数据并将该行数据转换为Map返回
	 * @说明：如果查询不到，或者查询多行， 则抛出异常
	 */
	public Map<String, Object> queryForMap(String sql, Object[] args){
		return baseDao.queryForMap(sql, args);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @param argTypes 对映的参数类型数据
	 * @return 查询一行、多列数据并将该行数据转换为Map返回
	 * @说明：如果查询不到，或者查询多行， 则抛出异常
	 */
	public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes){
		return baseDao.queryForMap(sql, args, argTypes);
	}

	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param elementType 返回对象集合
	 * @return 查询多行、一列数据并将该行数据转换为List<Object>返回
	 * @说明： 可以是String类型--得到一列的值，可以是实体对象类型---得到一个实体集合
	 */
	@SuppressWarnings("rawtypes")
	public List queryForList(String sql,Class<?> elementType){
		return baseDao.queryForList(sql, elementType);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @param elementType 返回对象集合
	 * @return 查询多行、一列数据并将该行数据转换为List<Object>返回
	 * @说明： 可以是String类型--得到一列的值，可以是实体对象类型---得到一个实体集合
	 */
	@SuppressWarnings("rawtypes")
	public List queryForList(String sql,Object[] args,Class<?> elementType){
		return baseDao.queryForList(sql, args,  elementType);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @param argTypes 对映的参数类型数据
	 * @param elementType 返回对象集合
	 * @return 查询多行、一列数据并将该行数据转换为List<Object>返回
	 * @说明： 可以是String类型--得到一列的值，可以是实体对象类型---得到一个实体集合
	 */
	@SuppressWarnings("rawtypes")
	public List queryForList(String sql,Object[] args,int[] argTypes,Class<?> elementType){
		return baseDao.queryForList(sql, args, argTypes, elementType);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @return 查询多行、多列数据并将该行数据转换为List<Map<String,Object>>返回
	 */
	public List<Map<String,Object>> queryForList(String sql){
		return baseDao.queryForList(sql);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @return 查询多行、多列数据并将该行数据转换为List<Map<String,Object>>返回
	 */
	public List<Map<String,Object>> queryForList(String sql,Object[] args){
		return baseDao.queryForList(sql, args);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @param argTypes 对映的参数类型数据
	 * @return 查询多行、多列数据并将该行数据转换为List<Map<String,Object>>返回
	 */
	public List<Map<String,Object>> queryForList(String sql,Object[] args,int[] argTypes){
		return baseDao.queryForList(sql, args, argTypes);
	}
	/**
	 * 分页查询
	 * @param sql 查询的分页数据
	 * @param sqlCount 查询的分页数据的总条数
	 * @param pageIndex 页码
	 * @param pageSize 页大小
	 * @return PageBean的分页对象
	 * @注：该分页是 分页前 排序。
	 */
	public PageBean getPages(String sql,String sqlCount,Integer pageIndex,Integer pageSize){
		PageBean pagination=null;
		try {
			pagination=new PageBean(baseDao.getPages(sql,sqlCount, pageIndex, pageSize),pageIndex, pageSize);
			return pagination;
		} catch (Exception e) { 
			throw new RuntimeException(e);
		}
	}
	/**
	 * 分页查询
	 * @param sql 查询的分页数据
	 * @param sqlCount 查询的分页数据的总条数
	 * @param pageIndex 页码
	 * @param pageSize 页大小
	 * @param args 参数数组
	 * @return PageBean的分页对象
	 * @注：该分页是 分页前 排序。
	 */
	public PageBean getPages(String sql, String sqlCount,Integer pageIndex, Integer pageSize,Object[] args){
		PageBean pagination=null;
		try {
			pagination=new PageBean(baseDao.getPages(sql,sqlCount, pageIndex, pageSize, args),pageIndex, pageSize);
			return pagination;
		} catch (Exception e) { 
			throw new RuntimeException(e);
		}
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @return 查询一批数据，返回为SqlRowSet，类似于ResultSet，但不再绑定到连接上
	 * @说明：可以遍历SqlRowSet ，得到各种样式的数据
	 */
	public SqlRowSet queryForRowSet(String sql){
		return baseDao.queryForRowSet(sql);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @return 查询一批数据，返回为SqlRowSet，类似于ResultSet，但不再绑定到连接上
	 * @说明：可以遍历SqlRowSet ，得到各种样式的数据
	 */
	public SqlRowSet queryForRowSet(String sql,Object[] args){
		return baseDao.queryForRowSet(sql, args);
	}
	/**
	 * 操作执行SQL
	 * 查询
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @param argTypes 对映的参数类型数据
	 * @return 查询一批数据，返回为SqlRowSet，类似于ResultSet，但不再绑定到连接上
	 * @说明：可以遍历SqlRowSet ，得到各种样式的数据。
	 * 使用示例：
	 *	SqlRowSet rs; int ColumnCount=rs.getMetaData().getColumnCount();
	 *	while (rs.next()) { Map<String, Object> obj = new LinkedHashMap<String, Object>();//一行的值
	 *		int i = 1; for (int j = 0; j < ColumnCount; j++)  obj.put(rs.getMetaData().getColumnName(j+1), rs.getString(i++));
	 *	}
	 */
	public SqlRowSet queryForRowSet(String sql,Object[] args,int[] argTypes){ 
		return baseDao.queryForRowSet(sql, args, argTypes);
	}
	/**
	 * 操作执行SQL
	 * 查询--对应的java bean集合
	 * @param sql 执行的语句
	 * @param clazz 传入返回List对象的类型，例如：User.class， 得到的值可以转换成(List<User>)进行转换
	 * @return clazz的对象集合List
	 */
	@SuppressWarnings("rawtypes")
	public List queryForListT(String sql,Class<?> clazz){
		return baseDao.queryForListT(sql, clazz);
	}
	/**
	 * 操作执行SQL
	 * 查询--对应的java bean集合
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @param clazz 传入返回List对象的类型，例如：User.class， 得到的值可以转换成(List<User>)进行转换
	 * @return clazz的对象集合List
	 */
	@SuppressWarnings("rawtypes")
	public List queryForListT(String sql,Object[] args,Class<?> clazz){
		return baseDao.queryForListT(sql,args,clazz);
	}
	/**
	 * 操作执行SQL
	 * 查询--对应的java bean集合
	 * @param sql 执行的语句
	 * @param args 参数数组
	 * @param argTypes 对映的参数类型数据
	 * @param clazz 传入返回List对象的类型，例如：User.class， 得到的值可以转换成(List<User>)进行转换
	 * @return clazz的对象集合List
	 */
	@SuppressWarnings("rawtypes")
	public List queryForListT(String sql,Object[] args,int[] argTypes,Class<?> clazz){
		return baseDao.queryForListT(sql,args,argTypes,clazz);
	}
	
	/***
	 * 存储过程--- 查询
	 * @param callName  传入的存储过程名字
	 * @param params	传入的预处理的参数
	 * @return 结果集的List<Map<String, Object>>类型的数据
	 * @说明：主要用于查询得到结果集，不能获取返回的值。例如支持：TestCall11(IN P_ID int,IN P_NAME varchar(100)) 等只有IN 的类似存储过程
	 * @使用示例： executeCallReturnData("TestCall22", 1,"中国人call");
	 */
	public List<Map<String, Object>> executeCallReturnDataTRANS( String callName, Object...params){
		try {
			return baseDao.executeCallReturnData(callName, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/***
	 * 存储过程---执行（增删改）
	 * @param callName  传入的存储过程名字
	 * @param params	传入的预处理的参数
	 * @说明：主要用于通过存储过程执行操作数据的增删改。只支持：TestCall22(IN pid int,IN pname varchar(100),out flag int,out message varchar(1000)) 等必须有 两个输出参数的存储过程
	 * 		  flag =1 代表执行成功，msg=执行失败后的消息返回
	 * @使用示例： executeCall("TestCall22", 1,"中国人call"); 此处不需要传入后面两个返回的参数值
	 */
	public void executeCallTRANS( String callName, Object...params) {
		try {
			baseDao.executeCall(callName, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
