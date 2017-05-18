<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html>
<head>
<!-- Bootstraps CSS and Custom CSS -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
	integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
	crossorigin="anonymous">
<link rel="stylesheet" href="./css/style.css">
<title>Script Wars</title>
</head>
<body>
	<div id="Top" class="container">
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
					<a class="navbar-brand" href="#Top">{S}</a>
				</div>

				<div class="collapse navbar-collapse" id="id-navbar-collapse">
					<ul class="nav navbar-nav">
						<li><a href="#Description">About</a></li>
						<li><a href="#Games">Games</a></li>
						<li><a href="#Contributing">Contribute</a></li>
						<li><a href="./doc/index.html">Javadocs</a></li>
					</ul>
				</div>
			</div>
		</nav>

		<div class="page-header">
			<h1>Script Wars</h1>
		</div>

		<h2 id="Description">Description</h2>
		<p>
			Script Wars is a AI design competition designed for university
			software students. <br> Participants write small AI programs
			using the supplied library and connect to a competition server. The
			AIs then compete against each other in simple games for glory.
		</p>
		<hr>

		<h2 id="Games">Available Games</h2>
		<div id="selectedGame"></div>

		<div class="row">
			<jsp:useBean id="staticBean" class="brownshome.scriptwars.site.StaticBean" />
			<c:forEach items="${staticBean.typeList}" var="gameType">
				<c:choose>
					<c:when test="${fn:length(staticBean.typeList) == 1}">
						<div class="col-sm-12 text-center">
					</c:when>
					<c:when test="${fn:length(staticBean.typeList) == 2}">
						<div class="col-sm-6 text-center">
					</c:when>
					<c:when test="${fn:length(staticBean.typeList) == 3}">
						<div class="col-md-4 col-sm-6 text-center">
					</c:when>
					<c:otherwise>
						<div class="col-md-3 col-sm-6 text-center">
					</c:otherwise>
				</c:choose>
				<img title="${gameType.name}" src="gameicons/${gameType.name}"
					alt="${gameType.name}" class="responsiveimg"
					style="width: 192px; height: 192px;"
					onclick="setGameTypeSelected('${gameType.name}')">
				<span class="badge">${gameType.playerCount}</span>
			</div>
			</c:forEach>
		</div>
		<hr>

		<jsp:include page="/static/fragments/howToCompete.html" />
		<hr>

		<h2 id="Contributing">How to Contribute</h2>

		<p>
			This project is in the early stages of it's development and you are
			encouraged to take an active role in it's development. The Github page
			for the site can be found <a href="https://github.com/Brownshome/script-wars">
			here</a> along with more information about how to contribute to the project. But to sum up
			the information, the project needs help writing the css pages for the
			site and creating games other than "Tank Game". Any other changes to
			the project to benefit ease of use and / or the user experience are
			also welcome.
		</p>
		<p>
		Also feel free to document any improvements or feedback <a
			href="https://github.com/Brownshome/script-wars/issues">here</a>.
		</p>
	</div>

	<footer>
		<div class="container">
			<p class="text-muted">Copyright &copy; James Brown</p>
		</div>
	</footer>

	<!-- Put the Javascript at the end to speed loading -->
	<script src="https://code.jquery.com/jquery-3.2.1.min.js"
		integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4="
		crossorigin="anonymous"></script>
	<script
		src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
		integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
		crossorigin="anonymous"></script>
	<script src="./static/script/mainPage.js"></script>
</body>
</html>