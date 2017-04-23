<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Script Wars - ${gametype.name}</title>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="../css/style.css">
</head>
<body onload="onLoad('${gametype.name}')">
	<div class="container">
		<nav class="navbar navbar-default navbar-fixed-top">
			<div class="container">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle collapsed"
						data-toggle="collapse" data-target="#id-navbar-collapse"
						aria-expanded="false">
						<span class="sr-only">Toggle navigation</span> <span
							class="icon-bar"></span> <span class="icon-bar"></span> <span
							class="icon-bar"></span>
					</button>
					<a class="navbar-brand" href="../..">{S}</a>
				</div>

				<div class="collapse navbar-collapse" id="id-navbar-collapse">
					<ul class="nav navbar-nav">
						<li><a href="../doc/index.html">Javadocs</a></li>
					</ul>
				</div>
			</div>
		</nav>

		<div class="page-header">
			<h1>Game Type - ${gametype.name}</h1>
		</div>
	
		<div class="row">
			<div class="col-md-7">
				<canvas id="gameCanvas" width="512" height="512"></canvas>
			</div>
			
			<div id="playerTable" class="col-md-5"></div>
		</div>
		<hr>
		
		<h2>Active Games</h2>
		<div id="gameTable">
			<jsp:include page="/static/fragments/gameTable.jsp" />
		</div>
		<hr>
	
		<jsp:include page="/static/fragments/howToCompete.html"/>
		<hr>
		
		<jsp:include page="/static/games/${gametype.name}/rules.jsp" />
	</div>
	
	<footer>
		<div class="container">
			<p class="text-muted">Copyright &copy; James Brown</p>
		</div>
	</footer>
	
	<script src="../static/script/gameViewer.js" async></script>
	<script src="../static/script/gameTable.js" async></script>
	<script src="../static/script/IDRequest.js" async></script>
	<script src="../static/script/playerTableHandler.js" async></script>
	<script src="../static/games/${gametype.name}/displayScript.js" async></script>
	<!-- Put the Javascript at the end to speed loading -->
	<script src="https://code.jquery.com/jquery-3.2.1.min.js" integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
</body>
</html>