/**
 * Copyright 2016 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.jawr.web.test.jmx;

import java.io.IOException;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import net.jawr.web.config.jmx.JawrApplicationConfigManagerMBean;
import net.jawr.web.config.jmx.JawrConfigManagerMBean;

/**
 * Client.java - JMX client that interacts with the JMX agent.
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrJmxClient {

	private JawrApplicationConfigManagerMBean appMbean;

	private JawrConfigManagerMBean jsMbean;

	private JawrConfigManagerMBean cssMbean;

	private JawrConfigManagerMBean binaryMbean;

	/**
	 * @throws MalformedObjectNameException 
	 * @throws IOException 
	 * 
	 */
	public JawrJmxClient() throws MalformedObjectNameException, IOException {
		// Create an RMI connector client and
		// connect it to the RMI connector server
		String host = "localhost";
		int jmxPort = 1099;
		String urlPath = String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi", host, jmxPort);
        JMXServiceURL url = new JMXServiceURL(urlPath);
		JMXConnector jmxc = JMXConnectorFactory.connect(url);

		// Get an MBeanServerConnection
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
	
		ObjectName mbeanName = new ObjectName("net.jawr.web.jmx:type=JawrAppConfigManager,prefix=default,webappContext=jawr-integration-test");

		// Create a dedicated proxy for the MBean instead of
		// going directly through the MBean server connection
		appMbean = JMX.newMBeanProxy(mbsc, mbeanName,
				JawrApplicationConfigManagerMBean.class, true);

		mbeanName = new ObjectName("net.jawr.web.jmx:type=JawrConfigManager,prefix=default,webappContext=jawr-integration-test,name=jsMBean");
		jsMbean = JMX.newMBeanProxy(mbsc, mbeanName,
				JawrConfigManagerMBean.class, true);

		mbeanName = new ObjectName("net.jawr.web.jmx:type=JawrConfigManager,prefix=default,webappContext=jawr-integration-test,name=cssMBean");
		cssMbean = JMX.newMBeanProxy(mbsc, mbeanName,
				JawrConfigManagerMBean.class, true);

		mbeanName = new ObjectName("net.jawr.web.jmx:type=JawrConfigManager,prefix=default,webappContext=jawr-integration-test,name=binaryMBean");
		binaryMbean = JMX.newMBeanProxy(mbsc, mbeanName,
				JawrConfigManagerMBean.class, true);
 
	}

	public JawrApplicationConfigManagerMBean getApplicationMBean() {
		return appMbean;
	}

	/**
	 * @return the jsMbean
	 */
	public JawrConfigManagerMBean getJsMbean() {
		return jsMbean;
	}

	/**
	 * @return the cssMbean
	 */
	public JawrConfigManagerMBean getCssMbean() {
		return cssMbean;
	}

	/**
	 * @return the binaryMbean
	 */
	public JawrConfigManagerMBean getBinaryMbean() {
		return binaryMbean;
	}

}
