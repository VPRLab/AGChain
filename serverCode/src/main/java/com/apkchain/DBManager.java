package com.apkchain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Servlet implementation class DBManager
 */
public class DBManager extends HttpServlet {

    ServletConfig config;                             //����һ��ServletConfig����
    private static String username;                   //��������ݿ��û���
    private static String password;                   //��������ݿ���������
    private static String url;                        //�������ݿ�����URL
    private static Connection connection;             //��������
    private static String JDBC_DRIVER;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);                                  //�̳и����init()����
        this.config = config;                                //��ȡ������Ϣ
        username = "root";  //��ȡ���ݿ��û���
        password = "agchain";
        JDBC_DRIVER = "com.mysql.jdbc.Driver";
        url = "jdbc:mysql://localhost:3306/AGChain?characterEncoding=utf8&useSSL=false";   //��ȡ���ݿ�����URL
    }

    /**
     * ������ݿ����Ӷ���
     *
     * @return ���ݿ����Ӷ���
     */
    public static Connection getConnection() {
        try {
        	Class.forName(JDBC_DRIVER);
        	System.out.println("�������ݿ�...");
        	connection = DriverManager.getConnection(url,username,password);
        } catch (Exception ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return connection;
    }

    /**
     * �ر����е����ݿ�������Դ
     *
     * @param connection Connection ����
     * @param statement Statement ��Դ
     * @param resultSet ResultSet �������
     */
    public static void closeAll(Connection connection, Statement statement,
            ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
