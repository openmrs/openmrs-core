#
class mysql::client (
  $bindings_enable = $mysql::params::bindings_enable,
  $package_ensure  = $mysql::params::client_package_ensure,
  $package_name    = $mysql::params::client_package_name,
) inherits mysql::params {

  include '::mysql::client::install'

  if $bindings_enable {
    class { 'mysql::bindings':
      java_enable   => true,
      perl_enable   => true,
      php_enable    => true,
      python_enable => true,
      ruby_enable   => true,
    }
  }

}
