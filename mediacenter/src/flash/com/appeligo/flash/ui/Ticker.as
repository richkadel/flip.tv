import com.appeligo.ticker.TickerFeed;
import com.appeligo.ticker.TickerItem;
import com.appeligo.flash.ui.ScrollingList;

class com.appeligo.flash.ui.Ticker extends MovieClip {
	private var lastUpdate = null;
	private var updateInterval = 30000;
	
	private var ticker:TickerFeed;
	private var list:ScrollingList;
	
	function setChannel(channel) {
		lastUpdate = null;
		ticker.setChannel(channel);
		ticker.update();
	}
	
    function getTickerFeed() {
    	return ticker;
    }
	
    function setTickerFeed(ticker) {
    	this.ticker = ticker;
    }
	
    function setScrollingList(list) {
    	this.list = list;
    }
	
    function getScrollingList() {
    	return this.list;
    }
	
    function getUpdateInterval() {
    	return updateInterval;
    }
	
    function setUpdateInterval(updateInterval) {
    	this.updateInterval = updateInterval;
    }
    
    function getActiveItem():TickerItem {
    	var linkItem = this.list.getActiveClip();
    	return linkItem.getTickerItem();
    }
	
	function update() {
		var now = new Date();
		if (lastUpdate != null && lastUpdate.getTime() + updateInterval > now.getTime()) {
			return;
		}
		if (!this.list.isScrollingEnabled()) {
			return;
		}
		lastUpdate = now;
		
		var thisObj:Ticker = this;
		ticker.update(function(items) {
			thisObj.processTickerItems(items);
		});
	}
	
	function processTickerItems(items:Array) {
		for(var i=0; i < ticker.tickerItems.length; i++) {
			if (this["tickerItem" + i]) {
				this["tickerItem" + i].removeMovieClip();
			}
			this.attachMovie("linkItem", "tickerItem" + i, i+1);
			this["tickerItem" + i]._x = i*50;
			this["tickerItem" + i]._y = 0;
			this["tickerItem" + i].setTickerItem(ticker.tickerItems[i]);
		}
		list.setClipCount(ticker.tickerItems.length);
		list.reset();
	}
}