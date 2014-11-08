<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Distributed Storage System</title>
<script  src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script  src="http://malsup.github.io/jquery.form.js"></script>
<style>
[class="fileTable"] {
    border: 1px solid black;
}
[class="listRow"] {
    border: 1px solid black;
}

</style>
</head>
	<body>
    <h1>Distributed Storage System</h1><br>
	<div id="result" name="result"></div><br>
	<form id="putFile" name="putFile" enctype="multipart/form-data">
	    <table>
	      <tr>
	        <td colspan="2" style="font-weight:bold;">Insert:</td>        
	      </tr>
	      <tr>	      
	        <td>File:<input type="file" id="fileName" name="fileName"/></td>
	        <td><input type="button" value="Insert" id="insertValue" name="insertButton" onClick="insert()"/></td>	      
	      </tr>
	    </table>
	    <input type="hidden" id="action" name="action"/>
    </form>
    
    <form action="#" method="get" name="submitGet">
          <table>
            <tr>
          	<td><input type="text" name ="fileName" placeholder="file name"/></td>
            	<td><input type="submit" onclick='changeGetPath(this)' value="Download Content" name="download"/></td>
            </tr>
            
	    <tr>
          	<td><input type="text" name ="existFileName" placeholder="file name"/></td>
            	<td><input type="button" onclick='checkExist(this)' value="Check Exist" name="download"/></td>
            </tr>
            <tr>
          	<td><input type="text" name ="deleteFileName" placeholder="file name"/></td>
            	<td><input type="button" onclick='deleteFile(this)' value="Delete" name="download"/></td>
            </tr>
          </table>

    </form>
	<form id="listFile" name="listFile" enctype="multipart/form-data">
          <table>
            <tr>
          	<td><input type="text" name ="filePrefix" placeholder="keyword"/></td>			
            	<td><input type="button" onclick='listAllFiles()' value="List Content" name="list"/></td>
            </tr>
          </table>

          <table class="fileTable" id="fileTableID">
 	 	<tr class="fileTable">
    	  		<th class="fileTable">File name</th>
    	  		<th class="fileTable">File size</th>
  		</tr>
          </table>
    </form>
    
   
    <script type="text/javascript">
    	var bucket = "my-project";
    	function insert(){
    		$("#action").val("insert");
    		
            var filename = document.forms["putFile"]["fileName"].value;
            if (bucket == null || bucket == "" || filename == null || filename == "") {
              alert("Both Bucket and FileName are required");
              return false;
            } else {
              var postData = new FormData(document.forms["putFile"]);
              var request = new XMLHttpRequest();
              request.open("POST", "/gcs/" + bucket + "/" + filename, false);
              //request.setRequestHeader("Content-Type", "text/plain;charset=UTF-8");
              request.send(postData);
              $('#result').innerHTML = "Uploading";
              if(request.response!=null){
            	  document.forms["putFile"]["fileName"].value="";
            	  $('#result')[0].innerHTML = "Insert Complete";
              }
              else{
            	  document.forms["putFile"]["fileName"].value="";
            	  $('#result')[0].innerHTML = "Insert Failure";
              }
            }
    	}
    	
    	function changeGetPath() {
            var filename = document.forms["submitGet"]["fileName"].value;
            if (bucket == null || bucket == "" || filename == null || filename == "") {
              alert("Both Bucket and FileName are required.");
              return false;
            } else {
              document.submitGet.action = "/gcs/" + bucket + "/download/" + filename;
            }
        }
	
	function downloadFile() {
              document.submitGet.action = "/gcs/" + bucket + "/download/" + "aaa";
		
	}
	
	function checkExist() {
         	var filename = document.forms["submitGet"]["existFileName"].value;
		var request = new XMLHttpRequest();
            	request.open("GET", "/gcs/" + bucket + "/check/" + filename, false);
		request.send();
		alert(request.response);
	}
	function deleteFile() {
         	var filename = document.forms["submitGet"]["deleteFileName"].value;
		var request = new XMLHttpRequest();
            	request.open("GET", "/gcs/" + bucket + "/delete/" + filename, false);
		request.send();
		alert(request.response);
	}
	function listAllFiles() {
		var keyword = document.forms["listFile"]["filePrefix"].value;
		var request = new XMLHttpRequest();
            	request.open("GET", "/gcs/" + bucket + "/list/" + keyword, false);
		request.send();
		var data = request.response;
		alert(request.response);
		$(".listRow").remove();
		var resultArray = data.split(/\n/);
		for(i = 0; i < resultArray.length - 1; i++) {
			var fileAttrs = resultArray[i].split("/");
			$("#fileTableID").append("<tr class='listRow'><td class='listRow'>" + fileAttrs[0] + "</td><td class='listRow'>" + fileAttrs[1] + "</td></tr>" );
		}
	  }
    </script>
  </body>
</html>
