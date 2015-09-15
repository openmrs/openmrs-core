class bower {
	package { 'bower':
	  ensure   => present,
	  provider => 'npm',
	}
}