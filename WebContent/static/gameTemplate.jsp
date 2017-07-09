<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
	<title>Script Wars - ${gametype.name}</title>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="../css/style.css">
	<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.11.0/styles/default.min.css">
</head>
<body onload="onLoad('${gametype.name}')">
	<div class="container">
		<jsp:include page="/static/fragments/navbar.jsp" />

		<div class="page-header">
			<h1>Game Type - ${gametype.name}</h1>
		</div>
	
		<div class="row">
			<div class="col-md-7">
				<canvas id="gameCanvas" width="1024" class="img-responsive" height="1024" style="border:1px solid black"></canvas>
			</div>
			
			<div id="playerTable" class="col-md-5"></div>
		</div>
		<hr>
		
		<h2>Active Games</h2>
		<div id="gameTable">
			<jsp:include page="/static/fragments/gameTable.jsp" />
		</div>
		<hr>
		
		<jsp:include page="/static/games/${gametype.name}/page.jsp" />
	</div>
	
	<footer>
		<div class="container">
			<p class="text-muted">Copyright &copy; James Brown</p>
		</div>
	</footer>
	
	<script src="../static/script/gamePage.js"></script>
	<script src="../static/script/displayHandler.js"></script>
	<script src="../static/script/gridDisplayHandler.js"></script>
	<script src="../static/games/${gametype.name}/displayScript.js"></script>
	<!-- Put the Javascript at the end to speed loading -->
	<script src="https://code.jquery.com/jquery-3.2.1.min.js" integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
	<script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.11.0/highlight.min.js"></script>
	<script>hljs.initHighlightingOnLoad();</script>
</body>
</html>