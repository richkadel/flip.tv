
// NOT IN USE... SEE COMMENTS BELOW IN DocumentComplete



//////////////////////////////////////////////
// Global Objects
//--------------------------------
// There are two global object in this script: MainWindow and ToolbarControl.
// MainWindow object allows you to access the main browser window via the following expression:
//
// MainWindow.document.parentWindow
//
// E.g. if you want to give an alert use the following code:
//
// MainWindow.document.parentWindow.alert('Document complete!!!!!');


//////////////////////////////////////////////
// Global Code Space
//--------------------------------
// Code in the main (non-fuction) space will be launched once: on toolbar loading.
// Use it to initialize some variables or other things.
//
// To see this in action please uncomment the following line of code:
//
//MainWindow.document.parentWindow.alert('Toolbar '+ToolbarControl.ver+' is loaded.');

//////////////////////////////////////////////
// DocumentComplete function
//--------------------------------
// It is called each time the page or toolbar is reloaded 
// (parameter 'type' is "window" or "toolbar" correspondingly).

function DocumentComplete(tool, type) 
{
//-- use the following code to reload the toolbar once
	CoreDocumentComplete(tool, type);
	if(type == "window") {
		var url=tool.url;
	MainWindow.document.parentWindow.alert('From the server...Document complete!'+url);
		var www = "http://www.";
		if (url.indexOf(www) == 0) {
			url = "http://"+url.substring(www.length);
		}
		if (
// THIS ISN'T WORKING SO IT'S NOT BEING CALLED.  FOR ONE THING, I DON'T THINK YOU CAN HAVE TWO DocumentComplete METHODS
// IN TWO DIFFERENT INCLUDED SCRIPTS FROM THE TOOLBAR.  BUT CHANGING IT TO CALL CoreDocumentComplete (above) DIDN'T WORK
// EITHER.  SO I MOVED THE LOCALHOST BACK TO THE CORE.
//			url.indexOf("http://google.com/") == 0 ||
			url.indexOf("http://localhost:8080/search/search.action") == 0 ||
			
			false) {

			tool.reload();
		}
	}
}

// Run JavaScript from a button of the toolbar.
// Just assign the script to a button/menu item using 'Launch Script' command.

//function Launch(tool)
//{
//                MainWindow.document.parentWindow.alert ("Your javascript function!");
//}
