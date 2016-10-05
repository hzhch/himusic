package hzc.himusic.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * ����http����Ĺ�����
 */
public class HttpUtils {

	/**
	 * ����GET����
	 * @param url  ������Դ·��
	 * @return
	 * @throws Exception 
	 */
	public static InputStream getInputStream(String path) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn=(HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		InputStream is = conn.getInputStream();
		return is;
	}
	
	/**
	 * ������������Ϊ�ַ���
	 * @return
	 */
	public static String isToString(InputStream is)throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null){
			sb.append(line);
		}
		return sb.toString();
	}
	
}


