/**
 *  
 * @return Object literal singleton instance of MonitorSound
 */
var NoiseMonitor = {
/**
	 * @param successCallback The callback which will be called when monitoring starts successful
	 * @param failureCallback The callback which will be called when monitoring encouters an error
	 */
	getAppVersion : function(successCallback, failureCallback) {
		return PhoneGap.exec(successCallback, //Success callback from the plugin
		failureCallback, //Error callback from the plugin
		'NoiseMonitor', //Tell PhoneGap to run "NoiseMonitor" Plugin
		'app_version', //Tell plugin, which action we want to perform
		[ 'helloPassedArgument' ]); //Passing list of args to the plugin
	},
	/**
	 * @param successCallback The callback which will be called when monitoring starts successful
	 * @param failureCallback The callback which will be called when monitoring encouters an error
	 */
	startMonitor : function(successCallback, failureCallback, telNr, threshold, alertMode, babyphoneMode) {
		return PhoneGap.exec(successCallback, //Success callback from the plugin
		failureCallback, //Error callback from the plugin
		'NoiseMonitor', //Tell PhoneGap to run "NoiseMonitor" Plugin
		'start_monitor', //Tell plugin, which action we want to perform
		[ telNr, threshold, alertMode, babyphoneMode ]); //Passing list of args to the plugin
	},
	/**
	 * @param successCallback The callback which will be called when monitoring starts successful
	 * @param failureCallback The callback which will be called when monitoring encouters an error
	 */
	testMonitor : function(successCallback, failureCallback, telNr, threshold, alertMode, babyphoneMode) {
		return PhoneGap.exec(successCallback, //Success callback from the plugin
		failureCallback, //Error callback from the plugin
		'NoiseMonitor', //Tell PhoneGap to run "NoiseMonitor" Plugin
		'test_monitor', //Tell plugin, which action we want to perform
		[ telNr, threshold, alertMode, babyphoneMode ]); //Passing list of args to the plugin
	},
	/**
	 * @param successCallback The callback which will be called when monitoring stops successful
	 * @param failureCallback The callback which will be called when monitoring encouters an error
	 */
	stopMonitor : function(successCallback, failureCallback) {
		return PhoneGap.exec(successCallback, //Success callback from the plugin
		failureCallback, //Error callback from the plugin
		'NoiseMonitor', //Tell PhoneGap to run "NoiseMonitor" Plugin
		'stop_monitor', //Tell plugin, which action we want to perform
		[ 'helloPassedArgument' ]); //Passing list of args to the plugin
	},
	/**
	 * @param successCallback The callback which will be called when monitoring stops successful
	 * @param failureCallback The callback which will be called when monitoring encouters an error
	 */
	getMicReading : function(successCallback, failureCallback) {
		return PhoneGap.exec(successCallback, //Success callback from the plugin
		failureCallback, //Error callback from the plugin
		'NoiseMonitor', //Tell PhoneGap to run "NoiseMonitor" Plugin
		'mic_amplitude_max', //Tell plugin, which action we want to perform
		[ 'helloPassedArgument' ]); //Passing list of args to the plugin
	},
	/**
	 * @param successCallback The callback which will be called when monitoring stops successful
	 * @param failureCallback The callback which will be called when monitoring encouters an error
	 */
	isMonitorRunning : function(successCallback, failureCallback) {
		return PhoneGap.exec(successCallback, //Success callback from the plugin
		failureCallback, //Error callback from the plugin
		'NoiseMonitor', //Tell PhoneGap to run "NoiseMonitor" Plugin
		'is_monitor_running', //Tell plugin, which action we want to perform
		[ 'helloPassedArgument' ]); //Passing list of args to the plugin
	}
};