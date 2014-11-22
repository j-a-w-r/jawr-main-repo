package net.jawr.wicket.resource;

import net.jawr.web.wicket.JawrWicketApplicationInitializer;

import org.apache.wicket.protocol.http.WebApplication;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see net.jawr.Start#main(String[])
 */
public class WicketApplication extends WebApplication {
	/**
	 * Constructor
	 */
	public WicketApplication() {

	}

	@Override
	protected void init() {

		JawrWicketApplicationInitializer.initApplication(this);
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<HomePage> getHomePage() {
		return HomePage.class;
	}

}
