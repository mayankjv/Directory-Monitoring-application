/**
 * Function that sends data via Web Socket
 * 
 * @returns
 */
function sendMessage() {

	stompClient.send("/socket/message", {}, JSON.stringify({
		'name' : $("#text-content").val()
	}));
}

// Global Variable which holds an object of StompClient
var stompClient = null;

// Connecting the socket at the start
$(document).ready(function() {
	connectSocket();
	$("#pie-chart-div-license-text").hide();
	$("#word-cloud-div-license-text").hide();

});

/**
 * This function connects the socket and enables data to be transfered via the
 * socket in both the directions
 * 
 * @returns
 */
function connectSocket() {
	// Creating a new SockJS object
	var socket = new SockJS('/filestatisticsui');

	// Instantiating stompClient object
	stompClient = Stomp.over(socket);

	// Connecting the socket
	stompClient.connect({}, function(frame) {
		console.log('Connected: ' + frame);

		// subscribing the url on which progress bar response will be received
		stompClient.subscribe('/socketresponse/status', function(status) {
			console.log(status.body);
			showStatus(JSON.parse(status.body));
		});

		// subscribing the url on which watcher response will be received
		stompClient.subscribe('/socketresponse/watcher', function(json) {
			console.log("response");
			reflectChanges(JSON.parse(json.body))
		});

	});
}

/**
 * This funciton will reflect the changes to the current page if the watcher
 * response is received.
 * 
 * @param data_
 *            is a JSON object that holds all the files that are to be rendered
 *            in the table.
 * @returns
 */
function reflectChanges(data_) {
	if (data_.directory_name == currFolderName) {
		data_ = data_.files;
		document.getElementById("back-btn").style.display = 'block';
		document.getElementById("table-head").innerHTML = "";
		document.getElementById("table-body").innerHTML = "";
		document.getElementById("table-head").innerHTML = document
				.getElementById("table-head").innerHTML
				+ "<tr><th>"
				+ "<a class=\"header-anchor\" href=\"#\" onclick='sortTableByName()'>File Name<//a>"
				+ "</th><th>View</th><th>"
				+ "<a class=\"header-anchor\" href=\"#\" onclick='sortTableByType()'>Type<//a>"
				+ "</th><th>"
				+ "<a class=\"header-anchor\" href=\"#\" onclick='sortTableBySize()'>Size (Bytes)<//a>"
				+ "</th><th>"
				+ "<a class=\"header-anchor\" href=\"#\" onclick='sortTableByWords()'>Word Count<//a>"
				+ "</th><th>"
				+ "<a class=\"header-anchor\" href=\"#\" onclick='sortTableByLines()'>Line Count<//a>"
				+ "</th><th>Tokens</th></tr>";
		for ( var ij in data_) {

			if (data_[ij].type == "Folder") {
				document.getElementById("table-body").innerHTML = document
						.getElementById("table-body").innerHTML
						+ "<tr><td><a class=\"row-anchor\" href=\"#\" onclick='showFilesInThisSubFolder(\""
						+ data_[ij].name
						+ "\")'>"
						+ data_[ij].name
						+ "</a></td><td><a class=\"row-anchor\" href=\"#\" data-toggle=\"modal\" data-target=\"#modal-folder-summary\"   onclick='showFolderSummary(\""
						+ data_[ij].file.path.replace(/\\/g, "\\\\")
						+ "\")'>view</a></td><td>"
						+ data_[ij].type
						+ "</td><td>-</td><td>-</td><td>-</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm\" disabled>Show Tokens</button> </td></tr>";
			} else if (data_[ij].type == "binary" || data_[ij].type == "log") {
				document.getElementById("table-body").innerHTML = document
						.getElementById("table-body").innerHTML
						+ "<tr><td><a class=\"row-anchor\" href=\" /downloadFile?filePath="
						+ encodeURI(data_[ij].file)
						+ "\">"
						+ data_[ij].name
						+ "</a></td><td><a class=\"row-anchor\" href=\"#\">view</a></td><td>"
						+ data_[ij].type
						+ "</td><td>"
						+ data_[ij].size
						+ "</td><td>-</td><td>-</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm  \" data-toggle=\"modal\" data-target=\"#tokenModal\" onclick='getTokensForThisFile(\""
						+ data_[ij].name
						+ "\")' disabled>Show Tokens</button> </td></tr>";

			} else {
				if (data_[ij].words == 0) {
					document.getElementById("table-body").innerHTML = document
							.getElementById("table-body").innerHTML
							+ "<tr><td><a class=\"row-anchor\" href=\" /downloadFile?filePath="
							+ encodeURI(data_[ij].file)
							+ "\">"
							+ data_[ij].name
							+ "</a></td><td><a class=\"row-anchor\" href=\"#\"  onclick='openFileInViewer(\""
							+ data_[ij].name
							+ "\")'>view</a></td><td>"
							+ data_[ij].type
							+ "</td><td>"
							+ data_[ij].size
							+ "</td><td>"
							+ data_[ij].words
							+ "</td><td>"
							+ data_[ij].lines
							+ "</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm  \" data-toggle=\"modal\" data-target=\"#tokenModal\"        data-backdrop=\"static\" data-keyboard=\"false\"               onclick='getTokensForThisFile(\""
							+ data_[ij].name
							+ "\")' disabled>Show Tokens</button> </td></tr>";

				} else {
					document.getElementById("table-body").innerHTML = document
							.getElementById("table-body").innerHTML
							+ "<tr><td><a class=\"row-anchor\" href=\" /downloadFile?filePath="
							+ encodeURI(data_[ij].file)
							+ "\">"
							+ data_[ij].name
							+ "</a></td><td><a class=\"row-anchor\" href=\"#\"  onclick='openFileInViewer(\""
							+ data_[ij].name
							+ "\")'>view</a></td><td>"
							+ data_[ij].type
							+ "</td><td>"
							+ data_[ij].size
							+ "</td><td>"
							+ data_[ij].words
							+ "</td><td>"
							+ data_[ij].lines
							+ "</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm  \" data-toggle=\"modal\" data-target=\"#tokenModal\"        data-backdrop=\"static\" data-keyboard=\"false\"               onclick='getTokensForThisFile(\""
							+ data_[ij].name
							+ "\")' >Show Tokens</button> </td></tr>";
				}
			}
		}
	}
}

/**
 * This function shows the status that is obtained via web socket and updates
 * the progress bar accordingly
 * 
 * @param data
 * @returns
 */
function showStatus(data) {

	$("#wait-alert").hide();
	data = JSON.parse(data);
	$("#progress-bar").width(data.name + "%").text(data.name + "%").show();
	if (data.name == "100") {
		setTimeout(function() {
			$("#progress-bar").hide();
			$(".btn-warning").click();
		}, 1000);
	}
}

$(function() {
	$("#selectable").selectable({
		stop : function() {
			$(".ui-selected", this).each(function() {
				folder = $(this).text()
			});
		}
	});
});