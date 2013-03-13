
class com.appeligo.flash.ui.ScrollingList extends MovieClip {
	private var scrollingEnabled = true;
	private var clipArray;
	private var orientation = "horizontal";
	private var spacing = 10;
	private var mouseBuffer = 20;
	private var totalWidth = 0;
	private var totalHeight = 0;
	private var autoScroll = false;
	
	private var boundsTop = 0;
	private var boundsBottom = 0; 
	private var boundsLeft = 0;
	private var boundsRight = 0;
	
	/* Holds the delta between the mouse position and the center of the screen */
	private var mouseDeltaX = 0;
	private var mouseDeltaY = 0;
	
	/* The reference point from which we will determine the clip locations */
	private var originX = 0;
	private var originY = 0;
	
	/* Friction needed for falloff */
	private var friction = 1 + (5 / 150);  // 1 + (FRICTION / 150)
	
	private var movieWidth;
	private var movieHeight;
	private var movieCenterX;
	private var movieCenterY;
	
	private var clipBaseName;
	private var clipCount;

	/*
	 * These are really big numbers that we use to determine the location of the
	 * clips.  We cube that number so it becomes really big.  Dividing those numbers
	 * into it seems to solve the problem.
	 */
	private var constX;
	private var constY;
	
	private var smooth = false;
	private var looping = true;
	private var scrollSpeed = 3;
    
    private function init(movieX, movieY, movieWidth, movieHeight) {
    trace("clipCount="+clipCount);
		var tempX = movieX;
		var tempY = movieY;
		
    	clipArray = createClipArray(clipBaseName, clipCount);
		
		boundsTop = movieY;
		boundsBottom = movieY + movieHeight; 
		boundsLeft = movieX;
		boundsRight = movieX + movieWidth;
		
		for (var i = 0; i < clipArray.length; i++) {
			if (orientation == "horizontal") {
				tempX += spacing;
				clipArray[i]._x = tempX + getClipOffset(clipArray[i], "x");
				clipArray[i]._y = tempY + getClipOffset(clipArray[i], "y");
				tempX += clipArray[i]._width;
				
			} else {
				tempY += spacing;
				clipArray[i]._x = tempX + getClipOffset(clipArray[i], "x");
				clipArray[i]._y = tempY + getClipOffset(clipArray[i], "y");
				tempY += clipArray[i]._height;
			}
		}
			
		boundsTop -= mouseBuffer;
		boundsBottom += mouseBuffer;
		boundsLeft -= mouseBuffer;
		boundsRight += mouseBuffer;
		
		
		this.movieWidth = movieWidth;
		this.movieHeight = movieHeight;
		movieCenterX = boundsLeft + mouseBuffer + movieWidth / 2;
		movieCenterY = boundsTop + mouseBuffer + movieHeight / 2;
		
		totalWidth = Math.max(tempX - getBounds(_parent).xMin, movieWidth);
		totalHeight = Math.max(tempY - getBounds(_parent).yMin, movieHeight);
		
		trace("width " + this.movieWidth);
		trace("height " + this.movieHeight);
		//trace("centerx " + movieCenterX);
		//trace("totalWidth " + totalWidth);
		
		constX = (Math.pow(movieCenterX - boundsLeft, 3) + Math.pow(movieWidth, 3) +
			Math.pow(movieCenterX - boundsLeft, 2) + Math.pow(movieWidth, 2)) / 4;
		constY = (Math.pow(movieCenterY - boundsTop, 3) + Math.pow(movieHeight, 3) +
			Math.pow(movieCenterY - boundsTop, 2) + Math.pow(movieHeight, 2)) / 4;
    }
	
	function setScrollingEnabled(enable) {
		scrollingEnabled = enable;
	}
	
	function isScrollingEnabled() {
		return scrollingEnabled;
	}
	
	function setOrientation(orientation) {
		this.orientation = orientation;
	}
	
	function getOrientation() {
		return orientation;
	}
	
	function setSpacing(spacing) {
		this.spacing = spacing;
	}
	
	function getSpacing() {
		return spacing;
	}
	
	function setMouseBuffer(mouseBuffer) {
		this.mouseBuffer = mouseBuffer;
	}
	
	function getMouseBuffer() {
		return mouseBuffer;
	}
	
	function setAutoScroll(autoScroll) {
		this.autoScroll = autoScroll;
	}
	
	function getAutoScroll() {
		return autoScroll;
	}
	
	function setClipBaseName(clipBaseName) {
		this.clipBaseName = clipBaseName;
	}
	
	function getClipBaseName() {
		return clipBaseName;
	}
	
	function setClipCount(clipCount) {
		this.clipCount = clipCount;
	}
	
	function getClipCount() {
		return clipCount;
	}
	
	function setFriction(friction) {
		this.friction = 1 + (friction / 150);
	}
	
	function getClipOffset(clipObj, axis) {
		var tempOffset = 0;
		if (axis == "x") {
			tempOffset = clipObj._x - clipObj.getBounds(_parent).xMin;
			
		} else if (axis == "y") {
			tempOffset = clipObj._y - clipObj.getBounds(_parent).yMin;
		} 
		return tempOffset;
	}
	
	private function createClipArray(baseName, total) {
	trace("createClipArray " + total);
		var tempArray = new Array();
		for (var i = 0; i < total; i++) {
			if (null != _parent[baseName + i]) {
				tempArray[i] = _parent[baseName + i];
			}
		}
		return tempArray;
	}
	
	function isMouseOver(mouseX, mouseY) {
		if (getOrientation() == "horizontal") {
			return mouseY >= boundsTop && mouseY <= boundsBottom;
		} else {
			return mouseX >= boundsLeft && mouseX <= boundsRight;
		}
	}
    
    function getActiveClip() {
		for (var i = 0; i < clipArray.length; i++) {
			if (orientation == "horizontal") {
				if (clipArray[i]._x <= movieCenterX && 
					(clipArray[i]._x + clipArray[i]._width + spacing) >= movieCenterX) {
					return clipArray[i];
				}
			} else {
				if (clipArray[i]._y <= movieCenterY && 
					(clipArray[i]._y + clipArray[i]._height + spacing) >= movieCenterY) {
					return clipArray[i];
				}
			}
		}
		if (clipArray.length > 0) {
	    	return clipArray[0];
	    } else {
	    	return null;
	    }
    }
	
	function scrolling(mouseX, mouseY) {
		var isOver = isMouseOver(mouseX, mouseY);
		
		if (!isOver && autoScroll) {
			mouseX = movieCenterX + (movieWidth*0.3);
			mouseY = movieCenterY + (movieHeight/2);
			isOver = true;
		}	
		
		if (isOver && scrollingEnabled) {
			// save the old distance
			var oldMouseDeltaX = mouseDeltaX;
			var oldMouseDeltaY = mouseDeltaY;
		
			// calculate the distance the mouse is from the 
			// center of the screen and "ease in" if needed
			if (smooth) {
				mouseDeltaX = oldMouseDeltaX + ((mouseX - movieCenterX - oldMouseDeltaX) / (friction * 2))
				mouseDeltaY = oldMouseDeltaY + ((mouseY - movieCenterY - oldMouseDeltaY) / (friction * 2))
			} else {
				mouseDeltaX = mouseX - movieCenterX;
				mouseDeltaY = mouseY - movieCenterY;
			}
		
		} else {
			// if smoothing is true, descelerate to 0
			if (smooth) {
				mouseDeltaX = mouseDeltaX / friction;
				mouseDeltaY = mouseDeltaY / friction;
			} else {
				mouseDeltaX = 0;
				mouseDeltaY = 0;
			}	
		}
		
		//trace("mouseDeltaX " + mouseDeltaX);
		
		var changed = false;
		if (orientation == "horizontal" && mouseDeltaX != 0) {
			// here the mouse delta is multiplied by the scrolling speed
			// we then take that number and cube it. if you took a range
			// of numbers and cubed them, the last third of the list would
			// be much, much larger than the first two thirds, giving
			// the effect of a slow speed up for the first two thirds of
			// the mouse activation area and a quick speed up over the last
			// third. this offers the user a bit more control when in the center
			// portion of the list. this results in a very large number for us
			// so we divide it by another large number (constX) and get a 
			// x value that we can subtract from our reference point.
			originX = originX - Math.pow(mouseDeltaX * scrollSpeed, 3) / constX;
		
			// this holds the offset for the other clips as we loop through them
			var clipOffsetX = 0;
		
			// loop through the array of objects and reposition them
			for (var i = 0; i < clipArray.length; i++) {
		
				// check our reference point... if its too large
				// we reset it so our clips don't fly away ;)
				if (originX < -totalWidth) {
					originX = originX % totalWidth;
				} else if (originX > totalWidth) {
					originX = originX % totalWidth;
				}
		
				// if it is set not to loop, reset the reference point
				// so that the list stops when it gets to either of the ends
				if (!looping) {
					if (originX > 1) {
						originX = 1;
					} else if (originX < movieWidth - totalWidth + 1 + spacing) {
						originX = movieWidth - totalWidth + 1 + spacing;
					}
				}
		
				// calculate the new x position for the clip and check to see
				// if it moves past the movie boundaries. if it does, move the 
				// clip to the opposite end of the list. this gives us the 
				// looping effect.
				var newX = originX + clipOffsetX;
				if (newX + clipArray[i]._width < 0) {
					newX += totalWidth;
				} else if (newX > movieWidth) {
					newX -= totalWidth;
				}
				newX += boundsLeft;
		
				// set the clip to its new position and update the offset for the next clip
				if (Math.abs(clipArray[i]._x - (newX + getClipOffset(clipArray[i], "x"))) > 0.1) {
					changed = true;
				}
				clipArray[i]._x = newX + getClipOffset(clipArray[i], "x");
				clipOffsetX += clipArray[i]._width + spacing;
			}
		
		} else if (orientation == "vertical" && mouseDeltaY != 0) {
			// the same code for a vertical list
			originY = originY - Math.pow(mouseDeltaY * scrollSpeed, 3) / constY;
		
			var clipOffsetY = 0;
			for (var i = 0; i < clipArray.length; i++) {
		
				if (originY < -totalHeight) {
					originY = originY % totalHeight;
				} else if (originY > totalHeight) {
					originY = originY % totalHeight;
				}
		
				// if it is set not to loop, reset the reference point
				// so that the list stops when it gets to either of the ends
				if (!looping) {
					if (originY > 1) {
						originY = 1;
					} else if (originY < movieHeight - totalHeight + 1 + spacing) {
						originY = movieHeight - totalHeight + 1 + spacing;
					}
				}
		
				var newY = originY + clipOffsetY;
				if (newY + clipArray[i]._height < 0) {
					newY = totalHeight + newY;
				} else if (newY > movieHeight) {
					newY = newY - totalHeight;
				}
				newY += boundsTop;
		
				if (Math.abs(clipArray[i]._y - (newY + getClipOffset(clipArray[i], "y"))) > 0.1) {
					changed = true;
				}
				clipArray[i]._y = newY + getClipOffset(clipArray[i], "y");
				clipOffsetY += clipArray[i]._height + spacing;
			}
		}
			
		if (!changed) {
			mouseDeltaX = 0;
			mouseDeltaY = 0;
		}
	}
	
	function reset() {
		gotoAndPlay(1);
	}
}