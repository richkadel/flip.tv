import com.appeligo.ticker.TickerItem;

class com.appeligo.flash.ui.LinkItem extends MovieClip {
	private var label;
	private var tickerItem:TickerItem;
	
	function init(label) {
		this.label = label;
		this.label.autoSize = true;
	}

	function setTickerItem(tickerItem:TickerItem) {
		this.tickerItem = tickerItem;
		label.text = tickerItem.title;
	}
	
	function getTickerItem():TickerItem {
		return tickerItem;
	}
}