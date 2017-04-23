/**
 * Handles the AJAX requests for the playerTable
 */

function updatePlayerList() {
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if(this.readyState == 4 && this.status == 200) {
			document.getElementById("playerTable").innerHTML = this.responseText;
		}
	};
	
	request.open("GET", "../playertable/" + internalSlotVariable, true);
	request.send();
}