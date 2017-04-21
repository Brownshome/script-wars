/**
 * Handles the scripting on the main page
 */

function setGameTypeSelected(gameName) {
	var request = new XMLHttpRequest();
	request.open('GET', './gameinfo/' + gameName, true);
	request.onreadystatechange = function() {
		if(request.status == 200 && request.readyState == 4) {
			document.getElementById("selectedGame").innerHTML = request.responseText;
		}
	};
	request.send();
}

function closeGameInfo() {
	document.getElementById("selectedGame").innerHTML = "";
}