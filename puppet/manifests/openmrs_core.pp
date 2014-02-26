class openmrs_core($username = "vagrant", $setup_ssh = "true") {
  $home = "/home/${username}"

  Exec { path => "/usr/bin:/bin:/usr/sbin:/sbin" }

  group { "puppet":
    ensure => "present",
  }

  # --- Apt-get update ---------------------------------------------------------

  exec { 'apt-update':
    command => "/usr/bin/apt-get update",
    onlyif => "/bin/bash -c 'exit $(( $(( $(date +%s) - $(stat -c %Y /var/lib/apt/lists/$( ls /var/lib/apt/lists/ -tr1|tail -1 )) )) <= 604800 ))'"
  }

  Exec["apt-update"] -> Package <| |>

  # --- MySQL ---------------------------------------------------------------------

  class { 'mysql::server': }

  package { "libmysqlclient-dev":
    ensure => installed
  }

  # --- Packages -----------------------------------------------------------------

  package { 'curl':
    ensure => installed
  }

  package { 'build-essential':
    ensure => installed
  }

  package { 'vim':
    ensure => installed
  }

  # --- SSH ---------------------------------------------------------------------

  if $setup_ssh == "true" {
    file { "${home}/.ssh":
      ensure => directory,
    }

    file { "${home}/.ssh/id_rsa":
      ensure => present,
      mode => 600,
      source => "/tmp/vagrant-puppet/modules-1/id_rsa",
      require => File["${home}/.ssh"]
    }

    file { "${home}/.ssh/id_rsa.pub":
      ensure => present,
      mode => 622,
      source => "/tmp/vagrant-puppet/modules-1/id_rsa.pub",
      require => File["${home}/.ssh"]
    }

    file { "${home}/.bashrc":
      ensure => present,
      mode => 777,
      content => template(".bashrc.erb")
    }

    file { "${home}/.gitconfig":
      ensure => present,
      mode => 777,
      content => template(".gitconfig.erb")
    }
  }
}
