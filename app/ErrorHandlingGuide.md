# Types of exceptions

There are three types of exceptions:

* the ones that we are **expecting and know how to handle**
* the ones that we **don't know how to handle, but are catching anyways**
* the fatal ones that will cause the app to **crash**

For the first type: 

We should handle them in our code, don't have to re-throw, don't have to send it to fabric.
For example, something exception happens in UI, we catch it, show a toast, then we can forget about it.
After all, it's an expected exception and we know what to do when it happens.

For the second type:

For example, an API from some 3rd party lib is has throws XXXException on its signature, it does not happen under normal circumstances, 
but since it has the exception on its signature, we have to catch it.

But when it does throw that exception, that means something out of our expectation has happened, we wanna know.
We should wrap them up in a LMISException and call reportToFabric to report it to fabric server.

For the third type:

For example, an API call may cause a RuntimeException, we can either re-throw it or just let it pop up to higher level of stack, both will blow up the app.
When the app does blow up, fabric will automatically report a fatal issue.
We should not eat this type of exceptions up, since it leaves the app in a nondeterministic state.

# Fatal and non-fatal issues

When the app blows up because of a unhandled exception, fabric automatically uploads it as a fatal issue.

Non-fatal issues are the ones created by us calling LMISException.reportToFabric.

# Logs during development

LMISException.reportToFabric will also print the exception to console, so we are not losing the e.printStackTrace info during development.

# Availability of network

Since the devices in health facilities do not always have network connection, the error reports that we send to fabric follows
a strategy that's similar to how we sync stock/requisition data.
 
For fatal issues:

Fabric tries to send fatal report to server right after it happens, but if network is not available at the time, the error report will be saved
locally, and Fabric lib will retry to send it next time the app is started, until it gets any luck with the network.
 
For non-fatal issues:

Non-fatal issues are always saved locally and later on sent to server when network is available.

(Tried and confirmed on virtual devices, should try in QA devices and demo devices)