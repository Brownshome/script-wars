function addServerBot(className, slot) {
	var request = new XMLHttpRequest();
	request.open("POST", "../requestBot", true);
	request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	request.send("slot=" + slot + "&name=" + className);
}