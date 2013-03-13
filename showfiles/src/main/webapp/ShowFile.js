/**
 * ShowFile.js
 */

var leftDelim = " {@ "
var rightDelim = " @} "


var handleSuccess = function(o) {

	function parseHeaders() {
		var allHeaders = headerStr.split("\n");
		var headers;
		for(var i=0; i < headers.length; i++){
			var delimitPos = header[i].indexOf(':');
			if(delimitPos != -1){
				headers[i] = "<p>" +
				headers[i].substring(0,delimitPos) + ":"+
				headers[i].substring(delimitPos+1) + "</p>";
			}
		}
		return headers;
	}
	window.status = "Markup Written";
	/*
	if(o.responseText !== undefined) {
		var messageDiv=document.getElementById("messageDiv");
		messageDiv.innerHTML = "Transaction id: " + o.tId;
		messageDiv.innerHTML += "HTTP status: " + o.status;
		messageDiv.innerHTML += "Status code message: " + o.statusText;
		messageDiv.innerHTML += "HTTP headers: " + parseHeaders();
		messageDiv.innerHTML += "Server response: " + o.responseText;
		messageDiv.innerHTML += "Argument object: property foo = " + o.argument.foo +
						 "and property bar = " + o.argument.bar;
	}
	*/
}

var handleFailure = function(o) {

	if(o.responseText !== undefined) {
		var messageDiv=document.getElementById("messageDiv");
		messageDiv.innerHTML = "Failed to write:<br/>";
		messageDiv.innerHTML += "<li>Transaction id: " + o.tId + "</li>";
		messageDiv.innerHTML += "<li>HTTP status: " + o.status + "</li>";
		messageDiv.innerHTML += "<li>Status code message: " + o.statusText + "</li>";
	}
}

var callback = {
  success:handleSuccess,
  failure: handleFailure,
  argument: null
};

function getHTMLText(document) {
	var text = '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" '+
			'"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">\n';
	text += '<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-us" lang="en-us">\n'
	text += document.childNodes[1].innerHTML;
	text += '\n</html>'
	return text;
}

function getPlainText(node) {
	var text = "";
	var children = node.childNodes;
	var len = children.length;
	
	for (var i = 0; i < len; i++) {
		var child = children[i];
		if (child.nodeType == child.TEXT_NODE) {
			text += child.textContent;
		} else if (child.nodeType == child.ELEMENT_NODE) {
			var tagName = child.tagName.toUpperCase();
			if (tagName == "A") {
				var firstChild = child.firstChild;
				if (firstChild != null &&
				    firstChild.nodeType == firstChild.ATTRIBUTE_NODE &&
					firstChild.attributeName == "href") {
					break;
				}
			}
			if ((tagName != "HEAD") &&
				(tagName != "H1") &&
			    (tagName != "SPAN")) {
				text += getPlainText(child);
			}
		}
	}
	return text;
}

function getSel() {
	if (window.getSelection) {
		var sel;
		var range;
		try {
			sel = window.getSelection();
			range = sel.getRangeAt(0);
		} catch (e) {
			return;
		}
		if (!range.startContainer.isSameNode(range.endContainer)) {
			return;
		}
		
		var node = range.startContainer;
		var text = node.textContent;
		var len = text.length;
		
		var start = range.startOffset;
		var end = range.endOffset;
		
		var ldLen = leftDelim.length;
		var rdLen = rightDelim.length;
		
		wideTest = text.substring(Math.max(0,start-(ldLen-1)),
								  Math.min(len,end+(rdLen-1)));
		if ((wideTest.indexOf(leftDelim) > -1) ||
			(wideTest.indexOf(rightDelim) > -1)) {
			return;
		}
		/*
		for (i = start; i < end; i++) {
			if ((text[i] == '{') ||
				(text[i] == '}')) {
				return;
			}
		}
		*/
		
		var existingLeftDelim = -1;
		var existingRightDelim = -1;
		var leftSubstring = text.substring(0,Math.min(len,start+(ldLen-1)));
		var rightSubstring = text.substring(Math.max(0,end-(rdLen-1)));
		
		existingLeftDelim = leftSubstring.lastIndexOf(leftDelim);
		if (leftSubstring.lastIndexOf(rightDelim) > existingLeftDelim) {
			existingLeftDelim = -1;
		}
		
		existingRightDelim = rightSubstring.indexOf(rightDelim);
		var checkInside = rightSubstring.indexOf(leftDelim);
		if (checkInside > -1 && checkInside < existingRightDelim) {
			existingRightDelim = -1;
		} else {
			existingRightDelim += Math.max(0,end-(rdLen-1));
		}
		
		if ((existingLeftDelim >= 0) &&
			(existingRightDelim >= 0) &&
			((existingRightDelim-existingLeftDelim) >= ldLen)) {
			
			var origText = text.substring(existingLeftDelim+ldLen, existingRightDelim);
			node.replaceData(existingLeftDelim,
				existingRightDelim-existingLeftDelim+rdLen, origText);
				
			sel.removeRange(range);
			return;
		}
		/*
		var existingLeftBrace = -1;
		var existingRightBrace = -1;
		for (i = start-1; i >= 0; i--) {
			if (text[i] == '}') {
				break;
			}
			if (text[i] == '{') {
				existingLeftBrace = i;
				break;
			}
		}
		
		for (i = end; i < len; i++) {
			if (text[i] == '{') {
				break;
			}
			if (text[i] == '}') {
				existingRightBrace = i+1;
				break;
			}
		}
		
		if ((existingLeftBrace >= 0) &&
			(existingRightBrace >= 0)) {
			
			var origText = text.substring(existingLeftBrace+1, existingRightBrace-1);
			node.replaceData(existingLeftBrace,
				existingRightBrace-existingLeftBrace, origText);
				
			sel.removeRange(range);
			return;
		}
		*/
		
		var endOffset = end;
		for (i = end; i < len; i++) {
			if ((text[i] >= 'a' && text[i] <= 'z') ||
			    (text[i] >= 'A' && text[i] <= 'Z') ||
			    (text[i] >= '0' && text[i] <= '9') ||
			    (text[i] == '\'')) {
			    endOffset++;
			} else {
				break;
			}
		}
		
		var startOffset = start;
		for (i = start-1; i >= 0; i--) {
			if ((text[i] >= 'a' && text[i] <= 'z') ||
			    (text[i] >= 'A' && text[i] <= 'Z') ||
			    (text[i] >= '0' && text[i] <= '9') ||
			    (text[i] == '\'')) {
			    startOffset--;
			} else {
				break;
			}
		}
		
		if (startOffset == endOffset) {
			return;
		}
		
		var selectedText = text.substring(startOffset, endOffset);
		node.replaceData(startOffset, endOffset-startOffset, leftDelim+selectedText+rightDelim);
		sel.removeRange(range);
	} else if (document.selection) {
		var range = document.selection.createRange();
		range.expand("word");
		var selectedText = range.text;
		range.text = leftDelim+selectedText+rightDelim;
	} else {
		throw "Browser doesn't support selection manipulation";
		//return;
	}
}

function writeMarkup() {
 	var loc = document.location.toString();
	var len = loc.length;
	var name = loc.substring(loc.indexOf("/ShowFile/")+10);
	name = name.substring(0,name.indexOf("."));
	
	try {
		var sUrl = loc.substring(0,loc.indexOf("/showfiles/"));
		sUrl += "/showfiles/PutFile?name="+name+".markup";
		var request = YAHOO.util.Connect.asyncRequest(
			"POST", sUrl, callback, getPlainText(document)); 
			
		var sUrl = loc.substring(0,loc.indexOf("/showfiles/"));
		sUrl += "/showfiles/PutFile?name="+name+".markup.html";
		var request = YAHOO.util.Connect.asyncRequest(
			"POST", sUrl, callback, getHTMLText(document)); 
	} catch (e) {
		var messageDiv=document.getElementById("messageDiv");
		messageDiv.innerHTML = e.toString();
	}
}

function load() {
	window.status = "started";
}

window.onload=load;
document.onmouseup=getSel;
