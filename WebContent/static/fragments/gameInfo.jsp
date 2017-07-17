<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="root" value="." scope="request"/>

<div class="jumbotron">
	<button type="button" class="close" id="jumboclosebutton" aria-label="Close" onclick="closeGameInfo()">
		<span aria-hidden="true">&times;</span>
	</button>
	
	<div class="row">
		<div class="col-md-3 center-block">
			<img src="${root}/gameicons/${gameType.name}" alt="${gameType.name}" 
			class="responsiveimg" style="width:192px;height:192px;">
		</div>
		
		<div class="col-md-4">
			<div class="list-group">
				<div class="list-group-item">
				<strong>Difficulty</strong><br>
				${gameType.difficulty}
				</div>
				<div class="list-group-item">
				<strong>Language</strong><br>
				${gameType.language}
				</div>
				<div class="list-group-item">
				<strong>Players</strong><br>
				${gameType.playerCount}
				</div>
			</div>
		</div>
		
		<div class="col-md-5"><div class="pannel pannel-default">
			<h3>${gameType.name}</h3>
			<p>${gameType.description}</p>
			<p><a class="btn btn-primary btn-lg" href="${root}/games/${gameType.name}">Join</a></p>
		</div></div>
	</div>
</div>