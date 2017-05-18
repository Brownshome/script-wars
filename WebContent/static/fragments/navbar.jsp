<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
	<body>
		<jsp:useBean id="staticBean" class="brownshome.scriptwars.site.StaticBean" />
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
					<a class="navbar-brand" href="/">{S}</a>
				</div>

				<div class="collapse navbar-collapse" id="id-navbar-collapse">
					<ul class="nav navbar-nav">
						<li class="dropdown">
          					<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Games <span class="caret"></span></a>
          						<ul class="dropdown-menu">
	            					<c:forEach items="${staticBean.typeList}" var="gameType">
	            						<li><a href="/games/${gameType.name}">${gameType.name}</a></li>
	            					</c:forEach>
          						</ul>
       					</li>
						<li><a href="https://github.com/Brownshome/script-wars">GitHub</a></li>
						<li><a href="/doc/index.html">JavaDocs</a></li>
					</ul>
				</div>
			</div>
		</nav>
	</body>
</html>