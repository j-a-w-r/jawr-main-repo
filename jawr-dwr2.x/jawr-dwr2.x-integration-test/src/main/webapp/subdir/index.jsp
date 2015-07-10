<%@ taglib uri="http://jawr.net/tags" prefix="jwr"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>

<jwr:style src="/fwk/core/component.css" media="all" />
<jwr:script src="/bundles/global.js" />
<jwr:script src="/simpletext/index.js" />
<jwr:script src="/js/bundle/msg.js" />
</head>
<body>
	<div class="mandatory">For this element, the style and the image
		associated are retrieved directly from the classpath.</div>
	<div style="height: 20px">
		<jwr:img src="../img/appIcons/application.png" />
		This HTML image use a generated path which force the caching for the
		browser.
	</div>
	<div>
		<span class="calendar">The CSS image is retrieved from the
			webapp, which is a classic case.</span>
	</div>
	<div>
		<span class="clock">The CSS image is retrieved from the
			classpath. The CSS used here is defined in the webapp under the "css"
			folder.<br> This mean that you could reference a CSS image in
			the classpath from a standard CSS define in a bundle.
		</span>
	</div>
	<div style="height: 20px">
		<jwr:image value="temp" src="img/cog.png" />
		This input image use a generated path which force the caching for the
		browser.
	</div>

	<script type="text/javascript">
		alert("A little message retrieved from the message bundle : "
				+ messages.ui.msg.hello.world());
	</script>

	<div>
		<h1>Dynamically Updating Text</h1>

		<p>This is a simple demonstration of how to dynamically update a
			web-page with text fetched from a web server.</p>

		<ul id="tabList">
			<li><a href="#" tabId="demoDiv">Demo</a></li>
			<li><a href="#" tabId="explainDiv">How it works</a></li>
			<li><a href="#" tabId="sourceDiv">Source</a></li>
		</ul>

		<div id="tabContents">

			<div id="demoDiv">

				<p>
					Name: <input type="text" id="demoName" value="Joe" /> <input
						value="Send" id="sendButton" type="button" onclick="update()" /> <br /> Reply: <span
						id="demoReply"
						style="background: #eeffdd; padding-left: 4px; padding-right: 4px;"></span>
				</p>

			</div>

			<div id="explainDiv">
				<p>
					When you click on the "Send" button the browser calls the onclick
					event, which calls the
					<code>update()</code>
					function:
				</p>

				<pre>
function update() {
  var name = dwr.util.getValue("demoName");
  Demo.sayHello(name, loadinfo);
}
</pre>
			</div>
		</div>
	</div>
</body>
</html>
