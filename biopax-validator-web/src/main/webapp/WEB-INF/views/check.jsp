<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8" />
	<meta name="author" content="BioPAX" />
	<meta name="description" content="BioPAX Validator" />
	<meta name="keywords" content="BioPAX, Validation, Validator, Rule, OWL, Exchange" />
	<link rel="stylesheet" type="text/css" href="styles/style.css" media="screen" />
	<link rel="shortcut icon" href="images/favicon.ico" />
	<script type="text/javascript" src="scripts/rel.js"></script>
	<script type="text/javascript">
	  function switchInput() {
		  var f = document.getElementById('file');
		  var u = document.getElementById('url');
		  var cb = document.getElementById('switch');
		  if(cb.checked == true) {
			f.disabled = false;
			u.disabled = true;
			u.value = null;
			document.getElementById('urlMsg').innerHTML = null;
		  } else {
			f.disabled = true;
			f.value = null;
			u.disabled = false;
		  }
	  };

	  function isUrl(s) {
			var regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
			return regexp.test(s);
	  };

	  function validate() {
		  var u = document.getElementById('url');
		  if(!u.disabled && !isUrl(u.value)) {
			  document.getElementById('urlMsg').innerHTML = 'Malformed URL!';
			  return false;
		  }
	  };
	  
	</script>
	<title>Validate from URL</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">

<h2>Check BioPAX<b>*</b></h2>
<form method="post" enctype="multipart/form-data" onsubmit="return validate();">
    <div class="form-row" style="padding-top: 1em;">
    	<input type="radio" id="switch" name="switch" checked="checked" onchange="switchInput();" value="fdfdfdf"/>
    	<label>Choose up to 25 BioPAX files:</label>
		<input id="file" type="file" name="file_0" accept="application/rdf+xml"/>
		<div id="files_list" ></div>
		<br/>
		<input type="radio" id="switch" name="switch" onchange="switchInput();"/>
		<label>Check a BioPAX OWL at the location:</label>
		<br/>
        <input id="url" class="input" type="text" name="url" size="80%" disabled="disabled"/>
        <br/>
        <label style="color: red;" id="urlMsg">${error}</label>
    </div>
	<div class="form-row" style="padding-top: 2em;">
		Options:<br/>
		<input type="checkbox" name="autofix" value="true"/>
		<label>Auto-Fix! (<b>experimental:</b> some rules can, e.g., fix db/id, set 'displayName', remove duplicates, etc.)</label>
		<br/>
		<input type="checkbox" name="normalize" value="true"/>
		<label>Normalize! (<b>experimental:</b> where it is possible, - replaces identifiers of entity references, CVs, BioSource 
		with Miriam's standard URNs; for xrefs, - generates new IDs like <em>urn:biopax:*Xref:&lt;db&gt;_&lt;id&gt;_&lt;ver&gt;</em> this makes BioPAX data integration and linking easier. 
		if required, data are auto-converted to BioPAX Level3 first. It may remove some or add new BioPAX errors, especially if there were errors in the file.)</label>
		<br/>
		<div class="form-row" style="padding-top: 2em;">
		<label>Filter (check/fix, but not necessarily mention it)</label><br/>
		<select name="filter">
			<option label="Show Warnings and Errors" value="WARNING" selected="selected">Show Warnings and Errors</option>
			<option label="Show Errors Only" value="ERROR">Show Errors Only</option>
			<option label="Do not Show Me Problems :)" value="IGNORE">Do not Show Me Problems :)</option>
		</select>
		</div>
	</div>
		<div class="form-row" style="padding-top: 2em;">
		Report as:
		<br/>
		<input type="radio" name="retDesired" value="html" checked="checked"/>
		<label>HTML</label>
		<br/>
		<input type="radio" name="retDesired" value="xml"/>
		<label>XML (<a href="<c:url value='/ws.html'/>">unmarshalable</a>)</label>
		<br/>
		<input type="radio" name="retDesired" value="owl"/>
		<label>Modified BioPAX (only)</label>
	</div>
	<div class="form-buttons" style="padding-top: 2em;">
        <div class="button"><input name="submit" type="submit" value="Submit"/></div>
	</div>   
</form>
	
	
    </div>
    <div id="right">
      <jsp:include page="/templates/menu.jsp"/>
      <jsp:include page="/templates/box.jsp"/>
    </div>
    <div id="clear"></div>
  </div>
  <jsp:include page="/templates/footer.jsp"/>
</div>

<script type="text/javascript" src="scripts/multifile_compressed.js"></script>
<script type="text/javascript">
  var multi_selector = new MultiSelector( document.getElementById( 'files_list' ), 25);
  multi_selector.addElement( document.getElementById( 'file' ) );
</script>

</body>
</html>

