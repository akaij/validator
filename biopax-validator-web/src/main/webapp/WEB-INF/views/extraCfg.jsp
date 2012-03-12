<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
	<script type="text/javascript"> <!-- this function is here (not in a separate .js file) for off-line use -->
		function switchit(list) {
			var listElementStyle = document.getElementById(list).style;
			if (listElementStyle.display == "none") {
				listElementStyle.display = "block";
			} else {
				listElementStyle.display = "none";
			}
		}
	</script>
	<title>Additional Configuration</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">

<h2>Additional Configuration</h2>
(defined in rules-context.xml)
<p/>
<h3>For XrefRule, XrefSynonymDbRule rules</h3>
Some db synonyms/spellings are configured in the xml file,
and these then completed and new groups created automatically -
from the Miriam resource and MI ("database citation" terms). 
The first name in each group is the preferred one.<br />
Click <a href="javascript:switchit('syngroups')">here to show/hide</a> 
the list:<br />
 <ol id="syngroups" style="list-style: inside; display: none" >
  <c:forEach var="g" items="${extraDbSynonyms}">
	<li>group:<ol>
	  <c:forEach var="db" items="${g}">
	    <li>${db}</li>
      </c:forEach>
    </ol></li>
    <br />
  </c:forEach>
 </ol>
<p/>
<hr />

<h3>For DataPropertyIllegalValueRule rule</h3>
Auto-recognized "illegal" values (perhaps, the property should be empty instead):
<p/>
  <ol>
	<c:forEach var="v" items="${warnOnDataPropertyValues}">
		<li>${v}</li>
	</c:forEach>
  </ol>
<p/>  
<hr />

<h3>For UnificationXrefLimitedRule rule</h3>
Allowed unif. xref.db values (synonyms are also ok), by parent class:
  <table border>
  	<tr><th>class</th><th>db names (&apos;:&apos; separated)</th></tr>
	<c:forEach var="entry" items="${dbAllow}">
		<tr><td>${entry.key.simpleName}</td><td>${entry.value}</td></tr>
	</c:forEach>
  </table>
  <br />
Not recommended unif. xref.db values (synonyms are not ok either), by parent class:
  <table border>
  <tr><th>class</th><th>db names (&apos;:&apos; separated)</th></tr>
	<c:forEach var="entry" items="${dbDeny}">
		<tr><td>${entry.key.simpleName}</td><td>${entry.value}</td></tr>
	</c:forEach>
  </table>
<p/>  
<hr />

<h3>For DisplayNameRule rule</h3>
Recommended max display name length by class:
  <table border>
    <tr><th>class</th><th>max displayName</th></tr>
	<c:forEach var="entry" items="${maxDisplayNameLengths}">
		<tr><td>${entry.key.simpleName}</td><td>${entry.value}</td></tr>
	</c:forEach>
  </table>
  <br />
<p/>  

  </div>
    <div id="right">
      <jsp:include page="/templates/menu.jsp"/>
      <jsp:include page="/templates/box.jsp"/>
    </div>
    <div id="clear"></div>
  </div>

  <jsp:include page="/templates/footer.jsp"/>
</div>

</body>
</html>