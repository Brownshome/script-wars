var GameTable = {
		selectedSlot: null,
		
		clickOnSlot: function(slot) {
			GameTable.selectedSlot = slot;
			GameTable.highlightSlot();
		},
		
		table: document.getElementById("gameTable"),
		
		highlightSlot: function() {
			for(let index = 0; ; index++) {
				let element = document.getElementById('entry-' + index);
				if(element == null) {
					break;
				}

				if(index == GameTable.selectedSlot) {
					element.classList.add('info');
				} else {
					element.classList.remove('info');
				}
			}
		}
};

var AJAX = {
		addServerBot: function(type, slot) {
			var request = new XMLHttpRequest();
			request.open("POST", "../requestBot", true);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send("slot=" + slot + "&name=" + type);
		},
		
		updateGameTable: function() {
			var request = new XMLHttpRequest();
			
			request.onreadystatechange = function() {
				if(this.readyState == 4 && this.status == 200) {
					GameTable.table.innerHTML = this.responseText;
					
					if(GameTable.selectedSlot != null) {
						GameTable.highlightSlot();
					}
				}
			};
			
			request.open("GET", "../static/fragments/gameTable.jsp", true);
			request.send();
		},
		
		//Requests an connectionID value from the server using AJAX
		requestID : function(type) {
			var request = new XMLHttpRequest();
			request.onreadystatechange = AJAX.IDAJAXComplete;
			request.open("POST", "../requestID", true);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send("type=" + type);
		},

		requestSpecificID : function(slot) {
			var request = new XMLHttpRequest();
			request.onreadystatechange = IDRequest.IDAJAXComplete;
			request.open("POST", "../requestID", true);
			request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			request.send("slot=" + slot);
		},

		IDAJAXComplete : function() {
			if(this.readyState == 4 && this.status == 200) {
				alert("Your ID is " + this.responseText);
			}
		}
};