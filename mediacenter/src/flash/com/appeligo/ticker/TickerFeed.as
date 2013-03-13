import com.appeligo.util.XmlUtils;
import com.appeligo.ticker.TickerItem;

class com.appeligo.ticker.TickerFeed {
    private var data:XML;
    private var url:String;
    private var callback:Function;
    
    var tickerItems:Array;
    var channel:Number;
    
    // constructor
    function TickerFeed(url:String, channel:Number){
        this.url = url;
        this.channel = channel;
        
        this.data = new XML();
		this.data.ignoreWhite = true;
		var thisObj:TickerFeed = this;
		this.data.onLoad = function(success) {
			if (success) {
				thisObj.processTickerItems();
			}
		}
    }
    
    function setChannel(channel) {
    	this.channel = channel;
    }
	
	private function processTickerItems():Void {
		var tickerItems:Array = new Array();
		var items = XmlUtils.getElementsByTagName(data, "item");
    	trace("ticker received " + items.length + " items.");
        for(var i=0; i < items.length; i++) {
        	var title = XmlUtils.getElementText(items[i], "title");
        	var link = XmlUtils.getElementText(items[i], "link");
        	var description = XmlUtils.getElementText(items[i], "description");
        	tickerItems[i] = new TickerItem(title, link, description);
		}
		this.tickerItems = tickerItems;
		
		if (this.callback) {
			this.callback(this.tickerItems);
			this.callback = null;
		}
	}
	
    function update(callback:Function):Void {
    	var dest = this.url + this.channel;
    	trace("ticker updating url=" + dest);
    	this.callback = callback;
    	this.data.load(dest);
    }
}