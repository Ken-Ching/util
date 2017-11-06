package team.kc.util.http;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpPostUtil {
	protected static final Logger logger = LoggerFactory.getLogger(HttpPostUtil.class);

	private static String BOUNDARY_PREFIX = "--";
	private static String BOUNDARY = "--------http post boundary";
	private static String NEW_LINE = "\r\n";
	
	public static String send (String url, InputStream ins) {  
        try {  
            // 服务器的域名  
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();  
            // 设置为POST
            conn.setRequestMethod("POST");  
            // 发送POST请求必须设置如下两行  
            conn.setDoOutput(true);  
            conn.setDoInput(true);  
            conn.setUseCaches(false);  
            // 设置请求头参数  
            conn.setRequestProperty("connection", "Keep-Alive");  
            conn.setRequestProperty("Charsert", "UTF-8");  
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);  
  
            OutputStream out = new DataOutputStream(conn.getOutputStream());  
  
            // 将参数头的数据写入到输出流中  
            out.write(getParamStart().getBytes());  
  
            // 数据输入流,用于读取文件数据  
            byte[] bufferOut = new byte[1024];  
            int bytes = 0;  
            // 每次读1KB数据,并且将文件数据写入到输出流中  
            while ((bytes = ins.read(bufferOut)) != -1) {  
                out.write(bufferOut, 0, bytes);  
            }  
            // 最后添加换行  
            out.write(NEW_LINE.getBytes());  
            ins.close();  
  
            // 写上结尾标识  
            out.write(getParamEnd().getBytes());  
            out.flush();  
            out.close();  
  
            int code = conn.getResponseCode();
    		if (code == 200) {
    			InputStream incon = conn.getInputStream();
    			ByteArrayOutputStream outcon = new ByteArrayOutputStream();
    			byte[] buf = new byte[1024 * 8];
    			int len;
    			while ((len = incon.read(buf)) != -1) {
    				outcon.write(buf, 0, len);
    			}
    			conn.disconnect();
    			String s = new String(outcon.toByteArray(), "utf-8");
    			return s;
    		}
  
        } catch (Exception e) {  
            logger.error("发送POST请求出现异常！" + e.getMessage(), e);  
        } 
        
        return null;
    }
    
    // 把文件转换成字节数组
 	public static byte[] getBytes(InputStream in) throws Exception {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		byte[] b = new byte[1024];
 		int n;
 		while ((n = in.read(b)) != -1) {
 			out.write(b, 0, n);
 		}
 		in.close();
 		return out.toByteArray();
 	}
 	
 	private static String getParamStart () {
 		// 上传文件    
        StringBuilder sb = new StringBuilder();  
        sb.append(BOUNDARY_PREFIX);  
        sb.append(BOUNDARY);  
        sb.append(NEW_LINE);  
        // 文件参数,photo参数名可以随意修改  
        sb.append("Content-Disposition: form-data;" + NEW_LINE);  
        sb.append("Content-Type:application/octet-stream");  
        // 参数头设置完以后需要两个换行，然后才是参数内容  
        sb.append(NEW_LINE);  
        sb.append(NEW_LINE);
        
        return sb.toString();
 	}
 	
 	// 定义最后数据分隔线，即--加上BOUNDARY再加上--。 
 	private static String getParamEnd () {
        StringBuilder sb = new StringBuilder();  
        sb.append(NEW_LINE);  
        sb.append(BOUNDARY_PREFIX);  
        sb.append(BOUNDARY);  
        sb.append(BOUNDARY_PREFIX);  
        sb.append(NEW_LINE);
        
        return sb.toString();
 	}
    
    public static void main (String[] args) {
    	try {
			System.out.println( send("http://10.133.12.130:8003/icr/recognize_vehicle_license", new FileInputStream("d://1.png")) );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	//sendPost("http://imgs-sandbox.intsig.net/icr/recognize_driving_license", "");
    }
}
