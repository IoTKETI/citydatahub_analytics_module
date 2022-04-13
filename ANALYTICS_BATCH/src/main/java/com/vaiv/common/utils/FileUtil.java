package com.vaiv.common.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import net.sf.json.JSONObject;

public class FileUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	// character encoding
	private static final String CHARSET = "euc-kr";

	/**
	 * 파일 업로드
	 * @param request
	 * @param response
	 * @param uploadFile
	 * @param uploadPath
	 * @return
	 * @throws Exception 
	 */
	public static JSONObject fileUpload(HttpServletRequest request,
			HttpServletResponse response, MultipartFile uploadFile, String uploadPath) throws Exception{
		
		logger.info(" fileUpload uploadFile: "+uploadFile+", uploadPath: "+uploadPath);
		
		String result = "success";
		String originalFileName = uploadFile.getOriginalFilename();
		String extension = originalFileName.substring(originalFileName.lastIndexOf("."), originalFileName.length());
		
		String savedFileName = String.valueOf(System.currentTimeMillis()) + extension;
		String savedFilePath = uploadPath + savedFileName; // 저장경로
		String fileType = uploadFile.getContentType();
		long fileSize = uploadFile.getSize();
		
		mkdir(uploadPath);
	
		File file = new File(savedFilePath);
		uploadFile.transferTo(file);
		
		JSONObject json = new JSONObject();
		json.put("result", result);
		json.put("originalFileName", originalFileName);
		json.put("savedFileName", savedFileName);
		json.put("savedFilePath", savedFilePath);
		json.put("fileType", fileType);
		json.put("fileSize", fileSize);
		
		logger.info("# fileUpload Complete Json: "+ json.toString());
		return json;
	}

	/**
	 * 파일 삭제
	 * @param file
	 */
	public static void fileDelete(String file){
		if( file != null ){
			logger.info(" fileDelete file: "+file);
			File deleteFile = new File(file);
			if( deleteFile.exists() ){
				deleteFile.delete();
				logger.info("# fileDelete Complete file: "+file);
			}	
		}
	}
	
	
	/**
	 * 디렉토리의  하위 폴더,파일 모두 삭제
	 * @param path
	 */
	public static void deleteAllFiles(String path){ 
		File selectedDir= new File(path); 
        
        // 하위 디렉토리들을 배열에 담는다. 
        File[] innerFiles= selectedDir.listFiles(); 
        
        // 하위 디렉토리 삭제 
        for(int i=0; i<innerFiles.length; i++){ 
            innerFiles[i].delete(); 
        } 
	}
	
	
	/**
	 * 파일 복사
	 * @param before
	 * @param after
	 * @return
	 * @throws Exception 
	 */
	public static String fileCopy(String before, String after) throws Exception{
		logger.info(" fileCopy before: "+before+", after: "+after);
		File beforeFile = new File(before);
		
		if( beforeFile.exists() ){
			File afterFile = new File(after);
			
			if( afterFile.exists() )	FileUtil.fileDelete(after);
			
			Files.copy(beforeFile.toPath(), afterFile.toPath());
			logger.info("# fileCopy Complete before: "+before+", after: "+after);
			return "success";
		}else{
			return "Not find file : "+beforeFile;
		}
	}
	
	/**
	 * 디렉토리 생성
	 * @param file
	 */
	public static void mkdir(String file) throws Exception{
		if( file != null ){
			File path = new File(file);
			if( !path.exists() ){
				path.mkdirs();
				logger.info("# mkdir Complete file: "+file);
			}
		}
	}
	
	/**
	 * 파일 다운로드
	 * @param request
	 * @param response
	 * @param fileName
	 * @param file
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws IOException 
	 */
	public static String fileDownload(HttpServletRequest request, HttpServletResponse response, String fileName, File file) throws UnsupportedEncodingException{
		logger.info(" fileDownload fileName: "+fileName+", file: "+file);
		String result = "success";
		int fileSize = (int) file.length();
		String mimetype = request.getSession().getServletContext().getMimeType(file.getName());
		String userAgent = request.getHeader("User-Agent");
		boolean ie = userAgent.indexOf("MSIE") > -1 || userAgent.indexOf("rv:11") > -1;
		
		if (StringUtils.isEmpty(mimetype)) 	mimetype = "application/octet-stream;";
	
		if ( file.exists() ) {
			BufferedInputStream in = null;
			
			if( ie ){
				fileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
			}else{
				fileName = new String(fileName.getBytes("utf-8"),"iso-8859-1").replace("+", "%20"); 
			}
			
			try {
				in = new BufferedInputStream(new FileInputStream(file));
				response.setBufferSize(fileSize);
				response.setContentType(mimetype + "; charset=" + CHARSET);
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				response.setContentLength(fileSize);
	 
				FileCopyUtils.copy(in, response.getOutputStream());
				
				response.getOutputStream().flush();
				response.getOutputStream().close();
				logger.info("# fileDownload Complete fileName: "+fileName+", file: "+file);				
			} catch (IOException e) {
				logger.error("Error fileDownload "+e.toString());
			}finally{
				try {
					if( in != null )
						in.close();
				} catch (IOException e) {
					logger.error("Error fileDownload "+e.toString());
				}
			}

		} else {
			result = "Not found File";
		}
		
		return result;
	}
	
	
	/**
	 * URL 이미지 저장
	 * @param url
	 * @param fileName
	 * @param path
	 * @return
	 */
	public static Map<String,String> urlImageDownload(String urlVal, String fileName, String path){
		logger.info(" urlImageDownload urlVal: "+urlVal+", fileName: "+fileName+", path: "+path);

		Map<String,String> result = new HashMap<String, String>();
		InputStream is = null;
		OutputStream os = null;
		
		String originalFileName = urlVal.substring(urlVal.lastIndexOf("/"));
		String fileExt = urlVal.substring(urlVal.lastIndexOf(".")+1);
		fileName = fileName + "." + fileExt;
		String filePath = path + "/" + fileName;
		long fileSize = 0;
		
		try {
			URL url = new URL(urlVal);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(120000);
			conn.setReadTimeout(120000);
			
			is = url.openStream();
			os = new FileOutputStream(new File(filePath));
			byte[] b = new byte[2048];
			int length;
			
			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}
			
			fileSize = new File(filePath).length();
			
			result.put("fileExt", fileExt);
			result.put("fileName", fileName);
			result.put("filePath", filePath);
			result.put("originalFileName", originalFileName);
			result.put("fileSize", ""+fileSize);
			result.put("result", "success");
			logger.info("result urlImageDownload : "+result);
		} catch (IOException e) {
			result.put("filePath", urlVal);
			result.put("fileSize", "0");
			result.put("result", "fail");
			logger.info("result urlImageDownload : "+result);
			logger.error("Error urlImageDownload : "+e.toString());
		}finally{
			try {
				if( is != null ) is.close();
				if( os != null ) os.close();
			} catch (IOException e) {
				logger.info("result urlImageDownload : "+result);
			}
		}
		return result;
	}
	
	
	
	/**
	 * 이미지 리자이징
	 * @param inputImagePath
	 * @param outputImagePath
	 */
	public static void resize(String inputImagePath, String outputImagePath) throws Exception{
		resize(inputImagePath, outputImagePath, 400, 96);
	}
	
	/**
	 * 이미지 리사이징
	 * @param inputImagePath
	 * @param outputImagePath
	 * @param maxSize
	 * @param dpi
	 */
	public static void resize(String inputImagePath, String outputImagePath, int maxSize, int dpi) throws Exception{
		logger.info(" resize start : inputImagePath: "+inputImagePath+", outputImagePath: "+outputImagePath+", maxSize: "+maxSize+", dpi: "+dpi);
		
		File inputFile = new File(inputImagePath);
		File outputFile = new File(outputImagePath);
		String fileExt = inputImagePath.substring(inputImagePath.lastIndexOf(".")+1);
	}
	
	public static BufferedImage toBufferedImage(Image image, int type) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		BufferedImage result = new BufferedImage(w, h, type);
		Graphics2D g = result.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return result;
	}

	/**
	 * 텍스트 파일을 이미지로 변환
	 * @param textList
	 * @param filePath
	 * @param fileName
	 * @param fileType
	 * @throws Exception
	 */
	public static void makeImage(List<String> textList, String filePath, String fileName, String fileType) 
			  throws Exception{

		mkdir(filePath);		
		File file = new File(filePath,fileName+"."+fileType);
		Font font = new Font("Batang", Font.PLAIN, 20);
		int width = getMaxRectangle2DWidth(font, textList)+40;
		int height = textList.size()*50;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		int textHeight = 40;
		for (int i = 0; i < textList.size(); i++) {
			g.drawString(textList.get(i), 20, textHeight*(i+1));
		}
		g.dispose();
		
		ImageIO.write(image, fileType, file);
	}
	
	/**
	 * List중 가장 긴 width 구하기
	 * @param font
	 * @param textList
	 * @return
	 */
	public static int getMaxRectangle2DWidth(Font font, List<String> textList) throws Exception{
		FontRenderContext frc = new FontRenderContext(null, true, true);
		Rectangle2D bounds = null;
		int maxWidth=0, tempWidth=0;
		for( String text : textList ){
			bounds = font.getStringBounds(text, frc);
			tempWidth = (int) bounds.getWidth();
			if( tempWidth > maxWidth )	maxWidth = tempWidth;
		}
		
		return maxWidth;
	}
	
	
	
	
}