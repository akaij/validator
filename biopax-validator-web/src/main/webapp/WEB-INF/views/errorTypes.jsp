<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
<head>
	<title>BioPAX Validator: errors</title>
	<jsp:include page="head.jsp"/>
</head>
<body>

<jsp:include page="header.jsp"/>

<h2>Validation Error Classes</h2>

<div class="row">
<div class="jumbotron">
<p>
Every class of error &lt;code&gt; is defined in the error-codes.properties as follows: 
</p>
<ul>
<li><b>&lt;code&gt;.default</b>=<em>a common message to show for all such cases</em></li>
<li><b>&lt;code&gt;</b>=<em>a specific message template with optional parameters, e.g.: property={0}, value={1}. 
(Note: the id of the associated with the error case object will be added to the error message automatically, in all cases!)</em></li>
<li><b>&lt;code&gt;.category</b>=<em>one of: syntax, specification, recommendation, information</em></li>
</ul>
</div>
</div>

<div class="row">
<h3>
Following error classes (codes) can be reported by the BioPAX rules:
</h3>
<dl>
  <c:forEach var="err" items="${errorTypes}">
    <dt>code:&nbsp;<dfn>${err.code}</dfn>, category:&nbsp;<dfn>${err.category}</dfn></dt>
    <dd><p>common message:&nbsp;<dfn>${err.defaultMsg}</dfn><br/>
        case-specific template:&nbsp;<code>${err.caseMsgTemplate}</code></p>
	</dd>
  </c:forEach>
</dl>
</div>

<jsp:include page="footer.jsp"/>

</body>
</html>