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
		<jsp:include page="/static/fragments/navbar.jsp" />

		<div class="page-header">
			<h1>Script Wars</h1>
		</div>

		<h2>Description</h2>
		
		<p>Script wars is a site where you can write small scripts, in any language you want. These scripts
		then play simple games against the creations of other like-minded people, or if you are feeling
		brave, my own personal concoctions, for honour and glory (and also the chance to replace the top AI).</p>
			
		<p>The games are designed to have simple rule-sets and yet have potential for innovation and deep 
		tactics if you are feeling up to it. So while you can make something functional within 100 lines,
		you will need to put in some real elbow grease if you are looking for world domination.</p>
		
		<p>This site was designed to help my fellow software students hone their coding skills so some
		knowledge of at least one programming language is assumed. That being said there are fully functional
		coded examples given for every game, so feel free to copy paste and see what happens.</p>
		
		<p><em>~ James Brown</em></p>
		<hr>

		<h2>Available Games</h2>
		<div id="selectedGame"></div>

		<jsp:useBean id="staticBean" class="brownshome.scriptwars.site.StaticBean" />
		<div class="row">
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
				<a href="#"><img title="${gameType.name}" src="gameicons/${gameType.name}"
					alt="${gameType.name}" class="responsiveimg"
					style="width: 192px; height: 192px;"
					onclick="setGameTypeSelected('${gameType.name}')"></a>
				<span class="badge">${gameType.playerCount}</span>
				</div>
			</c:forEach>
			</div>
		<hr>

		<h2>How To Compete</h2>
		<div class="row">
			<div class="col-md-3">
			<div class="panel panel-primary">
				<div class="panel-heading">Library downloads, more coming soon!</div>
				<div class="list-group">
					<a class="list-group-item" href="/static/script-wars-client.jar">Java library</a>
					<a class="list-group-item" href="/static/script-wars-python.zip">Python library</a>
				</div>
			</div>
			</div>
			<div class="col-md-9">
				<p>To take part, first download the library of your choice from the list on the left and include
				it in the project. For example in java this can be done by adding <code>script-wars-client.jar</code>
				to your classpath. This can be done by clicking <code>Properties/Java Build Path/Libraries/Add External JARs...</code> in
				eclipse or using the <code>-classpath .;script-wars-client.jar</code> flag on the command line tools.</p>
				
				<p>Once you have grabbed your library of choice select a game from the section above and use the examples, information and
				documentation there to make a world conquering AI. Press the join button and use the ID given to you to connect
				to the site by passing it to the library you downloaded. For example the low level Java library asks for the ID
				in the constructor as follows <code>network = new Network(ID, &quot;www.script-wars.com&quot;, &quot;John Smith Low Level&quot;);</code></p>
			
				<p><strong>Please note that languages other than Java may only have access to the low level library.</strong></p>
				
				<p>More information can be found on the individual game pages and in the project JavaDocs found at the top of this page.</p>
			</div>
		</div>
		<hr>

		<h2>How to Contribute</h2>

		<p>This project is in the early stages of it's development and you are
		encouraged to take an active role in it's development. The GitHub page
		is linked at the top of the page and contains detailed information about
		what is needed and how to contribute to the project. </p>
			
		<p>There are several parts of the project that need assistance. Firstly I would dearly like to
		have more games than <Strong>Tank Game</strong>. This requires basic knowledge of java and a cool
		game idea. Secondly libraries in other languages are something I am always looking for, this
		is a little bit more involved and will require low level UDP and/or TCP knowledge.</p>
		
		<p>Other than those specific things please feel free to report any bugs you find, improvements you want or cool
		ideas you have by submitting an issue <strong><a href="https://github.com/Brownshome/script-wars/issues">here</a></strong>.
		</p>
		<hr>
		
		<h2>Credits</h2>
		
		<p>I could not have done this without the following people chipping in and helping out, I am eternally grateful.</p>
		<ul>
			<li><a href="https://github.com/Evander7">Evander7</a> for writing the Python API.</li>
			<li><a href="https://github.com/liamtbrand">Liam</a> for persuading me that a high level API was required and then
			writing one when I was too lazy.</li>
			<li><a href="https://github.com/Ravid12">Ravid</a> for introducing me to the wonderful world of Bootstrap and taking the
			 first crack at the css.</li>
		</ul>
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