class com.appeligo.util.XmlUtils {
	static function getText(node:XMLNode):String {
		var text:String = "";
		if (node.nodeType == 3 || node.nodeType == 4) {
			text = node.nodeValue;
		}
		
		var children = node.childNodes;
        for(var i=0; i < children.length; i++) {
        	text += getText(children[i]);
        }
		return text;
	}
	
	static function getElementsByTagName(xmlDoc, tagname, xmlObjArray){
	    var Nodes = null;
	    if (xmlObjArray == null) {
	        Nodes = new Array();
	    } else {
	        Nodes = xmlObjArray;
	    }
	
	    if (xmlDoc.hasChildNodes()){
	        for (var i=0; i < xmlDoc.childNodes.length; i++) {
	            if( (xmlDoc.childNodes[i].nodeType == 1) &&
	                (xmlDoc.childNodes[i].nodeName == tagname)) {
	                Nodes.push(xmlDoc.childNodes[i]);
	            }
	            getElementsByTagName(xmlDoc.childNodes[i], tagname, Nodes);
	        }
	    }
	    return Nodes;
	}
	
	static function getElementByTagName(xmlDoc, tagname){
	    if (xmlDoc.hasChildNodes()){
	        for (var i=0; i < xmlDoc.childNodes.length; i++) {
	            if( (xmlDoc.childNodes[i].nodeType == 1) &&
	                (xmlDoc.childNodes[i].nodeName == tagname)) {
	                return xmlDoc.childNodes[i];
	            }
	        }
	    }
	    return null;
	}
	
	static function getElementText(xmlDoc, tagname):String {
	    if (xmlDoc.hasChildNodes()){
	        for (var i=0; i < xmlDoc.childNodes.length; i++) {
	            if( (xmlDoc.childNodes[i].nodeType == 1) &&
	                (xmlDoc.childNodes[i].nodeName == tagname)) {
	                return getText(xmlDoc.childNodes[i]);
	            }
	        }
	    }
	    return null;
	}
}