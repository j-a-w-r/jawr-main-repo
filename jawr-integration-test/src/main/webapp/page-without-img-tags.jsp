<%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>    
 <jwr:style src="/fwk/core/component.css" media="all" />
 <jwr:script src="/js/bundle/msg.js" />
</head>
<body>
<div class="mandatory">
	For this element, the style is retrieved directly from the classpath, the image is retrieved from the webapp.
</div>
<div>
	<span class="calendar">The CSS image is retrieved from the webapp, which is a classic case.</span>
</div>

<script type="text/javascript">
	alert("A little message retrieved from the message bundle : "+messages.ui.msg.hello.world());
</script>
</body>
</html>
