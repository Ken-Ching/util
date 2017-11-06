package kc.team.ie8.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImageShowServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("content-type","text/json;charset=UTF-8"); 
		resp.setHeader("Content-Language","zh-CN"); 
		resp.setCharacterEncoding("UTF-8");
	}
	
	

}
