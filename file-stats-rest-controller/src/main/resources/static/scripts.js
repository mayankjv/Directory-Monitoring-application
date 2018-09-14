//Global variable that stores the path that the user traverses so as to facilitate backward navigation
var backPath = [ "" ];
var currFolderName = "";
var pieChartConfig;
var wordCloudConfig;
var fileInModal = "";

/**
 * This function requests the search results from the server based on the query
 * entered by the user.
 * 
 * @returns
 */
function getSearchResults() {

	if (document.getElementById("name").value != ""
			&& document.getElementById("name_filter").value == "filter") {
		var alert = document.getElementById("alert-failure-search");
		alert.style.display = "block";
		alert.innerHTML = "Select a filter for name !";
		setTimeout(function() {
			alert.style.display = "none";
		}, 1000);
	}

	else if (document.getElementById("type").value != ""
			&& document.getElementById("type_filter").value == "filter") {
		var alert = document.getElementById("alert-failure-search");
		alert.style.display = "block";
		alert.innerHTML = "Select a filter for type !";
		setTimeout(function() {
			alert.style.display = "none";
		}, 1000);
		flag = 1;
	} else if (document.getElementById("size").value != ""
			&& document.getElementById("size_filter").value == "filter") {
		var alert = document.getElementById("alert-failure-search");
		alert.style.display = "block";
		alert.innerHTML = "Select a filter for size !";
		setTimeout(function() {
			alert.style.display = "none";
		}, 1000);
		flag = 1;
	} else {
		backPath.push(currFolderName);
		var show = new XMLHttpRequest();
		show.onreadystatechange = searchResponse;
		var name = document.getElementById("name").value;
		var name_filter = $('#name_filter').parents(".dropdown").find('.btn')
				.val();
		var type = document.getElementById("type").value;
		var type_filter = $('#type_filter').parents(".dropdown").find('.btn')
				.val();
		var size = document.getElementById("size").value;
		var size_filter = $('#size_filter').parents(".dropdown").find('.btn')
				.val();
		var keyword = $('#keyword').val();
		show.open("GET", "/search?name=" + name + "&name_filter=" + name_filter
				+ "&type=" + type + "&type_filter=" + type_filter + "&size="
				+ size + "&size_filter=" + size_filter + "&keyword=" + keyword
				+ "&folder=" + currFolderName, true);
		show.send();
	}
}

/**
 * This function shows the pie chart a modal.
 * 
 * @returns
 */
function showPieChart() {

	$('#word-cloud-div').hide();
	$('#pie-chart-div').show();
	document.getElementById("word-cloud-btn").disabled = false;
	document.getElementById("pie-chart-btn").disabled = true;

	zingchart.render({
		id : 'pie-chart-div',
		data : pieChartConfig,
		height : 400,
		width : "100%"
	});
	zingchart.shape_click = function(p) {
		if (p.shapeid == 'animation') {
			zingchart.exec('myChart', 'reload');
		}
	}
}

/**
 * This function provides the functionality of addition of stop words to the
 * user
 * 
 * @returns
 */
function addStopword() {
	var word = document.getElementById("stopword").value;
	var show = new XMLHttpRequest();
	show.onreadystatechange = respStopwords;
	show.open("GET", "/addStopword?word=" + word, true);
	show.send();

}

/**
 * this function shows the word cloud in a modal when requested by the user.
 * 
 * @returns
 */
function showWordCloud() {

	$('#word-cloud-div').show();
	$('#pie-chart-div').hide();
	document.getElementById("word-cloud-btn").disabled = true;
	document.getElementById("pie-chart-btn").disabled = false;
	zingchart.render({
		id : 'word-cloud-div',
		data : wordCloudConfig,
		height : 400,
		width : '100%'
	});

}

/**
 * This function gets the response of the search results from the user and
 * renders the result in the grid.
 * 
 * @returns
 */
function searchResponse() {
	if (this.readyState == 4 && this.status == 200) {
		if (jQuery.isEmptyObject(this.responseText)) {
			document.getElementById("table-body").innerHTML = "";
			document.getElementById("table-head").innerHTML = "";
			var alert = document.getElementById("alert-failure-out");
			alert.style.display = "block";
			alert.innerHTML = "No files to show !";
			setTimeout(function() {
				alert.style.display = "none";
			}, 1500);

		} else {
			var json = this.responseText;
			var data_ = JSON.parse(json);
			document.getElementById("table-body").innerHTML = "";
			document.getElementById("table-head").innerHTML = "";
			document.getElementById("table-head").innerHTML = document
					.getElementById("table-head").innerHTML
					+ "<tr><th>"
					+ "<a class=\"header-anchor\" href=\"#\" onclick='sortTableByName()'>File Name<//a>"
					+ "</th><th>View</th><th>"
					+ "<a class=\"header-anchor\" href=\"#\" onclick='sortTableByType()'>Type<//a>"
					+ "</th><th>"
					+ "<a class=\"header-anchor\" href=\"#\" onclick='sortTableBySize()'>Size(Bytes)<//a>"
					+ "</th><th>"
					+ "<a class=\"header-anchor\" href=\"#\" onclick='sortTableByWords()'>Word Count<//a>"
					+ "</th><th>"
					+ "<a class=\"header-anchor\" href=\"#\" onclick='sortTableByLines()'>Line Count<//a>"
					+ "</th><th>Tokens</th></tr>";
			for ( var ij in data_) {
				if (data_[ij]._type == "Folder") {
					document.getElementById("table-body").innerHTML = document
							.getElementById("table-body").innerHTML
							+ "<tr><td><a class=\"row-anchor\" href=\"#\" onclick='showFilesInThisSubFolder(\""
							+ data_[ij]._file_name
							+ "\")'>"
							+ data_[ij]._file_name
							+ "</a></td><td><a class=\"row-anchor\" href=\"#\" data-toggle=\"modal\" data-target=\"#modal-folder-summary\"  data-backdrop=\"static\" data-keyboard=\"false\" onclick='showFolderSummary(\""
							+ data_[ij]._file.replace(/\\/g, "\\\\")
							+ "\")'>view</a></td><td>"
							+ data_[ij]._type
							+ "</td><td>-</td><td>-</td><td>-</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm\" disabled>Show Tokens</button> </td></tr>";
				} else if (data_[ij]._type == "binary"
						|| data_[ij]._type == "log") {
					document.getElementById("table-body").innerHTML = document
							.getElementById("table-body").innerHTML
							+ "<tr><td><a class=\"row-anchor\" href=\" /downloadFile?filePath="
							+ encodeURI(data_[ij]._file)
							+ "\">"
							+ data_[ij]._file_name
							+ "</a></td><td><a class=\"row-anchor\" href=\"#\">view</a></td><td>"
							+ data_[ij]._type
							+ "</td><td>"
							+ data_[ij]._size
							+ "</td><td>-</td><td>-</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm  \" data-toggle=\"modal\" data-target=\"#tokenModal\" onclick='getTokensForThisFile(\""
							+ data_[ij]._file_name
							+ "\")' disabled>Show Tokens</button> </td></tr>";

				} else {
					if (data_[ij]._words == 0) {
						document.getElementById("table-body").innerHTML = document
								.getElementById("table-body").innerHTML
								+ "<tr><td><a class=\"row-anchor\" href=\" /downloadFile?filePath="
								+ encodeURI(data_[ij]._file)
								+ "\">"
								+ data_[ij]._file_name
								+ "</a></td><td><a class=\"row-anchor\" href=\"#\"  onclick='openFileInViewer(\""
								+ data_[ij]._file_name
								+ "\")'>view</a></td><td>"
								+ data_[ij]._type
								+ "</td><td>"
								+ data_[ij]._size
								+ "</td><td>"
								+ data_[ij]._words
								+ "</td><td>"
								+ data_[ij]._lines
								+ "</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm  \" data-toggle=\"modal\" data-target=\"#tokenModal\"        data-backdrop=\"static\" data-keyboard=\"false\"               onclick='getTokensForThisFile(\""
								+ data_[ij]._file_name
								+ "\")' dissabled>Show Tokens</button> </td></tr>";

					} else {
						document.getElementById("table-body").innerHTML = document
								.getElementById("table-body").innerHTML
								+ "<tr><td><a class=\"row-anchor\" href=\" /downloadFile?filePath="
								+ encodeURI(data_[ij]._file)
								+ "\">"
								+ data_[ij]._file_name
								+ "</a></td><td><a class=\"row-anchor\" href=\"#\"  onclick='openFileInViewer(\""
								+ data_[ij]._file_name
								+ "\")'>view</a></td><td>"
								+ data_[ij]._type
								+ "</td><td>"
								+ data_[ij]._size
								+ "</td><td>"
								+ data_[ij]._words
								+ "</td><td>"
								+ data_[ij]._lines
								+ "</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm  \" data-toggle=\"modal\" data-target=\"#tokenModal\"        data-backdrop=\"static\" data-keyboard=\"false\"               onclick='getTokensForThisFile(\""
								+ data_[ij]._file_name
								+ "\")' >Show Tokens</button> </td></tr>";
					}
				}

			}
			$("#dismiss").click();
		}
	}

}

/**
 * this function requests the content of a given file from the server.
 * 
 * @param name
 * @returns
 */
function openFileInViewer(name) {
	$('#open-viewer').click();
	var showFile = new XMLHttpRequest();
	showFile.onreadystatechange = fileResp;
	showFile.open("GET", "/openFileInViewer?fileName=" + name, true);
	showFile.send();
}

/**
 * This function receives the response from the server and renders the content
 * of the file with a given name in a modal.
 * 
 * @returns
 */
function fileResp() {
	var response = this.responseText;
	if (this.readyState == 4 && this.status == 200) {
		var json = this.responseText;
		json = JSON.parse(json);
		document.getElementById("file-view").innerHTML = "";
		for (i in json)
			document.getElementById("file-view").innerHTML = document
					.getElementById("file-view").innerHTML
					+ json[i];
	}
}

/**
 * This function requests the folder summary from the server
 * 
 * @param path
 * @returns
 */
function showFolderSummary(path) {
	path = path.replace(/\\/g, "\\\\");
	var showFolderSum = new XMLHttpRequest();
	showFolderSum.onreadystatechange = showFolderResponse;
	showFolderSum.open("GET", "/folderSummary?path=" + encodeURI(path), true);
	showFolderSum.send();

}

/**
 * This function receives the folder summary response from the server and
 * renders it in a Modal.
 * 
 * @returns
 */
function showFolderResponse() {
	var res = this.responseText;
	var json = JSON.parse(res);
	document.getElementById("folder-summary-modal-table").innerHTML = "";
	document.getElementById("folder-summary-modal-table").innerHTML = document
			.getElementById("folder-summary-modal-table").innerHTML
			+ "<tr><td>Folder Size</td><td>"
			+ json.totalsize
			+ " bytes</td></tr>";
	document.getElementById("folder-summary-modal-table").innerHTML = document
			.getElementById("folder-summary-modal-table").innerHTML
			+ "<tr><td>Number of Files</td><td>"
			+ json.totalfiles
			+ "</td></tr>";
	document.getElementById("folder-summary-modal-table").innerHTML = document
			.getElementById("folder-summary-modal-table").innerHTML
			+ "<tr><td>Smallest File Size</td><td>"
			+ json.smallest
			+ " bytes</td></tr>";
	document.getElementById("folder-summary-modal-table").innerHTML = document
			.getElementById("folder-summary-modal-table").innerHTML
			+ "<tr><td>Largest File Size</td><td>"
			+ json.largest
			+ " bytes</td></tr>";
	document.getElementById("folder-summary-modal-table").innerHTML = document
			.getElementById("folder-summary-modal-table").innerHTML
			+ "<tr><td>Last Modified</td><td>"
			+ json.lastmodified
			+ "</td></tr>";
}

/**
 * This function requests stopwords from the server
 * 
 * @returns
 */
function showStopwords() {
	var show = new XMLHttpRequest();
	show.onreadystatechange = respStopwords;
	show.open("GET", "/getStopwords", true);
	show.send();
}

/**
 * This function receives the stopwords from the server and renders them in a
 * modal as a list
 * 
 * @returns
 */
function respStopwords() {
	var json = JSON.parse(this.responseText);
	document.getElementById("stopwords-list").innerHTML = "";
	for (i in json) {
		document.getElementById("stopwords-list").innerHTML = document
				.getElementById("stopwords-list").innerHTML
				+ "<li class=\"list-group-item list-group-item-action\">"
				+ json[i] + "</li>";
	}
}

/**
 * This function sends a request to th server to add a path to the primary data
 * structure.
 * 
 * @returns
 */
function addPath() {
	var show = new XMLHttpRequest();
	show.onreadystatechange = resp;
	var path_ = document.getElementById("input_path").value;
	if (path_ == "") {
		var alert = document.getElementById("alert-failure");
		alert.style.display = "block";
		alert.innerHTML = "Path can not be empty!";
		setTimeout(function() {
			alert.style.display = "none";
		}, 1000);
		return;
	}
	var res_ = encodeURI(path_);
	show.open("GET", "/addPath?path=" + res_, true);
	show.send();
}

/**
 * This funciton receives the response as the list of files that are present
 * inside the path added .
 * 
 * @returns
 */
function resp() {
	if (this.readyState == 4 && this.status == 200) {
		if (jQuery.isEmptyObject(this.responseText)) {
			var alert = document.getElementById("alert-failure");
			alert.style.display = "block";
			alert.innerHTML = "please enter a valid path !";
			setTimeout(function() {
				alert.style.display = "none";
			}, 1000);

		} else {
			var json = this.responseText;
			json = JSON.parse(json);
			if (json[0] == "100") {
				var alert = document.getElementById("alert-failure");
				alert.style.display = "block";
				alert.innerHTML = "Path already present!";
				setTimeout(function() {
					alert.style.display = "none";
				}, 1500);
			} else {
				$("#add-path-modal-close").click();
				document.getElementById("wait-alert").innerHTML = "Calculating total files, please wait.";
				document.getElementById("table-body").style.display = 'none';
				document.getElementById("table-head").style.display = 'none';
				document.getElementById("back-btn").style.display = 'none';
				document.getElementById("table-body").innerHTML = "";
				document.getElementById("table-head").innerHTML = "";
				showAllPaths();
			}
		}
	}
}

/**
 * Requests the list of all the paths added so far.
 * 
 * @returns
 */
function showAllPaths() {

	document.getElementById("table-body").style.display = 'none';
	document.getElementById("table-head").style.display = 'none';
	document.getElementById("back-btn").style.display = 'none';
	var folderRequest = new XMLHttpRequest();
	folderRequest.onreadystatechange = folderResponse;
	folderRequest.open("GET", "/allFolders", true);
	folderRequest.send();
}

/**
 * Receives the list of all the paths added so far and renders them as a list
 * 
 * @returns
 */
function folderResponse() {
	if (this.readyState == 4 && this.status == 200) {
		var json = this.responseText;
		json = JSON.parse(json);
		if (json[0] == "404") {
			var alert = document.getElementById("alert-failure-out");
			alert.style.display = "block";
			alert.innerHTML = "No Folders to show, try adding path first !";
			setTimeout(function() {
				alert.style.display = "none";
			}, 1500);

		} else {
			document.getElementById("folders").innerHTML = "<li class=\"list-group-item list-group-item-info folders-header\">Folders</li>";
			for (i in json) {
				document.getElementById("folders").innerHTML = document
						.getElementById("folders").innerHTML
						+ "<a href=\"#\"  class=\"list-group-item list-group-item-action\"  onclick='showFilesInThisFolder(\""
						+ json[i] + "\")'>" + json[i] + "</a>";
			}
			if (json.length == 1) {
				document.getElementById("search-dropdown").disabled = false;
				showFilesInThisFolder(json);
			} else {
				document.getElementById("search-dropdown").disabled = true;
			}
		}
	}
}

/**
 * Requests data for the folder that was visited right before the current folder
 * and renders it in the Table.
 * 
 * @returns
 */
function back() {
	if (backPath.length <= 2) {
		var alert = document.getElementById("alert-failure-out");
		alert.style.display = "block";
		alert.innerHTML = "Cannot go back further, try selecting a folder!";
		setTimeout(function() {
			alert.style.display = "none";
		}, 1000);

	} else {
		backPath.pop();
		showFilesInThisFolder(backPath[backPath.length - 1]);
	}
}

/**
 * Checks if the name value is empty and enables/disables filter drop down
 * accordingly
 * 
 * @param value
 * @returns
 */
function isEmptyName(value) {
	if (value != "") {
		document.getElementById("name_filter").disabled = false;
	} else {
		document.getElementById("name_filter").value = 'filter';
		document.getElementById("name_filter").disabled = true;
	}
}

/**
 * Checks if the type value is empty and enables/disables filter drop down
 * accordingly
 * 
 * @param value
 * @returns
 */
function isEmptyType(value) {
	if (value != "") {
		document.getElementById("type_filter").disabled = false;
	} else {
		document.getElementById("type_filter").value = 'filter';
		document.getElementById("type_filter").disabled = true;
	}
}

/**
 * Checks if the size value is empty and enables/disables filter drop down
 * accordingly
 * 
 * @param value
 * @returns
 */
function isEmptySize(value) {
	if (value != "") {
		document.getElementById("size_filter").disabled = false;
	} else {
		document.getElementById("size_filter").value = 'filter';
		document.getElementById("size_filter").disabled = true;
	}
}

/**
 * requests files in a given folder from the server
 * 
 * @param data
 * @returns
 */
function showFilesInThisFolder(data) {

	document.getElementById("search-dropdown").disabled = false;
	document.getElementById("table-body").style.display = 'block';
	document.getElementById("table-head").style.display = 'block';
	if (backPath.indexOf(data) == -1)
		backPath.push(data);
	emptyTable();
	currFolderName = data;
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = xresp;
	xhttp.open("GET", "/filesInThisFolder?folder_name=" + data, true);
	xhttp.send();

}

/**
 * gets the response of the files in a given folder and renders the results in
 * the Table
 * 
 * @returns
 */
function xresp() {
	if (this.readyState == 4 && this.status == 200) {
		if ((this.responseText.length == 2)) {
			document.getElementById("table-body").innerHTML = "";
			document.getElementById("table-head").innerHTML = "";
			var alert = document.getElementById("alert-failure-out");
			alert.style.display = "block";
			alert.innerHTML = "No files to show !";
			setTimeout(function() {
				alert.style.display = "none";
			}, 1500);

		} else {
			var json = this.responseText;
			var data_ = JSON.parse(json);

			document.getElementById("back-btn").style.display = 'block';
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
				if (data_[ij]._type == "Folder") {
					document.getElementById("table-body").innerHTML = document
							.getElementById("table-body").innerHTML
							+ "<tr><td><a class=\"row-anchor\" href=\"#\" onclick='showFilesInThisSubFolder(\""
							+ data_[ij]._file_name
							+ "\")'>"
							+ data_[ij]._file_name
							+ "</a></td><td><a class=\"row-anchor\" href=\"#\" data-toggle=\"modal\" data-target=\"#modal-folder-summary\"   onclick='showFolderSummary(\""
							+ data_[ij]._file.replace(/\\/g, "\\\\")
							+ "\")'>view</a></td><td>"
							+ data_[ij]._type
							+ "</td><td>-</td><td>-</td><td>-</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm\" disabled>Show Tokens</button> </td></tr>";
				} else if (data_[ij]._type == "binary"
						|| data_[ij]._type == "log") {
					document.getElementById("table-body").innerHTML = document
							.getElementById("table-body").innerHTML
							+ "<tr><td><a class=\"row-anchor\" href=\" /downloadFile?filePath="
							+ encodeURI(data_[ij]._file)
							+ "\">"
							+ data_[ij]._file_name
							+ "</a></td><td><a class=\"row-anchor\" href=\"#\">view</a></td><td>"
							+ data_[ij]._type
							+ "</td><td>"
							+ data_[ij]._size
							+ "</td><td>-</td><td>-</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm  \" data-toggle=\"modal\" data-target=\"#tokenModal\" onclick='getTokensForThisFile(\""
							+ data_[ij]._file_name
							+ "\")' disabled>Show Tokens</button> </td></tr>";

				} else {
					if (data_[ij]._words == 0) {
						document.getElementById("table-body").innerHTML = document
								.getElementById("table-body").innerHTML
								+ "<tr><td><a class=\"row-anchor\" href=\" /downloadFile?filePath="
								+ encodeURI(data_[ij]._file)
								+ "\">"
								+ data_[ij]._file_name
								+ "</a></td><td><a class=\"row-anchor\" href=\"#\"  onclick='openFileInViewer(\""
								+ data_[ij]._file_name
								+ "\")'>view</a></td><td>"
								+ data_[ij]._type
								+ "</td><td>"
								+ data_[ij]._size
								+ "</td><td>"
								+ data_[ij]._words
								+ "</td><td>"
								+ data_[ij]._lines
								+ "</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm  \" data-toggle=\"modal\" data-target=\"#tokenModal\"        data-backdrop=\"static\" data-keyboard=\"false\"               onclick='getTokensForThisFile(\""
								+ data_[ij]._file_name
								+ "\")' disabled>Show Tokens</button> </td></tr>";

					} else {
						document.getElementById("table-body").innerHTML = document
								.getElementById("table-body").innerHTML
								+ "<tr><td><a class=\"row-anchor\" href=\" /downloadFile?filePath="
								+ encodeURI(data_[ij]._file)
								+ "\">"
								+ data_[ij]._file_name
								+ "</a></td><td><a class=\"row-anchor\" href=\"#\"  onclick='openFileInViewer(\""
								+ data_[ij]._file_name
								+ "\")'>view</a></td><td>"
								+ data_[ij]._type
								+ "</td><td>"
								+ data_[ij]._size
								+ "</td><td>"
								+ data_[ij]._words
								+ "</td><td>"
								+ data_[ij]._lines
								+ "</td><td> <button type=\"button\" class=\"btn btn-outline-info btn-sm  \" data-toggle=\"modal\" data-target=\"#tokenModal\"        data-backdrop=\"static\" data-keyboard=\"false\"               onclick='getTokensForThisFile(\""
								+ data_[ij]._file_name
								+ "\")' >Show Tokens</button> </td></tr>";
					}
				}
			}
		}
	}

}

/**
 * This function empties the content of the table
 * 
 * @returns
 */
function emptyTable() {
	document.getElementById("table-body").innerHTML = "";
	document.getElementById("table-head").innerHTML = "";
}

/**
 * requests files when navigating inside an inner folder
 * 
 * @param data
 * @returns
 */
function showFilesInThisSubFolder(data) {

	document.getElementById("search-dropdown").disabled = false;
	if (backPath.indexOf(data) == -1)
		backPath.push(data);
	emptyTable();
	currFolderName = data;
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = xresp;
	xhttp.open("GET", "/filesInThisFolder?folder_name=" + data, true);
	xhttp.send();
}

/**
 * requests tokens for a given file
 * 
 * @param fileName
 * @returns
 */
function getTokensForThisFile(fileName) {
	document.getElementById("modal-table").innerHTML = "";
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = tokenResponse;
	xhttp.open("GET", "/tokensForAFile?fileName=" + encodeURI(fileName), true);
	xhttp.send();
}

/**
 * receives tokens as response and renders the results as table, pie-charts and
 * word-cloud
 * 
 * @returns
 */
function tokenResponse() {
	if (this.readyState == 4 && this.status == 200) {
		var json = this.responseText;
		var data_ = JSON.parse(json);
		data_ = JSON.parse(data_);
		var jsonSeries = [];
		var jsonSerieswc = [];
		document.getElementById("modal-table").innerHTML = "<thead class=\"thead-dark\"><tr><th>Token</th><th>Count</th><th>Frequency %</th></tr></thead>";
		var data = data_.tokens;
		for (i in data) {
			var js1 = "{\"text\": \"" + data[i].token + "\" , \"count\": \""
					+ data[i].count + "\" }";
			var js = "{\"values\":[" + data[i].count + "], \"text\": \""
					+ data[i].token + "\", \"legend-text\": \"" + data[i].token
					+ "\" }";
			js = JSON.parse(js);
			js1 = JSON.parse(js1);
			jsonSerieswc.push(js1);
			jsonSeries.push(js);
			var freq = (data[i].count * 100) / data_.total;
			document.getElementById("modal-table").innerHTML = document
					.getElementById("modal-table").innerHTML
					+ "<tr><td>"
					+ data[i].token
					+ "</td><td>"
					+ data[i].count
					+ "</td><td>" + freq + "</td></tr>";
		}

		wordCloudConfig = {
			type : 'wordcloud',
			"options" : {
				"style" : {
					"tooltip" : {
						visible : true,
						text : '%text: %hits'
					}
				},
				"words" : jsonSerieswc
			}
		};

		pieChartConfig = {
			"type" : "pie",
			"legend" : {
				"max-items" : 5,
				"overflow" : "scroll",
				"scroll" : {
					"bar" : {
						"background-color" : "#FFFFFF",
					},
					"handle" : {
						"background-color" : "#cccccc",
						"border-radius" : "15px"
					}
				},
				"x" : "75%",
				"y" : "25%"
			},
			"plotarea" : {
				"margin-right" : "30%",
				"margin-top" : "15%"
			},
			"plot" : {
				"value-box" : {
					"font-size" : 0,
					"font-weight" : "normal",
					"offset-r" : "50%"
				}
			},
			"series" : jsonSeries
		};
		showPieChart();
	}
}

/**
 * function to sort the table by name
 * 
 * @returns
 */
function sortTableByName() {

	var table, rows, switching, i, x, y, shouldSwitch;
	table = document.getElementById("table-body");
	switching = true;
	while (switching) {
		switching = false;
		rows = table.rows;
		for (i = 1; i < (rows.length - 1); i++) {
			shouldSwitch = false;
			x = rows[i].getElementsByTagName("TD")[0];
			y = rows[i + 1].getElementsByTagName("TD")[0];
			if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
				shouldSwitch = true;
				break;
			}
		}
		if (shouldSwitch) {
			rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
			switching = true;
		}
	}
}

/**
 * function to sort the table by type
 * 
 * @returns
 */
function sortTableByType() {

	var table, rows, switching, i, x, y, shouldSwitch;
	table = document.getElementById("table-body");
	switching = true;
	while (switching) {
		switching = false;
		rows = table.rows;
		for (i = 1; i < (rows.length - 1); i++) {
			shouldSwitch = false;
			x = rows[i].getElementsByTagName("TD")[2];
			y = rows[i + 1].getElementsByTagName("TD")[2];
			if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
				shouldSwitch = true;
				break;
			}
		}
		if (shouldSwitch) {
			rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
			switching = true;
		}
	}
}

/**
 * function to sort the table by size
 * 
 * @returns
 */
function sortTableBySize() {

	var table, rows, switching, i, x, y, shouldSwitch;
	table = document.getElementById("table-body");
	switching = true;
	while (switching) {
		switching = false;
		rows = table.rows;
		for (i = 1; i < (rows.length - 1); i++) {
			shouldSwitch = false;
			x = rows[i].getElementsByTagName("TD")[3];
			y = rows[i + 1].getElementsByTagName("TD")[3];
			var x1;
			var y1;
			if (x.innerHTML == '-') {
				x1 = 0;
			} else
				x1 = Number(x.innerHTML);
			if (y.innerHTML == '-') {
				y1 = 0;
			} else
				y1 = Number(y.innerHTML);

			if (x1 > y1) {
				shouldSwitch = true;
				break;
			}

		}
		if (shouldSwitch) {
			rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
			switching = true;
		}
	}
}

/**
 * function to sort the table by number of words
 * 
 * @returns
 */
function sortTableByWords() {

	var table, rows, switching, i, x, y, shouldSwitch;
	table = document.getElementById("table-body");
	switching = true;
	while (switching) {
		switching = false;
		rows = table.rows;
		for (i = 1; i < (rows.length - 1); i++) {
			shouldSwitch = false;
			x = rows[i].getElementsByTagName("TD")[4];
			y = rows[i + 1].getElementsByTagName("TD")[4];

			var x1;
			var y1;
			if (x.innerHTML == '-') {
				x1 = 0;
			} else {
				x1 = Number(x.innerHTML);
			}
			if (y.innerHTML == '-') {
				y1 = 0;
			} else {
				y1 = Number(y.innerHTML);
			}

			if (x1 > y1) {
				shouldSwitch = true;
				break;
			}
		}
		if (shouldSwitch) {
			rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
			switching = true;
		}
	}
}

/**
 * function to sort the table by number of lines
 * 
 * @returns
 */
function sortTableByLines() {

	var table, rows, switching, i, x, y, shouldSwitch;
	table = document.getElementById("table-body");
	switching = true;
	while (switching) {
		switching = false;
		rows = table.rows;
		for (i = 1; i < (rows.length - 1); i++) {
			shouldSwitch = false;
			x = rows[i].getElementsByTagName("TD")[5];
			y = rows[i + 1].getElementsByTagName("TD")[5];
			var x1;
			var y1;
			if (x.innerHTML == '-') {
				x1 = 0;
			} else {
				x1 = Number(x.innerHTML);
			}
			if (y.innerHTML == '-') {
				y1 = 0;
			} else {
				y1 = Number(y.innerHTML);
			}

			if (x1 > y1) {
				shouldSwitch = true;
				break;
			}
		}
		if (shouldSwitch) {
			rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
			switching = true;
		}
	}
}

/**
 * function that disables select option form input is empty
 * 
 * @returns
 */
$(".dropdown-menu li a").click(
		function() {
			$(this).parents(".dropdown").find('.btn').html(
					$(this).text() + ' <span class="caret"></span>');
			$(this).parents(".dropdown").find('.btn')
					.val($(this).data('value'));

		});
