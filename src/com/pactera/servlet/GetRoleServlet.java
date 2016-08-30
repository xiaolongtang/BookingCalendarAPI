package com.pactera.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pactera.util.RoleUtil;


public class GetRoleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public GetRoleServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RoleUtil ru=new RoleUtil();
//		System.out.println(ru.checkGroupMembership("feea4630-59c5-4eb4-b263-a182ab5097ed","5abf9656-604d-4d80-ad4f-04b1a4eed78d"));
//		System.out.println(ru.checkGroups("5abf9656-604d-4d80-ad4f-04b1a4eed78d"));
		String role=ru.checkGroups("5abf9656-604d-4d80-ad4f-04b1a4eed78d");
		String json="{\"role\":\""+role+"\"}";
		returnWriter(response,json);
	}

    private void returnWriter(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(json);
    }
}
