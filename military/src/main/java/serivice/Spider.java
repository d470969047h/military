package serivice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Types;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 爬些武器装备信息
 * @author daihui
 * @date 2017-01-03
 *
 */
public class Spider {

	static String mainUrl = "http://weapon.huanqiu.com";
	
	static String rootPath = "D:/daihui/spider/飞行器/";
	/**
	 * 坦克链接
	 */
	static String tankUrl = "/weaponlist/tank";
	static String tankItemInfo = "/list_0_0_0_0_";
	/**
	 * 飞行器链接
	 */
	static String aircraftUrl = "/weaponlist/aircraft";
	static String aircraftItemInfo = "/list_0_0_0_0_";
	
	public static void main(String[] args) {
		String tank_url = mainUrl +tankUrl;
		Spider.getMilitary(tank_url,tankItemInfo);
		
//		String aircraft_url = mainUrl +aircraftUrl;
//		Spider.getMilitary(aircraft_url,aircraftItemInfo);
	}

	/**
	 * 获取名称和详细信息地址 
	 */
	public static void getMilitary(String url,String itemInfo) {
		try {
			
			Document pageDoc = Jsoup.connect(url).timeout(40000).get();
			Elements pageLink = pageDoc.select("div.pages a");
			String text = pageLink.get(pageLink.size() - 3).text();
			int totalPage = Integer.parseInt(text);
			String militaryName = "";
			
			for (int index = 0; index < totalPage; index++) {
				int tempNum = index + 1;
				String requestUrl = url + itemInfo + tempNum;
				Document doc = Jsoup.connect(requestUrl).timeout(20000).get();
				Elements link = doc.select("div.picList ul li span.name a");

				int num = link.size();
				for (int i = 0; i < num; i++) {
					militaryName = link.get(i).text();
					String detailUrl = link.get(i).attr("href");
					Spider.getTankDetailInfo(mainUrl+detailUrl,militaryName);//获取详细信息
				}
				//小图片
				Elements picLink = doc.select("div.picList ul li span.pic a img");
				for(Element plink:picLink){
					String mName = plink.attr("alt");
					String picSrc = plink.attr("src");
					if(mName.equals(militaryName)){
						String smallPicPath = rootPath+ FileTool.filenameFilter(militaryName)+"/";
						
						String[] splitPic = picSrc.split("/");
						String picName = splitPic[splitPic.length-1];
						Response resultImageResponse;
						try {
							resultImageResponse = Jsoup.connect(picSrc).ignoreContentType(true).execute();
							FileOutputStream out = (new FileOutputStream(new File(smallPicPath +picName)));
							out.write(resultImageResponse.bodyAsBytes());           
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取详细信息 
	 */
	public static void getTankDetailInfo(String url, String name) {
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		name = FileTool.filenameFilter(name);
		String path = rootPath + name + "/";
		FileTool.mkdir(path);
		try {
			Document detailDoc = Jsoup.connect(url).timeout(25000).get();
			Spider.saveDetailInfo(detailDoc, path,name,sb1);//保存中部文本内容
			Spider.saveBasicParameters(detailDoc, path, name, sb2);//保存基本参数
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取图片并，保存到本地 
	 */
	public static void getPic(Elements maxPic,String path){
		if(maxPic.size()>0){
			String picSrc = maxPic.get(0).attr("src");
			String[] splitPic = picSrc.split("/");
			String picName = splitPic[splitPic.length-1];
			
			//Open a URL Stream
			Response resultImageResponse;
			try {
				resultImageResponse = Jsoup.connect(picSrc).ignoreContentType(true).execute();
				// output here
				FileOutputStream out = (new FileOutputStream(new File(path +picName)));
				out.write(resultImageResponse.bodyAsBytes());           
				// resultImageResponse.body() is where the image's contents are.
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 保存详情页中间信息（文字和图片）
	 * @param detailDoc
	 * @param path
	 * @param name
	 * @param sb
	 */
	public static void  saveDetailInfo(Document detailDoc,String path,String name,StringBuffer sb){
		String contents = "";
		String txtName = path + name + ".txt";
		//图片
		Elements maxPic = detailDoc.select("div.conMain div.maxPic img");
		Spider.getPic(maxPic,path);
		//简介
		Elements intron = detailDoc.select("div.conMain div.intron div.module");
		String intronText = intron.get(0).text();
		sb.append("装备简介"+"\r\n");
		sb.append(intronText+"\r\n");
		//介绍信息
		Elements info = detailDoc.select("div.conMain div.info div.module div.otherList");
		for(Element item:info){
			Elements title_ = item.getElementsByClass("title_");
			String titleInfo = title_.get(0).text();
			sb.append(titleInfo+"\r\n");
			
			Elements textInfo = item.getElementsByClass("textInfo");
			for(Element textItem:textInfo){
				 Elements targetPs = textItem.select("p");
				for(Element p:targetPs){
					String inf = p.text();
					sb.append(inf+"\r\n");
				}
			}
		}
		contents = sb.toString();
		FileTool.createNewFile(txtName, contents);
	}
	
	/**
	 * 保存基本参数 
	 * @param detailDoc
	 * @param path
	 * @param name
	 * @param sb
	 */
	public static void  saveBasicParameters(Document detailDoc,String path,String name,StringBuffer sb){
		String contents = "";
		String txtName = path + "基本参数.txt";
		String sql = "insert into t_weapon(weapon,propName,propValue) values(?,?,?)";
		Elements basicParameters = detailDoc.select("div.side div.dataInfo ul li");
		for(Element item:basicParameters){
			String prop = item.text();
			int count = prop.split("：").length;
			String propName = prop.split("：")[0];
			String propValue = count == 2 ? prop.split("：")[1] : "无";
			sb.append(prop+"\r\n");
			//插入数据库
			int i = BaseDAO.update(sql,new Object[] {name, propName,propValue},new int[] {Types.VARCHAR,Types.VARCHAR, Types.VARCHAR });
		}
		contents = sb.toString();
		FileTool.createNewFile(txtName, contents);
		
		
	}
}
















