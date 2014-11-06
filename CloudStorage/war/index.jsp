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
          </table>

    </form>
	<form id="listFile" name="listFile" enctype="multipart/form-data">
          <table>
            <tr>
          	<td><input type="text" name ="filePrefix" placeholder="file prefix"/></td>			
            	<td><input type="button" onclick='listAllFiles()' value="List Content" name="list"/></td>
            </tr>
          </table>

          <table class="fileTable" id="fileTableID">
 	 	<tr class="fileTable">
    	  		<th class="fileTable">File name</th>
    	  		<th class="fileTable">File size</th>
    	  		<th class="fileTable">Download</th>		
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
	
	function listAllFiles() {
		var filePrefix = document.forms["listFile"]["filePrefix"].value;
		$.get("/gcs/" + bucket + "/list/" + filePrefix,function(data){
			alert(data);
			var resultArray = data.split(/\n/);
			resultArray.forEach(function(entry) {
				var fileAttrs = entry.split("/");
				$("#fileTableID").append("<tr><td>" + fileAttrs[0] + "</td><td>" + fileAttrs[1] + "</td><td><input type='button' value='download'/></td></tr>" );
			});
		  });		
	}
    </script>
  </body>
</html>
