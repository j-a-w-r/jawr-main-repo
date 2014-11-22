<html xmlns:wicket="http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd" >
<html>
<head>
<wicket:jawr>
	<link rel="stylesheet" type="text/css" href="/fwk/core/component.css" media="all" />
	<script type="text/javascript" src="/js/bundle/msg.js" ></script>
</wicket:jawr>
</head>
</head>
<body>
<div class="mandatory">
	For this element, the style and the image associated are retrieved directly from the classpath.
</div>
<div style="height: 20px">
	<wicket:jawr>
		<img src="/img/appIcons/application.png"/> This HTML image use a generated path which force the caching for the browser.
	</wicket:jawr> 
</div>
<div>
	<span class="calendar">The CSS image is retrieved from the webapp, which is a classic case.</span>
</div>
<div>
	<span class="clock">The CSS image is retrieved from the classpath. The CSS used here is defined in the webapp under the "css" folder.<br>
	This mean that you could reference a CSS image in the classpath from a standard CSS define in a bundle.</span>
</div>
<div style="height: 20px">
	<wicket:jawr>
		<image value="temp" src="/img/cog.png"/> This input image use a generated path which force the caching for the browser.
	</wicket:jawr>
</div>

<script type="text/javascript">
	alert("A little message retrieved from the message bundle : "+messages.ui.msg.hello.world());
</script>
</body>
</html>