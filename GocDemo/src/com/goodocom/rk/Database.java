package com.goodocom.rk;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database {
	
	//calltype: 
	private static String mDbPath="/data/data/com.goodocom.rk/BtPhone.db";
	static String Sql_create_phonebook_tab="create table if not exists phonebook(_id integer primary key autoincrement,phonename text,phonenumber text)";  
	static String Sql_create_calllog_tab="create table if not exists calllog(_id integer primary key autoincrement,phonename text,phonenumber text,calltype integer,time long)";
	static String PhoneBookTable="phonebook";
	static String CallLogTable="calllog";
	
	//get the database
	public static SQLiteDatabase getPhoneBookDb(){  
		
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(mDbPath,null); 
		return db;
	}
	
	//create table
	public static void createTable(SQLiteDatabase db, String Sql_table_name){  
		//执行SQL语句   
		db.execSQL(Sql_table_name);  
	}  
	
	//insert phonebook data to table
	public static void insert_phonebook(SQLiteDatabase db, String Table_name, String Phone_name, String Phone_num) {  
	    //实例化常量值  
	    ContentValues cValue = new ContentValues();  
	    cValue.put("phonename", Phone_name);  
	    cValue.put("phonenumber", Phone_num);  
	    db.insert(Table_name,null,cValue);  
	}  
	
	//insert call data to table
	public static void insert_calllog(SQLiteDatabase db, String Table_name, String Phone_name, String Phone_num, int calltype, long time){
		ContentValues cValue = new ContentValues();  
	    cValue.put("phonename", Phone_name);  
	    cValue.put("phonenumber", Phone_num); 
	    cValue.put("calltype", calltype);
	    cValue.put("time", time);
	    db.insert(Table_name,null,cValue);  
	}
		
	//search name with num
	static String queryPhoneName(SQLiteDatabase db, String Table_name, String Phone_num)  
	{  
	    //查询获得游标  
	    //参数1：表名    
	    //参数2：要想显示的列    
            //参数3：where子句    
            //参数4：where子句对应的条件值    
            //参数5：分组方式    
            //参数6：having条件    
            //参数7：排序方式    
		String Phonename;
		String Phonenum;
	        Cursor cursor = db.query("phonebook", new String[]{"phonename","phonenumber"}, "phonenumber=?", 
        		new String[]{Phone_num}, null, null, null);  //"ORDEY BY ASC"
		
        
        int count = cursor.getCount();
        
        while(cursor.moveToNext()){  
        	Phonenum = cursor.getString(cursor.getColumnIndex("phonenumber"));
          
            	Phonename = cursor.getString(cursor.getColumnIndex("phonename")); 
            	return Phonename;
        }  
 
	   return null;
	}  
	
	
	static void queryCallLog(SQLiteDatabase db, String CallType)  
	{  
	    //查询获得游标  
		//参数1：表名    
        //参数2：要想显示的列    
        //参数3：where子句    
        //参数4：where子句对应的条件值    
        //参数5：分组方式    
        //参数6：having条件    
        //参数7：排序方式    
		String Phonename;
		String Phonenum;
		String time;
        Cursor cursor = db.query("calllog", new String[]{"phonename","phonenumber","time"}, "calltype=?", 
        		new String[]{CallType}, null, null, null);  //"ORDEY BY ASC"
		  
        int count = cursor.getCount();
        
        while(cursor.moveToNext()){  
        	Phonenum = cursor.getString(cursor.getColumnIndex("phonenumber"));
            Phonename = cursor.getString(cursor.getColumnIndex("phonename")); 
            time = cursor.getString(cursor.getColumnIndex("time")); 
            Log.d("Database", "Cursor data =============="+Phonenum+Phonename+"  "+time);
        }  
	}  
	
	static void delete_table_data(SQLiteDatabase db, String Table_name) {  
	   //删除SQL语句  
	   String sql = "delete from " + Table_name;  
	   //执行SQL语句  
	   db.execSQL(sql);  
	}
	
	//delete table
	static void drop(SQLiteDatabase db, String Table_name){          
	    //删除表的SQL语句        
		String sql ="DROP TABLE "+Table_name;           
	    //执行SQL       
	    db.execSQL(sql);     
	}   
}
