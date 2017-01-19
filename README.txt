Dictao DACS/DVS/D3S plugin
--------------------------

This plugin enables the provisioning of DACS 1.0.10+, DxS 4.10.3+ and D3S 4.4.9+.

Using this plugin requires basic knowledge of the Dictao's product and an already installed and ready to use instance of the corresponding
products.

DACS/DVS service
----------------
It will retrieve standard information including phone, email, password and also attributes.

The three provided samples allow to :
- provision coupled DACS and DVS instances from an OpenDJ directory (see src/test/resources/etc-opendj2dacs)
- provision a PostgreSQL database from a DACS repository (see src/test/resources/etc-dacs2potsgresql)
   This last sample requires the corresponding PostgreSQL jar in the classpath

D3S service
-----------

This service is still in development

Additional options
------------------
You can also :
- bypass server SSL certificate hostname verification by adding the following JVM parameters: -DBYPASS_SSL_HOSTNAME_VERIFICATION=true 

Extra consideration
-------------------

LSC parallelism strategy issue:
Considering a event based referential, it (either source or destination service) is pulled through the getNextId method which can 
- the problem: the target is threaded using its own model (web service for example)
- the various ways to solve the problem:
	-  handle the synchronization in the destination service
		pro: do not have to handle any thread synchronization model
		cons: 
			- if a bunch requests are to be handle by the LSC, the source can be overloaded
			- it is the responsability of the source to know the level of parallelism it supports
		tech impl: probably a call back
	- continue to use LSC threading model and batching requests
		pro: 
		cons: the waiting queue itself and the "missing" result 
		tech impl: using a queue, the getNextId call of the IAsynchronousService, and the standard event-based synchronization model
	- other ways ?
	