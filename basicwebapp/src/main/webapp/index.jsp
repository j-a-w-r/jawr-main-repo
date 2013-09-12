<%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>    
 <jwr:style src="/css/common.css" displayAlternate="true" media="all" />
 <jwr:style src="/css/specific.css" media="all" />
  
 <jwr:script src="/js/common.js" />
 <jwr:script src="/js/bundle/msg.js" />
</head>
<body class="yui-skin-sam">

<div class="header">&nbsp;</div>

<div class="exampleIntro">
	<p>This page demonstrates some Jawr features.</p>
</div>

<div id="panelMessage">
	<div class="hd">Message</div>
	<div class="bd">This is a Panel that was marked up in the document.</div>
</div>

<div id="tabView" class="yui-navset">
    <ul class="yui-nav">
        <li class="selected"><a href="#tab1"><em>Internationalization</em></a></li>
        <li><a href="#tab2"><em>Skin</em></a></li>
        <li><a href="#tab3"><em>Generated content</em></a></li>
        <li><a href="#tab4"><em>Generate sprite image</em></a></li>
        <li><a href="#tab5"><em>Generate base64 image</em></a></li>
    </ul>            
    <div class="yui-content">
        <div id="tab1">
        	<p>Jawr allows you to define bundles which will change depending on the user locale </p>
        	<button id="localeMsgButton">Display localized message</button>
        </div>
        <div id="tab2">
        	<p>Jawr provides a way to define skin for your CSS bundle<br/>
        	The skin can also vary depending of the user locale. You can check by changing the locale of your browser to French, Spain or US.
        	</p>
        	<form>
				Select your skin :<select name="theme" id="themeSwitcher" onchange="JAWR.skin.switchToStyle(this.value);">
					<option value="">&nbsp;</option>
					<option value="oceanBlue">Ocean Blue</option>
					<option value="greyStorm">Grey Storm</option>
				</select>
				</form>
        </div>
        <div id="tab3">
        	<p>Jawr allows you to define generated content</p>
        	<div class="mandatory">
				For this element, the style and the image associated are retrieved directly from the classpath.
			</div>
			<div style="height: 20px">
				<jwr:img src="img:/cog.png" /> This HTML image use a generated path which force the caching for the browser.  
			</div>
			<div>
				<span class="calendar">The CSS image is retrieved from the webapp, which is a classic case.</span>
			</div>
			<div>
				<span class="clock">The CSS image is retrieved from the classpath. The CSS used here is defined in the webapp under the "css" folder.<br>
				This mean that you could reference a CSS image in the classpath from a standard CSS define in a bundle.</span>
			</div>
			<div style="height: 20px">
				<jwr:image value="temp" src="/img/cog.png"/> This input image use a generated path which force the caching for the browser. 
			</div>
        </div>
        <div id="tab4">
        	<p>Jawr allows you to define CSS sprite, which will be generated during the bundle processing using <a href="http://csssprites.org/">Smartsprites</a>.<br>
        	You will find below images where the references have been replaced by a sprite which has been dynamically generated.
        	This features requires to use Java 5 and the Smartsprites library.
        	</p>
        	<div class="mandatory">
				The image defined here is part of a generated sprite.
			</div>
			<div class="clock">
				  This one also
			</div>
        </div>
        <div id="tab5">
        	<p>Jawr allows you to generated base64 image in your CSS and your HTML images.<br/>
        	The major advantage of using the base64 image is that the browser don't need to make a request to the server for this image because it is embedded in the HTML page.
        	The base64 encoded image can be used directly for all major browser, except IE versions before 8.
        	On these browsers, for CSS images, we use MHTML.
        	And for HTML images, instead of generating the base64 encoded data, we reference it by their normal URLs.  
        	</p>
        	<div class="base64Clock">
				The image defined here is part of a generated sprite.
			</div>
			<div style="height: 20px">
				  <jwr:img src="/img/cog.png" base64="true" /> Jawr has generated the base64 encoded data for this HTML image, except on IE versions before 8.
			</div>
        </div>
    </div>
</div>
<script>
(function() {
    
    YAHOO.namespace("jawr.container");

    function init() {
		var tabView = new YAHOO.widget.TabView('tabView');

		// Instantiate a Panel from markup
		YAHOO.jawr.container.panelMsg = new YAHOO.widget.Panel("panelMessage", { width:"320px", visible:false, constraintoviewport:true, modal : true, fixedcenter : true } );
		YAHOO.jawr.container.panelMsg.render();

		YAHOO.util.Event.addListener("localeMsgButton", "click", displayLocaleMsg);

		// Initialize the theme swicther select box
		var themeSwitcher = document.getElementById("themeSwitcher");
		themeSwitcher.value = JAWR.skin.getCurrentSkin();
	}

    function displayLocaleMsg(){

    	YAHOO.jawr.container.panelMsg.setBody(messages.ui.msg.hello.world());
    	YAHOO.jawr.container.panelMsg.show();
    }
	
	YAHOO.util.Event.addListener(window, "load", init);
})();
</script>

</body>