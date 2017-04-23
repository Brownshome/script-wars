/**
 * Handles the requesting of IDs from the server
 */

//Requests an connectionID value from the server using AJAX
function requestID(type) {
	var request = new XMLHttpRequest();
	request.onreadystatechange = IDAJAXComplete;
	request.open("POST", "../requestID", true);
	request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	request.send("type=" + type);
}

function requestSpecificID(slot) {
	var request = new XMLHttpRequest();
	request.onreadystatechange = IDAJAXComplete;
	request.open("POST", "../requestID", true);
	request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	request.send("slot=" + slot);
}

function IDAJAXComplete() {
	if(this.readyState == 4 && this.status == 200) {
		alert("Your ID is " + this.responseText);
	}
}