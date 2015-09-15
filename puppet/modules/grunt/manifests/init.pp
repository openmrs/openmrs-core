class grunt {
	package { 'grunt-cli':
	  ensure   => present,
	  provider => 'npm',
	}
}