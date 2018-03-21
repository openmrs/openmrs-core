use File::Find;

#message utility functon

#load command-line parameters
if ($ARGV[0] =~ /^-/) {
    ($param, $MESSAGEFILE, $TARGET, $OUTPUTFILE, $DELETEFILE ) = @ARGV;
}
else {
    ($MESSAGEFILE, $TARGET, $OUTPUTFILE) = @ARGV;
    $param = "-s";  # default
}

#define global variables
our $code;
our $found = 0;

# first we need to load all lines of the message file into a list
open MESSAGEFILE or die "Unable to open $MESSAGEFILE\n";
while(<MESSAGEFILE>) {
    chomp;
    push @messages, $_;
}
close MESSAGEFILE;

# now do the actual work, depending on the parameter that has been set
if ($param =~ /-s/) {
    push @directories, $TARGET;


#    print "@directories" ;
#    open (TARGET, ">$TARGET") or die "Unable to open $TARGET\n";
#    print TARGET "\n<!--NOT FOUND-->\n\n";
#    close TARGET;

    searchForMessages();
    
}
elsif ($param =~ /-c/) {
    loadTargetMessages();
    compareMessages();
}
else {
    die "Invalid paramater.  Valid values are -c and -s.\n";
}


# now write out the output file
open (OUTPUTFILE, ">$OUTPUTFILE") or die "Unable to open $OUTPUTFILE\n";

# write out the output list first
foreach (@output) {
    print OUTPUTFILE "$_\n";
}

# add a line to delineate found message from not found messages
print OUTPUTFILE "\n<!--NOT FOUND-->\n\n";

# print out the codes that were not found
foreach (@notfound) {
    print OUTPUTFILE "$_\n";
}
close OUTPUTFILE;

open (DELETEFILE, ">$DELETEFILE") or die "Unable to open $DELETEFILE\n";
print DELETEFILE "\n<!--SHOULD DELETE-->\n\n";

##DILLEPC : Iterate the deleted hashmap and put them to list
foreach $code (keys %targetMessages) {
        push @shouddelete, "$code=$targetMessages{$code}";
}
##DILLEPC : print delete list
foreach (@shouddelete) {
    print DELETEFILE "$_\n";
}

close DELETEFILE;



#
# SUBROUTINUES
#

sub searchForMessages {
    # iterate through each message
    foreach (@messages) {
	$found = 0;  # reset the found flag to false
    
	# test if this is an actual message assignment
	if (/=/) {
	            @splitResult = split /\s*=\s*/;
        		$counter = 1;
        		$message = "";
        		$size = @splitResult;
        		foreach $result (@splitResult) {
        			if ($counter == 1)
        			{
        				$code = @splitResult[0];
        			}
        			elsif ($counter == $size)
        			{
        				$message = "$message$result";
        			}
        			else
        			{
        				$message = "$message$result=";
        			}
        			$counter  = $counter  +1;
        		}
	
	    # perform the actual search
	    find(\&search, @directories);

	    # push the message assignment onto the not found list
	    # or output list as appopriate
	    if (!$found) {
		push @notfound, $_;
	    }
	    else {
		push @output, $_;
	    }
	}
	elsif (!/<!--NOT FOUND-->/){
	    # just push white space/comments onto the output list
	    # (but skip any "NOT FOUND" line)
	    push @output, $_;
	}
    }
}


sub search {
    # we want to ignore:
    #  1) directories, 
    #  2) .properties files 
    #  3) files that start with . 
    #  4) any files in directories that start with .
    if (-f $File::Find::name 
	&& !($File::Find::name =~ /\/\./)
	&& !($File::Find::name =~ /\.properties/) ) {

	$SEARCHFILE = $File::Find::name;
    print  "$SEARCHFILE";
	open SEARCHFILE or die "Cannot open file $File::Find::name\n";
 
	while(<SEARCHFILE>) {
	    chomp;
	    if (/\Q$code/) {
		$found = 1;
	    }
	}
    } 
}

sub compareMessages {
    # iterate through each line in the source file
    foreach (@messages) {

	# test if this is a actual message assignment
	if (/=/) {
	            @splitResult = split /\s*=\s*/;
        		$counter = 1;
        		$message = "";
        		$size = @splitResult;
        		foreach $result (@splitResult) {
        			if ($counter == 1)
        			{
        				$code = @splitResult[0];
        			}
        			elsif ($counter == $size)
        			{
        				$message = "$message$result";
        			}
        			else
        			{

        				$message = "$message$result=";
        			}
        			$counter  = $counter  +1;
        		}

	    # now test to see if this code exists in the target file
	    if (exists $targetMessages{$code}) {
		# replace with the localized message and push onto the output list
		push @output, "$code=$targetMessages{$code}";
		delete $targetMessages{$code}; #DILLEPAC : if code is existing, it will delete from targertMessage Map. after iteration, Finally remain only delete code.
	    }
	    else {
		#push onto the "notfound" list as is
		push @notfound, $_;
	    }
	}
	else {
	    # just push whitespace/comments directly on to the output list
	    push @output, $_;
	}
    }
}


sub loadTargetMessages {
    # first we want to load all the codes from the *target* file into a hash
    open TARGET or die "Unable to open target $TARGET\n";

    # load all the existing mappings in the main message properties file into hash
    while (<TARGET>) {
	chomp;
	
	# skip any messages previously listed as "not found" 
	# (note that this skips *all* messages after the "not found" comment)
	if (/<!--NOT FOUND-->/) {
	    return;
	}

	if (/=/) {
	            @splitResult = split /\s*=\s*/;
        		$counter = 1;
        		$message = "";
        		$size = @splitResult;
        		foreach $result (@splitResult) {
        			if ($counter == 1)
        			{
        				$code = @splitResult[0];
        			}
        			elsif ($counter == $size)
        			{
        				$message = "$message$result";
        			}
        			else
        			{

        				$message = "$message$result=";
        			}
        			$counter  = $counter  +1;
        		}
	    $targetMessages{$code} = $message;
	}
    }
}


