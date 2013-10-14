/*******************************************************************************
 * ** Init Functions **
 ******************************************************************************/
function loadSettings() {
	$(document).bind("pagebeforeshow", function() {
		Settings.initFormValues();
	});
}

$(document).ready(function() {

});

$(document).bind("mobileinit", function() {
	// added these configs to disable slide transitions. They flicker on android
	// devices at the moment (since jquery mobile 1.0b2)
	$.mobile.defaultPageTransition = 'none';
	$.mobile.defaultDialogTransition = 'none';
	$.mobile.useFastClick = true;
});

$(document).bind("pagebeforeshow", function() {
	// Init Call Phone Nr.
	var telNr = Settings.getTelNr();
//	$("#callButton").button();
	if (telNr != null && telNr != "") {
		 $("#callButton .ui-btn-text").html("Call " + telNr);
//		 $("#callButton").removeClass("ui-state-disabled");
		 $("#callButton").removeAttr("disabled", "");

//		$("#callButton").val("Call " + telNr);
//		$("#callButton").button('enable');
	} else {
		$("#callButton .ui-btn-text").html("Call ");
//		$("#callButton").addClass("ui-state-disabled");
		 $("#callButton").attr("disabled","disabled");
		
//		$("#callButton").val("Call ");
//		$("#callButton").button('disable');
	}
	//	 $("#callButton").button('refresh');

	// init monitor button
	initMonitorButton();
});

var startMonitorText = 'Start Sound Monitor';
var stopMonitorText = 'Stop Sound Monitor';

function initMonitorButton() {
	// Init Start/Stop Monitor button

	NoiseMonitor.isMonitorRunning(function(m) {
		if (m == true) {
			uiMonitorButtonRunning();
		} else {
			uiMonitorButtonStopped();
		}
		
	}, failureCallback);

	// init version text for footers
	var elVersionHome = $("#footer_text_home");
	var elVersionAbout = $("#footer_text_about");

	NoiseMonitor.getAppVersion(function(m) {
		elVersionHome.html(m);
		elVersionAbout.html(m);
	}, failureCallback);
}

function uiMonitorButtonRunning() {
	var el = $("#monitorControl .ui-btn-text");
	var ui = $("#monitorControl span.ui-icon");
	el.html(stopMonitorText);
	ui.removeClass("ui-icon-check").addClass("ui-icon-delete");
}

function uiMonitorButtonStopped() {
	var el = $("#monitorControl .ui-btn-text");
	var ui = $("#monitorControl span.ui-icon");
	el.html(startMonitorText);
	ui.addClass("ui-icon-check").removeClass("ui-icon-delete");
}

/**
 * Toggle monitor button function
 */
function toggleMonitor() {
	var el = $("#monitorControl .ui-btn-text");
	var ui = $("#monitorControl span.ui-icon");
	if (el.html() == startMonitorText) {
		startMonitor(el, ui);
	} else if (el.html() == stopMonitorText) {
		stopMonitor(el, ui);
	}
};

/*******************************************************************************
 * ** All callable methods from the Noise Monitor Android service **
 ******************************************************************************/

function successCallback(message) {
	navigator.notification.alert(message, null, "SUCCESS", "OK");
}
function failureCallback(message) {
	navigator.notification.alert(message, null, "ERROR", "OK");
}

/**
 * Start the monitor
 * 
 * @param el
 *            The button element to toggle the text.
 */
function startMonitor() {
	function success(message) {
		// toggle the button text
		uiMonitorButtonRunning();
		// inform user
		successCallback(message);
	}

	function fail(message) {
		// hmm... not good, change the button anyway and inform the user
		//uiMonitorButtonRunning();
		failureCallback(message);
	}

	NoiseMonitor.startMonitor(success, fail, Settings.getTelNr(), Settings
			.getThreshold(), Settings.getAlertMode(), Settings
			.getBabyphoneMode());
}

/**
 * Stop the monitor
 * 
 * @param el
 *            The button element to toggle the text.
 */
function stopMonitor() {
	function success(message) {
		// toggle the button text
		uiMonitorButtonStopped();
		// inform user
		successCallback(message);
	}

	function fail(message) {
		uiMonitorButtonStopped();
		//failureCallback(message);
	}

	NoiseMonitor.stopMonitor(success, fail);
}

function getMicReading() {
	NoiseMonitor.getMicReading(successCallback, failureCallback);
}

function isMonitorRunning() {
	NoiseMonitor.isMonitorRunning(successCallback, failureCallback);
}

function testMonitor() {
	NoiseMonitor.testMonitor(function(){}, failureCallback, Settings.getTelNr(), Settings
			.getThreshold(), Settings.getAlertMode(), Settings
			.getBabyphoneMode());
}

/*******************************************************************************
 * ** User Settings **
 ******************************************************************************/
var Settings = {
	getTelNr : function() {
		return localStorage.getItem("telefone_nr");
	},
	getThreshold : function() {
		return localStorage.getItem("mic_threshold");
	},
	getAlertMode : function() {
		return localStorage.getItem("alert_mode");
	},
	getBabyphoneMode : function() {
		return localStorage.getItem("babyphone_mode");
	},
	saveAll : function() {
		localStorage.setItem("telefone_nr",
				document.getElementById("telnr").value);
		localStorage.setItem("mic_threshold", document
				.getElementById("threshold").value);
		localStorage.setItem("alert_mode", $("input:checked").val());
		localStorage.setItem("babyphone_mode", $("#babyphone_mode").val());
	},
	initFormValues : function() {
		// document.getElementById("telnr").value = this.getTelNr();
		// document.getElementById("threshold").value = this.getThreshold();

		// telephone settings
		var telNr = this.getTelNr();
		$("#telnr").val(telNr);

		// alert mode settings
		var mode = this.getAlertMode();
		if (mode == undefined) {
			$("#alert-mode-tel").attr("checked", true).checkboxradio("refresh");
		} else {
			$("input[value='" + mode + "']").attr("checked", true)
					.checkboxradio("refresh");
		}
		// $("input[type='radio']").attr("checked",true).checkboxradio("refresh");

		// threshold settings
		var threshold = this.getThreshold();
		if (threshold == undefined) {
			$("#threshold").val("5").slider("refresh");
		} else {
			$("#threshold").val(threshold).slider("refresh");
		}

		// babyphone mode
		var babyphoneSwitch = $("#babyphone_mode");
		var babyphoneValue = this.getBabyphoneMode();
		if (babyphoneValue == undefined || babyphoneValue == "off") {
			babyphoneSwitch[0].selectedIndex = 2;
		} else {
			babyphoneSwitch[0].selectedIndex = 1;
		}
		babyphoneSwitch.slider("refresh");
	}

}