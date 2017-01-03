package serivice;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;


/**
 * 
 * @author daihui
 *
 */
public class BaseDAO {
	
	private static String jdbcMySQLUri=null;
	
	public String getProperty(String propertyName){
		String result=null;
		try {
			Properties prop=new Properties();
			InputStream is=getClass().getClassLoader().getResourceAsStream("jdbc.properties");
			prop.load(is);
			result=prop.getProperty(propertyName);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	static{
		BaseDAO obj=new BaseDAO();
		String jdbcMySQLDriverClassName=obj.getProperty("jdbcMySQLDriverClassName");
		jdbcMySQLUri=obj.getProperty("jdbcMySQLUri");
		try {
			Class.forName(jdbcMySQLDriverClassName);//注册jdbc驱动类
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Connection getConn(){
		Connection conn=null;
		try {
			conn = DriverManager.getConnection(jdbcMySQLUri);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	public static void close(Connection conn){
		try {
			conn.close();
		} catch (Exception e) {
		}
	}

	public static void close(Statement stmt){
		try {
			stmt.close();
		} catch (Exception e) {
		}
	}

	public static void close(PreparedStatement stmt){
		try {
			stmt.close();
		} catch (Exception e) {
		}
	}

	public static void close(ResultSet rs){
		try {
			rs.close();
		} catch (Exception e) {
		}
	}
	
	/**
	 * insert,update,delete类的对数据库的内容造成更改的sql语句执行的方法
	 * @param sql sql语句
	 * @param params ?占位符对应的参数值
	 * @param types ?占位符对应的数据库字段类型
	 * @return sql语句执行后影响的数据库记录行数
	 */
	public static int update(String sql,Object params[],int types[]){
		int result=0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConn();
			pstmt=conn.prepareStatement(sql);
			for (int index=0;index<params.length;index++){
				pstmt.setObject(index+1, params[index], types[index]);
			}
			result=pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(pstmt);
			close(conn);
		}
		return result;
	}
	
	public static int queryForInt(String sql,Object params[],int types[]){
		int result=0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs=null;
		try {
			conn = getConn();
			pstmt=conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			while(rs.next()){
				return result=rs.getInt(1);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs);
			close(pstmt);
			close(conn);
		}
		return result;
	}
	}
	
	
	
	

//	static{
//		Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
//	}
//    public static void main(String[] args) throws Exception {
//        Connection conn = null;
//        String sql;
//        String url = "jdbc:mysql://localhost:3306/military?"
//                + "user=root&password=daihui888&useUnicode=true&characterEncoding=UTF8";
// 
//        try {
//            
// 
//            System.out.println("成功加载MySQL驱动程序");
//            // 一个Connection代表一个数据库连接
//            conn = DriverManager.getConnection(url);
//            // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
//            Statement stmt = conn.createStatement();
//            sql = "create table student(NO char(20),name varchar(20),primary key(NO))";
//            int result = stmt.executeUpdate(sql);// executeUpdate语句会返回一个受影响的行数，如果返回-1就没有成功
//            if (result != -1) {
//                System.out.println("创建数据表成功");
//                sql = "insert into student(NO,name) values('2012001','陶伟基')";
//                result = stmt.executeUpdate(sql);
//                sql = "insert into student(NO,name) values('2012002','周小俊')";
//                result = stmt.executeUpdate(sql);
//                sql = "select * from student";
//                ResultSet rs = stmt.executeQuery(sql);// executeQuery会返回结果的集合，否则返回空值
//                System.out.println("学号\t姓名");
//                while (rs.next()) {
//                    System.out
//                            .println(rs.getString(1) + "\t" + rs.getString(2));// 入如果返回的是int类型可以用getInt()
//                }
//            }
//        } catch (SQLException e) {
//            System.out.println("MySQL操作错误");
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            conn.close();
//        }
// 
//    }

