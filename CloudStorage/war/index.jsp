<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Distributed Storage System</title>
<script  src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script  src="http://malsup.github.io/jquery.form.js"></script>
</head>
	<body>
    <h1>Distributed Storage System</h1>
	<div id="result"></div>
	<form id="form" enctype="multipart/form-data">
	    <table>
	      <tr>
	        <td colspan="2" style="font-weight:bold;">Insert:</td>        
	      </tr>
	      <tr>	      
	        <td>Key:<input type="text" id="insertKey" name="key"/></td>
	        <td>File:<input type="file" id="insertValue" name="file"/></td>
	        <td><input type="button" value="Insert" id="insertValue" onClick="insert(event)"/></td>	      
	      </tr>
	    </table>
	    <input type="hidden" id="action" name="action"/>
    </form>
    
    <form id="fileholder" enctype="multipart/form-data" >
    	<input type="file" id="upload" style="display:none"/>
    
    </form>
    <script type="text/javascript">
    	function insert(event){
    		event.preventDefault();
    		$("#action").val("insert");
    		var data = new FormData();
    		var file = $("#insertValue")[0].files[0];
    		data.append("file",file);
	        $.ajax({
	        	url:'/uploadfile',
	        	data:data,
	        	type:'post',
	        	cache: false,
    			processData: false,
    		    contentType: false
	        }).done( function(result){
		        var form = $("#form");
		        $.ajax({
		        	url:'/distributedstorage',
		        	data:form.serialize(),
		        	type:'post'
		        });
		     });
	        
    		
    	}
    
    </script>
  </body>
</html>