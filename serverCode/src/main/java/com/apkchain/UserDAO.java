package com.apkchain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UserDAO {

	public static APK query(String pkgName) {
        //������ݿ�����Ӷ���
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        //����SQL����
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT * FROM CertInfo WHERE PackageName=?");

        //�������ݿ���ֶ�ֵ
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, pkgName);
            
            resultSet = preparedStatement.executeQuery();
            APK apk_file = new APK();
            if (resultSet.next()) {
            	System.out.println("Enter in the result set");
            	System.out.println(resultSet.getString("PackageName"));
            	System.out.println(resultSet.getString("CertID"));
            	apk_file.setPackageName(resultSet.getString("PackageName"));
            	apk_file.setCetID(resultSet.getString("CertID"));

                return apk_file;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
    }
	
	// query txn to check if this txn has been used before
	public static boolean ExistTxn(String txn_ID) {
		//������ݿ�����Ӷ���
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        //����SQL����
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT * FROM Txn WHERE TxnID=?");

        //�������ݿ���ֶ�ֵ
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, txn_ID);
            
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
            	//There exists such txn record
                return true;
            } else {
            	//There is no such txn record
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
	}
	
	//if this txn is not used insert the txn record
		public static boolean insertTxn(String txn_ID, String pkgName, String version,String hash) {
			if(!ExistTxn(txn_ID)) {
				//������ݿ�����Ӷ���
		        Connection connection = DBManager.getConnection();
		        PreparedStatement preparedStatement = null;
		        ResultSet resultSet = null;

		        //����SQL����
		        StringBuilder sqlStatement = new StringBuilder();
		        sqlStatement.append("INSERT INTO Txn (TxnID,PkgName,Version,Hash) VALUES(?,?,?,?)");

		        //�������ݿ���ֶ�ֵ
		        try {
		            preparedStatement = connection.prepareStatement(sqlStatement.toString());
		            preparedStatement.setString(1, txn_ID);
		            preparedStatement.setString(2, pkgName);
		            preparedStatement.setString(3, version);
		            preparedStatement.setString(4, hash);
		            
		            preparedStatement.executeUpdate();
		            return true;
		        } catch (SQLException ex) {
		            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
		            return false;
		        } finally {
		            DBManager.closeAll(connection, preparedStatement, resultSet);
		        }
			}else {
				return false;
			}
			
		}
	
	/**
	 * Check APK Duplicate
	 * @param hash
	 * @return
	 */
	public static boolean ExistAPK(String hash) {
		//������ݿ�����Ӷ���
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        //����SQL����
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT * FROM Txn WHERE Hash=?");

        //�������ݿ���ֶ�ֵ
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, hash);
            
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
            	//The APK already exists
                return true;
            } else {
            	//The APK doesn't exists
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
	}
	
	/**
	 * check if this APK have the former version
	 * @param pkgName
	 * @return
	 */
	public static boolean ExistPkgName(String pkgName) {
		//������ݿ�����Ӷ���
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        //����SQL����
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT * FROM Txn WHERE PkgName=?");

        //�������ݿ���ֶ�ֵ
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, pkgName);
            
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
            	//The APK already exists
                return true;
            } else {
            	//The APK doesn't exists
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
	}
	
	/**
	 * Update the IPFS hash of APK in DB from default to specific value
	 * @param hash
	 * @param txn
	 */
	
	public static void updateHash(String hash,String txn) {
		//������ݿ�����Ӷ���
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        //����SQL����
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("UPDATE Txn SET Hash=? WHERE TxnID=?");

        System.out.println(sqlStatement);
        //�������ݿ���ֶ�ֵ
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, hash);
            preparedStatement.setString(2, txn);
            int num=preparedStatement.executeUpdate();
            if(num>0){
                //�������ɹ������ӡsuccess
                System.out.println("Sucess");
            }else{
                //�������ʧ�ܣ����ӡFailure
                System.out.println("Failure");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
	}
	
	
}
