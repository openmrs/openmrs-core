#MySQL

####Table of Contents

1. [Overview](#overview)
2. [Module Description - What the module does and why it is useful](#module-description)
3. [Setup - The basics of getting started with mysql](#setup)
    * [What mysql affects](#what-mysql-affects)
    * [Setup requirements](#setup-requirements)
    * [Beginning with mysql](#beginning-with-mysql)
4. [Usage - Configuration options and additional functionality](#usage)
5. [Reference - An under-the-hood peek at what the module is doing and how](#reference)
5. [Limitations - OS compatibility, etc.](#limitations)
6. [Development - Guide for contributing to the module](#development)

##Overview

The MySQL module installs, configures, and manages the MySQL service.

##Module Description

The MySQL module manages both the installation and configuration of MySQL as
well as extends Pupppet to allow management of MySQL resources, such as
databases, users, and grants.

##Backwards Compatibility

This module has just undergone a very large rewrite.  As a result it will no
longer work with the previous classes and configuration as before.  We've
attempted to handle backwards compatibility automatically by adding a
`attempt_compatibility_mode` parameter to the main mysql class.  If you set
this to true it will attempt to map your previous parameters into the new
`mysql::server` class.

###WARNING

This may fail.  It may eat your MySQL server.  PLEASE test it before running it
live.  Even if it's just a no-op and a manual comparision.  Please be careful!

##Setup

###What MySQL affects

* MySQL package.
* MySQL configuration files.
* MySQL service.

###Beginning with MySQL

If you just want a server installing with the default options you can run
`include '::mysql::server'`.  If you need to customize options, such as the root
password or /etc/my.cnf settings then you can also include `mysql::server` and
pass in an override hash as seen below:

```puppet
class { '::mysql::server':
  override_options => { 'mysqld' => { 'max_connections' => '1024' } }
}
```

##Usage

All interaction for the server is done via `mysql::server`.  To install the
client you use `mysql::client`, and to install bindings you can use
`mysql::bindings`.

###Overrides

The hash structure for overrides in `mysql::server` is as follows:

```puppet
override_options = {
  'section' => {
    'item'             => 'thing',
  }
}
```

For items that you would traditionally represent as:

<pre>
[section]
thing
</pre>

You can just make an entry like `thing => true` in the hash.  MySQL doesn't
care if thing is alone or set to a value, it'll happily accept both.

###Custom configuration

To add custom mysql configuration you can drop additional files into
`/etc/mysql/conf.d/` in order to override settings or add additional ones (if you
choose not to use override_options in `mysql::server`).  This location is
hardcoded into the my.cnf template file.

##Reference

###Classes

####Public classes
* `mysql::server`: Installs and configures MySQL.
* `mysql::server::account_security`: Deletes default MySQL accounts.
* `mysql::server::monitor`: Sets up a monitoring user.
* `mysql::server::mysqltuner`: Installs MySQL tuner script.
* `mysql::server::backup`: Sets up MySQL backups via cron.
* `mysql::bindings`: Installs various MySQL language bindings.
* `mysql::client`: Installs MySQL client (for non-servers).

####Private classes
* `mysql::server::install`: Installs packages.
* `mysql::server::config`: Configures MYSQL.
* `mysql::server::service`: Manages service.
* `mysql::server::root_password`: Sets MySQL root password.
* `mysql::bindings::java`: Installs Java bindings.
* `mysql::bindings::perl`: Installs Perl bindings.
* `mysql::bindings::python`: Installs Python bindings.
* `mysql::bindings::ruby`: Installs Ruby bindings.
* `mysql::client::install`:  Installs MySQL client.

###Parameters

####mysql::server

#####`root_password`

What is the MySQL root password.  Puppet will attempt to set it to this and update `/root/.my.cnf`.

#####`old_root_password`

What was the previous root password (REQUIRED if you wish to change the root password via Puppet.)

#####`override_options`

This is the hash of override options to pass into MySQL.  It can be visualized
like a hash of the my.cnf file, so that entries look like:

```puppet
override_options = {
  'section' => {
    'item'             => 'thing',
  }
}
```

For items that you would traditionally represent as:

<pre>
[section]
thing
</pre>

You can just make an entry like `thing => true` in the hash.  MySQL doesn't
care if thing is alone or set to a value, it'll happily accept both.

#####`config_file`

The location of the MySQL configuration file.

#####`manage_config_file`

Should we manage the MySQL configuration file.

#####`purge_conf_dir`

Should we purge the conf.d directory?

#####`restart`

Should the service be restarted when things change?

#####`root_group`

What is the group used for root?

#####`package_ensure`

What to set the package to.  Can be present, absent, or version.

#####`package_name`

What is the name of the mysql server package to install.

#####`remove_default_accounts`

Boolean to decide if we should automatically include
`mysql::server::account_security`.

#####`service_enabled`

Boolean to decide if the service should be enabled.

#####`service_manage`

Boolean to decide if the service should be managed.

#####`service_name`

What is the name of the mysql server service.

#####`service_provider`

Which provider to use to manage the service.

####mysql::server::backup

#####`backupuser`

MySQL user to create for backing up.

#####`backuppassword`

MySQL user password for backups.

#####`backupdir`

Directory to backup into.

#####`backupcompress`

Boolean to determine if backups should be compressed.

#####`backuprotate`

How many days to keep backups for.

#####`delete_before_dump`

Boolean to determine if you should cleanup before backing up or after.

#####`backupdatabases`

Array of databases to specifically backup.

#####`file_per_database`

Should a seperate file be used per database.

#####`ensure`

Present or absent, allows you to remove the backup scripts.

#####`time`

An array of two elements to set the time to backup.  Allows ['23', '5'] or ['3', '45'] for HH:MM times.

####mysql::server::monitor

#####`mysql_monitor_username`

The username to create for MySQL monitoring.

#####`mysql_monitor_password`

The password to create for MySQL monitoring.

#####`mysql_monitor_hostname`

The hostname to allow to access the MySQL monitoring user.

####mysql::bindings

#####`java_enable`

Boolean to decide if the Java bindings should be installed.

#####`perl_enable`

Boolean to decide if the Perl bindings should be installed.

#####`php_enable`

Boolean to decide if the PHP bindings should be installed.

#####`python_enable`

Boolean to decide if the Python bindings should be installed.

#####`ruby_enable`

Boolean to decide if the Ruby bindings should be installed.

#####`java_package_ensure`

What to set the package to.  Can be present, absent, or version.

#####`java_package_name`

The name of the package to install.

#####`java_package_provider`

What provider should be used to install the package.

#####`perl_package_ensure`

What to set the package to.  Can be present, absent, or version.

#####`perl_package_name`

The name of the package to install.

#####`perl_package_provider`

What provider should be used to install the package.

#####`python_package_ensure`

What to set the package to.  Can be present, absent, or version.

#####`python_package_name`

The name of the package to install.

#####`python_package_provider`

What provider should be used to install the package.

#####`ruby_package_ensure`

What to set the package to.  Can be present, absent, or version.

#####`ruby_package_name`

The name of the package to install.

#####`ruby_package_provider`

What provider should be used to install the package.

####mysql::client

#####`bindings_enable`

Boolean to automatically install all bindings.

#####`package_ensure`

What to set the package to.  Can be present, absent, or version.

#####`package_name`

What is the name of the mysql client package to install.

###Defines

####mysql::db

Creates a database with a user and assign some privileges.

```puppet
    mysql::db { 'mydb':
      user     => 'myuser',
      password => 'mypass',
      host     => 'localhost',
      grant    => ['SELECT', 'UPDATE'],
    }
```

###Providers

####mysql_database

mysql_database can be used to create and manage databases within MySQL:

```puppet
mysql_database { 'information_schema':
  ensure  => 'present',
  charset => 'utf8',
  collate => 'utf8_swedish_ci',
}
mysql_database { 'mysql':
  ensure  => 'present',
  charset => 'latin1',
  collate => 'latin1_swedish_ci',
}
```

####mysql_user

mysql_user can be used to create and manage user grants within MySQL:

```puppet
mysql_user { 'root@127.0.0.1':
  ensure                   => 'present',
  max_connections_per_hour => '0',
  max_queries_per_hour     => '0',
  max_updates_per_hour     => '0',
  max_user_connections     => '0',
}
```

####mysql_grant

mysql_grant can be used to create grant permissions to access databases within
MySQL.  To use it you must create the title of the resource as shown below,
following the pattern of `username@hostname/database.table`:

```puppet
mysql_grant { 'root@localhost/*.*':
  ensure     => 'present',
  options    => ['GRANT'],
  privileges => ['ALL'],
  table      => '*.*',
  user       => 'root@localhost',
}
```

##Limitations

This module has been tested on:

* RedHat Enterprise Linux 5/6
* Debian 6/7
* CentOS 5/6
* Ubuntu 12.04

Testing on other platforms has been light and cannot be guaranteed.

#Development

Puppet Labs modules on the Puppet Forge are open projects, and community
contributions are essential for keeping them great. We can’t access the
huge number of platforms and myriad of hardware, software, and deployment
configurations that Puppet is intended to serve.

We want to keep it as easy as possible to contribute changes so that our
modules work in your environment. There are a few guidelines that we need
contributors to follow so that we can have a chance of keeping on top of things.

You can read the complete module contribution guide [on the Puppet Labs wiki.](http://projects.puppetlabs.com/projects/module-site/wiki/Module_contributing)

### Authors

This module is based on work by David Schmitt. The following contributor have contributed patches to this module (beyond Puppet Labs):

* Larry Ludwig
* Christian G. Warden
* Daniel Black
* Justin Ellison
* Lowe Schmidt
* Matthias Pigulla
* William Van Hevelingen
* Michael Arnold
* Chris Weyl

