class xvfb {
  package { "xvfb":
    ensure => "installed",
  }

  file { "/etc/init.d/xvfb":
    source => "puppet:///modules/xvfb/xvfb.init",
    mode => 755,
  }

  service { "xvfb":
    ensure => running,
    enable => true,
    hasstatus => true,
    hasrestart => true,
  }

  Package["xvfb"] -> File["/etc/init.d/xvfb"] -> Service["xvfb"]
}
