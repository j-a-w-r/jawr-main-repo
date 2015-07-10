if (!window.JAWR)
	JAWR = {};

JAWR.skin = function() {

	var skinCookieName = "{JAWR_SKIN_COOKIE_NAME}";
	var skinCookieDuration = 30;

	var setStyleFromCookie = function() {
		var cssTitle = getCookie(skinCookieName);
		if (cssTitle.length) {
			switchToStyle(cssTitle);
		}
	}

	var setCookie = function(cookieName, cookieValue, lifespanInDays,
			validDomain) {
		var domainString = validDomain ? ("; domain=" + validDomain) : '';
		document.cookie = cookieName + "=" + encodeURIComponent(cookieValue)
				+ "; max-age=" + 60 * 60 * 24 * lifespanInDays + "; path=/"
				+ domainString;
	}

	var getCookie = function(cookieName) {
		if (document.cookie.length > 0) {
			var startIdx = document.cookie.indexOf(cookieName + "=");
			if (startIdx != -1) {

				startIdx = startIdx + cookieName.length + 1;
				var endIdx = document.cookie.indexOf(";", startIdx);
				if (endIdx == -1){
					endIdx = document.cookie.length;
				}
				return unescape(document.cookie.substring(startIdx, endIdx));
			}
		}
		return "";
	}

	return {
		getCurrentSkin : function() {
			return getCookie(skinCookieName);
		},

		switchToStyle : function(cssTitle) {
			if (cssTitle && cssTitle.length > 0) {
				var i, linkTag;
				for (i = 0, linkTag = document.getElementsByTagName("link"); i < linkTag.length; i++) {
					if ((linkTag[i].rel.indexOf("stylesheet") != -1)
							&& linkTag[i].title) {
						linkTag[i].disabled = true;
						if (linkTag[i].title == cssTitle) {
							linkTag[i].disabled = false;
						}
					}
				}
				setCookie(skinCookieName, cssTitle, skinCookieDuration);
			}
		}
	};
}();
