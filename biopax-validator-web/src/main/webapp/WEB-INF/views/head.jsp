<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<meta charset="utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="author" content="BioPAX" />
<meta name="description" content="BioPAX Validator" />
<meta name="keywords" content="BioPAX, Validation, Validator, Rule, OWL, RDF, Exchange" />
<link rel="shortcut icon" href="'<spring:url value='/images//favicon.ico'/>'"/>
<link rel="stylesheet" type="text/css" href="<spring:url value='/css/bootstrap.min.css'/>"/>
<link rel="stylesheet" type="text/css" href="<spring:url value='/css/biopax.validator.css'/>" />
<!-- HTML5 shim, for IE6-8 support of HTML5 elements. All other JS at the end of file. -->
<!--[if lt IE 9]>
    <script src="<spring:url value='/js/html5shiv.js'/>"></script>
<![endif]-->
<script type="text/javascript" src="<spring:url value='/js/jquery.min.js'/>"></script>
<script type="text/javascript" src="<spring:url value='/js/bootstrap.min.js'/>"></script>
<script type="text/javascript" src="<spring:url value='/js/jquery.placeholder.js'/>"></script>
<script type="text/javascript" src="<spring:url value='/js/biopax.validator.js'/>"></script>
<script>
  <%-- get the server-side jvm property --%>
  var gaCode = "<%= System.getProperty("biopax.validator.ga.code") %>";
  if(gaCode) {
  	(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  	(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  	m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  	})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
  	ga('create', gaCode, 'auto');
  	ga('send', 'pageview');
  }
</script>
<!--[if lt IE 8]>
	<script src="<spring:url value='/js/icon-font-ie7.js'/>"></script>
	<script src="<spring:url value='/js/lte-ie7-24.js'/>"></script>
<![endif]-->
