class maven {
  package { [
      'maven'
    ]:
    ensure => 'installed',
  }
}
