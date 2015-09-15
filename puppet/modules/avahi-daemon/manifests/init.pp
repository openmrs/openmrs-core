class avahi-daemon {
    package { 'avahi-daemon':
        ensure => present,
    }
    
    file {'avahi-daemon.conf':
        path => '/etc/avahi/avahi-daemon.conf',
        ensure => file,
        owner => root,
        group => root,
        source => 'puppet:///modules/avahi-daemon/avahi-daemon.conf',
    }
}



