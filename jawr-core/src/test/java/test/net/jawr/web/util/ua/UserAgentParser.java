package test.net.jawr.web.util.ua;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAgentParser {

	private static class UserAgentParserInfo {
		
		Pattern pattern;
		String familyReplacement=null;
		String v1Replacement=null;
		
		public UserAgentParserInfo(String pattern, String familyReplacement, String v1Replacement) {
			
			this.pattern = Pattern.compile(pattern);
			this.familyReplacement = familyReplacement;
			this.v1Replacement = v1Replacement;
		}
		
		public UserAgentParserInfo(String pattern, String familyReplacement) {
			
			this(pattern, familyReplacement, null);
		}
		
		public UserAgentParserInfo(String pattern) {
			
			this(pattern, null, null);
		}
		
		
		@SuppressWarnings("unused")
		public String[] matchSpan(String strUserAgent){
			
			String[] result = new String[0];
			
			Matcher matcher = pattern.matcher(strUserAgent);
			if(matcher.find()){
				
				int count = matcher.groupCount();
				result = new String[count];
				for (int i = 0; i < count; i++) {
					result[i] = matcher.group(i);
				}
			}
			    
			return result;
		}
		
		@SuppressWarnings("unused")
		public String[] parse(String strUserAgent){
			
			String family = null;
			String v1 = null;
			String v2 = null;
			String v3 = null;
			Matcher matcher = pattern.matcher(strUserAgent);
			if(matcher.find()){
				
				if(familyReplacement != null){
					family = matcher.group(1);
					if(family.indexOf(familyReplacement) != -1){
						family = family.replace(matcher.group(1), familyReplacement);
					}else{
						family = familyReplacement;
					}
				}else{
			        family = matcher.group(1);
				}
			        
				if(v1Replacement != null){
					v1 = v1Replacement;
				}else if(matcher.groupCount() >= 2){
					v1 = matcher.group(2);
				}else if(matcher.groupCount() >= 3){
					v2 = matcher.group(3);
				}else if(matcher.groupCount() >= 4){
					v3 = matcher.group(4);
				}
			}
		      
		    return new String[]{family, v1, v2, v3};
		}
	}
	
	private String browser_slash_v123_names = "Jasmine|ANTGalio|Midori|Fresco|Lobo|Maxthon|Lynx|OmniWeb|Dillo|Camino|" +
			"Demeter|Fluid|Fennec|Shiira|Sunrise|Chrome|Flock|Netscape|Lunascape|" +
			"Epiphany|WebPilot|Vodafone|NetFront|Konqueror|SeaMonkey|Kazehakase|" +
			"Vienna|Iceape|Iceweasel|IceWeasel|Iron|K-Meleon|Sleipnir|Galeon|" +
			"GranParadiso|Opera Mini|iCab|NetNewsWire|Iron|Iris";

	private String browser_slash_v12_names = 
		    "Bolt|Jasmine|Midori|Maxthon|Lynx|Arora|IBrowse|Dillo|Camino|Shiira|Fennec|" +
		    "Phoenix|Chrome|Flock|Netscape|Lunascape|Epiphany|WebPilot|" +
		    "Opera Mini|Opera|Vodafone|" +
		    "NetFront|Konqueror|SeaMonkey|Kazehakase|Vienna|Iceape|Iceweasel|IceWeasel|" +
		    "Iron|K-Meleon|Sleipnir|Galeon|GranParadiso|" +
		    "iCab|NetNewsWire|Iron|Space Bison|Stainless|Orca|Dolfin|BOLT";
	
	
	@SuppressWarnings("unused")
	private UserAgentParserInfo[] USER_AGENT_PARSERS = new UserAgentParserInfo[]{
			  // #### SPECIAL CASES TOP ####
			  // # must go before Opera
			  new UserAgentParserInfo("^(Opera)/(\\d+)\\.(\\d+) \\(Nintendo Wii'", "Wii"),
			  //# must go before Browser/v1.v2 - eg: Minefield/3.1a1pre
			  new UserAgentParserInfo("(Namoroka|Shiretoko|Minefield)/(\\d+)\\.(\\d+)\\.(\\d+(?:pre)?)",
			     "Firefox ($1)"),
			  new UserAgentParserInfo("(Namoroka|Shiretoko|Minefield)/(\\d+)\\.(\\d+)([ab]\\d+[a-z]*)?",
			     "Firefox ($1)"),
			  new UserAgentParserInfo("(MozillaDeveloperPreview)/(\\d+)\\.(\\d+)([ab]\\d+[a-z]*)?"),
			  new UserAgentParserInfo("(SeaMonkey|Fennec|Camino)/(\\d+)\\.(\\d+)([ab]?\\d+[a-z]*)"),
			  //# e.g.: Flock/2.0b2
			  new UserAgentParserInfo("(Flock)/(\\d+)\\.(\\d+)(b\\d+?)"),

			  //# e.g.: Fennec/0.9pre
			  new UserAgentParserInfo("(Fennec)/(\\d+)\\.(\\d+)(pre)"),
			  new UserAgentParserInfo("(Navigator)/(\\d+)\\.(\\d+)\\.(\\d+)", "Netscape"),
			  new UserAgentParserInfo("(Navigator)/(\\d+)\\.(\\d+)([ab]\\d+)", "Netscape"),
			  new UserAgentParserInfo("(Netscape6)/(\\d+)\\.(\\d+)\\.(\\d+)", "Netscape"),
			  new UserAgentParserInfo("(MyIBrow)/(\\d+)\\.(\\d+)", "My Internet Browser"),
			  new UserAgentParserInfo("(Firefox).*Tablet browser (\\d+)\\.(\\d+)\\.(\\d+)", "MicroB"),
			  //# Opera will stop at 9.80 and hide the real version in the Version string.
			  //# see: http://dev.opera.com/articles/view/opera-ua-string-changes/
			  new UserAgentParserInfo("(Opera)/.+Opera Mobi.+Version/(\\d+)\\.(\\d+)",
			      "Opera Mobile"),
			  new UserAgentParserInfo("(Opera)/9.80.*Version\\/(\\d+)\\.(\\d+)(?:\\.(\\d+))?"),

			  //# Palm WebOS looks a lot like Safari.
			  new UserAgentParserInfo("(webOS)/(\\d+)\\.(\\d+)", "Palm webOS"),

			  new UserAgentParserInfo("(Firefox)/(\\d+)\\.(\\d+)\\.(\\d+(?:pre)?) \\(Swiftfox\\)", "Swiftfox"),
			  new UserAgentParserInfo("(Firefox)/(\\d+)\\.(\\d+)([ab]\\d+[a-z]*)? \\(Swiftfox\\)", "Swiftfox"),

			  //# catches lower case konqueror
			  new UserAgentParserInfo("(konqueror)/(\\d+)\\.(\\d+)\\.(\\d+)", "Konqueror"),

			  //# Maemo

			  //#### END SPECIAL CASES TOP ####

			  //#### MAIN CASES - this catches > 50% of all browsers ####
			  //# Browser/v1.v2.v3
			  new UserAgentParserInfo("(%s)/(\\d+)\\.(\\d+)\\.(\\d+) "+ browser_slash_v123_names),
			  //# Browser/v1.v2
			  new UserAgentParserInfo("(%s)/(\\d+)\\.(\\d+)" + browser_slash_v12_names),
			  //# Browser v1.v2.v3 (space instead of slash)
			  new UserAgentParserInfo("(iRider|Crazy Browser|SkipStone|iCab|Lunascape|Sleipnir|Maemo Browser) (\\d+)\\.(\\d+)\\.(\\d+)"),
			  //# Browser v1.v2 (space instead of slash)
			  new UserAgentParserInfo("(iCab|Lunascape|Opera|Android) (\\d+)\\.(\\d+)"),
			  new UserAgentParserInfo("(IEMobile) (\\d+)\\.(\\d+)", "IE Mobile"),
			  //# DO THIS AFTER THE EDGE CASES ABOVE!
			  new UserAgentParserInfo("(Firefox)/(\\d+)\\.(\\d+)\\.(\\d+)"),
			  new UserAgentParserInfo("(Firefox)/(\\d+)\\.(\\d+)(pre|[ab]\\d+[a-z]*)?"),
			  //#### END MAIN CASES ####

			  //#### SPECIAL CASES ####
			  //#_P(""),
			  new UserAgentParserInfo("(Obigo|OBIGO)[^\\d]*(\\d+)(?:.(\\d+))?", "Obigo"),
			  new UserAgentParserInfo("(MAXTHON|Maxthon) (\\d+)\\.(\\d+)", "Maxthon"),
			  new UserAgentParserInfo("(Maxthon|MyIE2|Uzbl|Shiira)", null, "0"),
			  new UserAgentParserInfo("(PLAYSTATION) (\\d+)", "PlayStation"),
			  new UserAgentParserInfo("(PlayStation Portable)[^\\d]+(\\d+).(\\d+)"),
			  new UserAgentParserInfo("(BrowseX) \\((\\d+)\\.(\\d+)\\.(\\d+)"),
			  new UserAgentParserInfo("(POLARIS)/(\\d+)\\.(\\d+)", "Polaris"),
			  new UserAgentParserInfo("(BonEcho)/(\\d+)\\.(\\d+)\\.(\\d+)", "Bon Echo"),
			  new UserAgentParserInfo("(iPhone) OS (\\d+)_(\\d+)(?:_(\\d+))?"),
			  new UserAgentParserInfo("(iPad).+ OS (\\d+)_(\\d+)(?:_(\\d+))?"),
			  new UserAgentParserInfo("(Avant)", null, "1"),
			  new UserAgentParserInfo("(Nokia)[EN]?(\\d+)"),
			  new UserAgentParserInfo("(Black[bB]erry).+Version\\/(\\d+)\\.(\\d+)\\.(\\d+)",
			      "Blackberry"),
			  new UserAgentParserInfo("(Black[bB]erry)\\s?(\\d+)", "Blackberry"),
			  new UserAgentParserInfo("(OmniWeb)/v(\\d+)\\.(\\d+)"),
			  new UserAgentParserInfo("(Blazer)/(\\d+)\\.(\\d+)", "Palm Blazer"),
			  new UserAgentParserInfo("(Pre)/(\\d+)\\.(\\d+)", "Palm Pre"),
			  new UserAgentParserInfo("(Links) \\((\\d+)\\.(\\d+)"),
			  new UserAgentParserInfo("(QtWeb) Internet Browser/(\\d+)\\.(\\d+)"),
			  //#_P("\(iPad;.+(Version)/(\d+)\.(\d+)(?:\.(\d+))?.*Safari/',
			  //#   family_replacement='iPad"),
			  new UserAgentParserInfo("(Version)/(\\d+)\\.(\\d+)(?:\\.(\\d+))?.*Safari/","Safari"),
			     new UserAgentParserInfo("(OLPC)/Update(\\d+)\\.(\\d+)"),
			  new UserAgentParserInfo("(OLPC)/Update()\\.(\\d+)","0"),
			  new UserAgentParserInfo("(SamsungSGHi560)", "Samsung SGHi560"),
			  new UserAgentParserInfo("^(SonyEricssonK800i", "Sony Ericsson K800i"),
			  new UserAgentParserInfo("(Teleca Q7)"),
			  new UserAgentParserInfo("(MSIE) (\\d+)\\.(\\d+)", "IE")
	};

}
