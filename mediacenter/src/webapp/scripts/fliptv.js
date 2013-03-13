
function FlipTV() {
	this.currentChannelIndex = 0;
	this.detailMode = false;
	this.digitBuffer = "";
	this.digitTimer = null;
	this.shift = false;
}

/**
 * Singleton instance
 */
FlipTV.Instance = new FlipTV();

/**
 * Gets the call sign for the current channel
 */
FlipTV.prototype.getCurrentCallSign = function() {
	return callSigns[this.currentChannelIndex];
}

/**
 * Gets the call sign for the given channel number.
 */
FlipTV.prototype.getCallSignFromChannel = function(channel) {
	for (var i=0; i < channels.length; i++) {
		if (channels[i] == channel) {
			return callSigns[i];
		}
	}
	return null;
}

/**
 * Changes the channel on the tv up one channel.
 */
FlipTV.prototype.channelUp = function() {
	this.currentChannelIndex++;
	if (this.currentChannelIndex >= channels.length) {
		this.currentChannelIndex=0;
	}
	this.resyncChannel();
}

/**
 * Changes the channel on the tv down one channel.
 */
FlipTV.prototype.channelDown = function() {
	this.currentChannelIndex--;
	if (this.currentChannelIndex < 0) {
		this.currentChannelIndex=channels.length-1;
	}
	this.resyncChannel();
}

/**
 * Changes the channel on the tv to the new value.
 */
FlipTV.prototype.setChannel = function(channel) {
	for (var i=0; i < channels.length; i++) {
		if (channels[i] == channel) {
			this.currentChannelIndex = i;
			this.resyncChannel();
			return true;
		}
	}
	return false;
}


/**
 * Synchronizes the channel to the channel that is specified by the currentChannelIndex value. 
 */
FlipTV.prototype.resyncChannel = function() {
	var MCE = window.external.MediaCenter();
	var arrayService = MCE.FindService(this.getCurrentCallSign(), "");
	MCE.PlayMedia(0,arrayService.item(0));
	
	var flashMovie = getFlashMovie();
	if (flashMovie.onChannelChanged) {
		flashMovie.onChannelChanged(channels[this.currentChannelIndex]);
	}
}

/**
 * Creates the custom viewport for the television display.
 */
FlipTV.prototype.CreateCustomViewport = function() {
	var MCE = window.external.MediaCenter();
	MCE.SharedViewPort.Visible = false;
	this.setFullMode();
	MCE.CustomViewPort.Visible = true;
}

/**
 * Changes the screen layout to detail mode where the television is only a minimal part of the display.
 */
FlipTV.prototype.setDetailMode = function() {
	var MCE = window.external.MediaCenter();
	var rect = window.external.MediaCenter.CustomViewPort.Rectangle;
	rect.Left = 0;
	rect.Top = 0;
	rect.Width = 400;
	rect.Height = 300;
	MCE.CustomViewPort.Rectangle = rect;
	this.detailMode = true;
}

/**
 * Changes the screen layout to full mode where the television is maximized on the display.
 */
FlipTV.prototype.setFullMode = function() {
	var MCE = window.external.MediaCenter();
	var rect = window.external.MediaCenter.CustomViewPort.Rectangle;
	rect.Left = 0;
	rect.Top = 0;
	rect.Width = 640;
	rect.Height = 480;
	MCE.CustomViewPort.Rectangle = rect;
	this.detailMode = false;
}


/**
 * Appends a digit to the digitBuffer.  If the digitBuffer fills up, then it will automatically
 * call applyDigits() to change the channel.  If the digitBuffer sitll isn't full, then it will
 * issue an onPendingChannelChange event to the flash movie
 */
FlipTV.prototype.appendDigit = function(digit) {
	//alert("append " + digit);
	this.digitBuffer += digit;
	this.digitLastPress = new Date();
	if (this.digitTimer) {
		clearTimeout(this.digitTimer);
		this.digitTimer = null;
	}
	if (this.digitBuffer.length == 3) {
		this.applyDigits();
	} else {
		//notify flash of the pending channel change
		var flashMovie = getFlashMovie();
		if (flashMovie.onPendingChannelChange) {
			flashMovie.onPendingChannelChange(this.digitBuffer);
		}
		var thisObj = this;
		this.digitTimer = setTimeout(
			function() {
				thisObj.applyDigits();
			}, 1500);
	}
}

/**
 * Applies the digits that are currently in the digitBuffer.  It will change the channel to the
 * value that is in the digitBuffer (if possible).  If the channel doesn't exist, it will issue
 * an onPendingChannelChangeCancel event to the flash movie.<b> 
 */
FlipTV.prototype.applyDigits = function() {
	//alert("applyDigits " + this.digitBuffer);
	if (this.digitBuffer.length > 0) {
		var channel = parseInt(this.digitBuffer);
		this.digitBuffer = "";
		if (!this.setChannel(channel)) {
			//pending change cancelled
			var flashMovie = getFlashMovie();
			if (flashMovie.onPendingChannelChangeCancel) {
				flashMovie.onPendingChannelChangeCancel(channels[this.currentChannelIndex]);
			}
		}
	}
}

/**
 * determine which remote control key the user selected 
 * and take appropriate action
 */
FlipTV.prototype.onRemoteEvent = function(keyChar) {
	var consumed = false;
	//notify flash of the remote event
	var flashMovie = getFlashMovie();
	if (flashMovie.onRemoteEvent) {
		consumed = flashMovie.onRemoteEvent(keyChar, this.shift);
	}
	
	//alert("key " + keyChar);
	var result = true;
    try {
        switch (keyChar) {
        case 37: //left
        case 38: //up
        case 39: //right
        case 40: //down
        alert(document.location);
        	send("fliptv.mcl", function() {});
            result = false;
            break;

        case 13: //enter
        	this.applyDigits();
            result = true;
            break;

        case 33: //channel up
        	this.channelUp();
            result = true;
            break;
            
        case 34: //channel down
        	this.channelDown();
            result = true;
            break;
            
        case 16: //shift key
        	this.shift = true;
            return true;


        case 48: //digit 0
        case 49: //digit 1
        case 50: //digit 2 
        case 51: //digit 3 
        case 52: //digit 4 
        case 53: //digit 5 
        case 54: //digit 6 
        case 55: //digit 7 
        case 56: //digit 8 
        case 57: //digit 9
        	if (!this.shift) {
	        	this.appendDigit(keyChar - 48);
	        }
            result = true;
            break;

        case -100: //more info
            result = true;
            break;
            
        default:
            //ignore all other clicks
            result = consumed;
            break;
        }
    } catch(ex) {
        //ignore error
    }
    this.shift = false;
    return result;
}

//--------------------------------

/**
 * Gets the flash movie
 */
function getFlashMovie() {
	return window.document.mceFlash;
}

/**
 * Initializes the flash movie with some of the MCE information.
 */
function initFlash() {
	var MCE = window.external.MediaCenter();
	var flashMovie = getFlashMovie();
	flashMovie.SetVariable("zipCode", MCE.PostalCode);
	flashMovie.SetVariable("minorVersion", MCE.MinorVersion);
	flashMovie.SetVariable("majorVersion", MCE.MajorVersion);
}

/**
 * Identify page as enabled for Media Center. This avoids a warning dialog to user.
 */
function IsMCEEnabled() {
	return true
}

/**
 * Scaling elements for page resize
 */
function onScaleEvent(vScale) {
    try {
        body.style.zoom=vScale;
    } catch(e) {
        // ignore error
    }
}

/**
 * Function for reloading page
 */
function reloadPage() {
    /* This function refreshes the page, and calls the onScaleEvent function
    to manage resizing of the elements on the page */
    window.location.reload()
    // determine width of page
    var newWidth = body.getBoundingClientRect().right
    // determine how much the page needs to be resized, by comparing page width to 1024
    var sizeAmount = (newWidth/1024)
    // call onScaleEvent function
    onScaleEvent(sizeAmount)
}

/**
 * determine which remote control key the user selected 
 * and take appropriate action
 */
function onRemoteEvent(keyChar) {
    return FlipTV.Instance.onRemoteEvent(keyChar);
}

function browseURL(url) {
	//alert(url);
	try {
		document.details.location.href = url;
	} catch (ex) {
		alert(ex.description);
	}
}

function toAbsoluteUrl(url) {
	if (url.indexOf("http") != 0) {
		//it's a relative url
		loc = document.location + "";		
		if (url.charAt(0) == '/') {
			prefix = loc.substring(0, loc.indexOf('/'));
		} else {
			prefix = loc.substring(0, loc.lastIndexOf('/')+1);
		}
		url = prefix + url;
	}
	return url;
}

function send(url, callback) {
	url = toAbsoluteUrl(url);
	var request = new ActiveXObject("Microsoft.XMLHTTP");
	request.onreadystatechange = function() {
	    if(request.readyState == 4) {
			if(request.status == 200) {
				callback(request.responseText);
			} else {
	        	alert("Error code " + request.status);
	       	}
  		}
  	}
	request.open('GET', url, true);
	request.send(null);
}
