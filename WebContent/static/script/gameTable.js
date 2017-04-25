/**
 * Handles the updates and logic for the game table
 */
var internalSlotVariable = null;

function setActiveStatus(slot) {
	internalSlotVariable = slot;
	
	for(index = 0; ; index++) {
		var element = document.getElementById('entry-' + index);
		if(element == null) {
			break;
		}
		
		if(index == slot) {
			element.classList.add('info');
		} else {
			element.classList.remove('info');
		}
	}
}

function updateGameTable() {
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if(this.readyState == 4 && this.status == 200) {
			document.getElementById("gameTable").innerHTML = this.responseText;
			if(internalSlotVariable != null) {
				setActiveStatus(internalSlotVariable);
			}
		}
	};
	request.open("GET", "../static/fragments/gameTable.jsp", true);
	request.send();
}