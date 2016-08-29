class mysql {
  $password = "OpenMRS"
  package { "mysql-client": ensure => installed }
  package { "mysql-server": ensure => installed }
  package { "libmysqlclient-dev": ensure => installed }

  exec { "Set MySQL server root password":
    subscribe => [ Package["mysql-server"], Package["mysql-client"], Package["libmysqlclient-dev"] ],
    refreshonly => true,
    unless => "mysqladmin -uroot -p$password status",
    path => "/bin:/usr/bin",
    command => "mysqladmin -uroot password $password",
  }
}
