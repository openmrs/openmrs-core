// timer
var timer;
var is_timer_running = 0;
var time_out;
// number of times no data is received from server.
var ajax_attempts = 0;

$j(document).ready( function() {

	var isMigrationRequired = $j("#archive_migration_form").is(':visible');
	var isMigrationRunning = $j.trim($j("#migration_status").text());
	if (isMigrationRunning != null && isMigrationRunning == "RUNNING") {
		time_out = Number($j("#time_out").text()) + 100;
		$j("#number_span").show();
		$j("#status_span").show();
		$j("#archive_migration_warning").hide();
		$j("#archive_migration_progress_img").show();
		setOnClickHandlers();
		// to make sure that ajax calls to continue being sent
		is_timer_running = 1;
		getMigrationStatus();
	} else if (!isMigrationRequired) {// if migration isn't required
		$j("#archive_migration_archive_dir").show();
	} else if (isMigrationRequired) { // migration is required
		time_out = Number($j("#time_out").text()) + 100;
		setOnClickHandlers();
	}
});

function setOnClickHandlers() {
	// when user clicks start button
	$j("#startButton").click( function() {
		startHl7InArchiveMigration();
	});

	// when user clicks stop button
	$j("#stopButton").click( function() {
		stopHl7InArchiveMigration();
	});
}

// calls the server via ajax to start the hl7 migration
// the returned array from the dwr call, in the first index of the array is a
// boolean value
// indicating if the process started or not, in the second is a descriptive
// message
function startHl7InArchiveMigration() {
	DWRHL7Service.startHl7ArchiveMigration( function(reply) {
		// if the migration process started, keep retrieving log messages from the
			// server about
			// the progress
			if (reply[0] == true) {
				$j("#number_span").show();
				$j("#status_span").show();
				$j("#archive_migration_progress_img").show();
				is_timer_running = 1;
				$j("#startButton").attr("disabled", true);
				$j("#archive_migration_warning").hide();
				$j("#stopButton").show();
				getMigrationStatus();
			} else {
				$j("#archive_migration_status")
						.html(
								"<img src=\""+openmrsContextPath+"/images/alert.gif\" /> "
										+ reply[1]);
				$j("#number_span").hide();
				$j("#status_span").hide();
				$j("#startButton").attr("disabled", true);
			}

			// if user clicked stop after page reload
			// we need to hide it when user clicks start again
			if ($j("#archive_migration_stop_status").is(':visible')) {
				$j("#archive_migration_stop_status").hide();
			}
		});

}// end function startHl7InArchiveMigration()

// calls the server via ajax to stop the hl7 migration process
function stopHl7InArchiveMigration() {
	// set stop flag
	is_timer_running = 0;
	DWRHL7Service.stopHl7ArchiveMigration( function(reply_stop) {
		if (reply_stop) {
			$j("#archive_migration_stop_status").html(reply_stop);
			$j("#archive_migration_stop_status").show();
			$j("#status_span").hide();
			$j("#archive_migration_status").hide();
			$j("#archive_migration_progress_img").hide();
			$j("#stopButton").hide();
			// wait for a few seconds to enable the start button to ensure
			// that user doesn't click start before migration thread has
			// finalized
			// since it also waits for 2 sec to actually reset the static
			// properties
			window
					.setTimeout(
							"$j(\"#startButton\").attr(\"disabled\", false);",
							time_out);
		}

	});
}// end function stopHl7InArchiveMigration()

// retrieves status info from the server via ajax calls and does the necessary
// html-dom manipulation on the page to give the user feed back.
function getMigrationStatus() {
	DWRHL7Service.getMigrationStatus( function(status_info) {
		if (status_info != null) {
			if (status_info.status != "RUNNING") {
				is_timer_running = 0;
				if ($j("#msg_running").is(':visible'))
					$j("#msg_running").hide();
				if (status_info.status != "NONE") {
					if (status_info.status == "COMPLETED") {
						if (status_info.areAllTransferred == 1) {
							$j("#archive_migration_progress_img").attr("src",
									openmrsContextPath+"/images/checkmark.png");
							$j("#number_span").hide();
							$j("#numberMigrated").hide();							
							$j("#msg_complete_all").show();
						} else {
							$j("#archive_migration_progress_img").attr("src",
									openmrsContextPath+"/images/alert.gif");
							$j("#msg_complete_not_all").show();
						}						
					} else if (status_info.status == "ERROR") {
						$j("#archive_migration_progress_img").attr("src",
								openmrsContextPath+"/images/problem.gif");
						$j("#msg_error").show();
					} else if (status_info.status == "STOPPED") {
						$j("#archive_migration_progress_img").hide();
						$j("#msg_stopped").show();
					}

					$j("#numberMigrated").html(status_info.numberMigrated);
					$j("#stopButton").hide();
				}
			} else {
				$j("#msg_running").show();
				$j("#numberMigrated").html(status_info.numberMigrated);
				// hide any other visible messaged forinstance if there was a
			// restart of the process,
			// most probably the stop message is showing since the user clicked
			// stop
			if ($j("#msg_stopped").is(':visible'))
				$j("#msg_stopped").hide();
			if ($j("#msg_complete_all").is(':visible'))
				$j("#msg_complete_all").hide();
			if ($j("#msg_complete_not_all").is(':visible'))
				$j("#msg_complete_not_all").hide();
			if ($j("#msg_error").is(':visible'))
				$j("#msg_error").hide();
		}

	} else {// either migration isnt running or no connection to server
		ajax_attempts++;
	}
})	;

	// if we aren't done yet, get status info about the migration process
	if (is_timer_running == 1 && ajax_attempts < 3) {
		timer = window.setTimeout("getMigrationStatus()", 300);
	} else {
		if (ajax_attempts >= 3)
			is_timer_running = 0;
		ajax_attempts = 0;
		window.clearTimeout(timer);
		return;
	}

}// end function getMigrationStatus()
