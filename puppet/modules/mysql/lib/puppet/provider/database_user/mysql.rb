Puppet::Type.type(:database_user).provide(:mysql) do

  desc 'manage users for a mysql database.'

  defaultfor :kernel => 'Linux'

  commands :mysql      => 'mysql'
  commands :mysqladmin => 'mysqladmin'

  def self.instances
    users = mysql([defaults_file, 'mysql', '-BNe' "select concat(User, '@',Host) as User from mysql.user"].compact).split("\n")
    users.select{ |user| user =~ /.+@/ }.collect do |name|
      new(:name => name)
    end
  end

  def create
    merged_name          = @resource[:name].sub('@', "'@'")
    password_hash        = @resource.value(:password_hash)
    max_user_connections = @resource.value(:max_user_connections) || 0

    mysql([defaults_file, 'mysql', '-e', "grant usage on *.* to '#{merged_name}' identified by PASSWORD
      '#{password_hash}' with max_user_connections #{max_user_connections}"].compact)

    exists? ? (return true) : (return false)
  end

  def destroy
    merged_name   = @resource[:name].sub('@', "'@'")
    mysql([defaults_file, 'mysql', '-e', "drop user '#{merged_name}'"].compact)

    exists? ? (return false) : (return true)
  end

  def password_hash
    mysql([defaults_file, 'mysql', '-NBe', "select password from mysql.user where CONCAT(user, '@', host) = '#{@resource[:name]}'"].compact).chomp
  end

  def password_hash=(string)
    mysql([defaults_file, 'mysql', '-e', "SET PASSWORD FOR '%s' = '%s'" % [ @resource[:name].sub('@', "'@'"), string ] ].compact)

    password_hash == string ? (return true) : (return false)
  end

  def max_user_connections
    mysql([defaults_file, "mysql", "-NBe", "select max_user_connections from mysql.user where CONCAT(user, '@', host) = '#{@resource[:name]}'"].compact).chomp
  end

  def max_user_connections=(int)
    mysql([defaults_file, "mysql", "-e", "grant usage on *.* to '%s' with max_user_connections #{int}" % [ @resource[:name].sub("@", "'@'")] ].compact).chomp

    max_user_connections == int ? (return true) : (return false)
  end

  def exists?
    not mysql([defaults_file, 'mysql', '-NBe', "select '1' from mysql.user where CONCAT(user, '@', host) = '%s'" % @resource.value(:name)].compact).empty?
  end

  def flush
    @property_hash.clear
    mysqladmin([defaults_file, 'flush-privileges'].compact)
  end

  # Optional defaults file
  def self.defaults_file
    if File.file?("#{Facter.value(:root_home)}/.my.cnf")
      "--defaults-file=#{Facter.value(:root_home)}/.my.cnf"
    else
      nil
    end
  end
  def defaults_file
    self.class.defaults_file
  end

end
