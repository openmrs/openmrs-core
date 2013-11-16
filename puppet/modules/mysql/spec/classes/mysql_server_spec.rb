require 'spec_helper'
describe 'mysql::server' do
  let(:facts) {{:osfamily => 'RedHat', :root_home => '/root'}}

  context 'with defaults' do
    it { should contain_class('mysql::server::install') }
    it { should contain_class('mysql::server::config') }
    it { should contain_class('mysql::server::service') }
    it { should contain_class('mysql::server::root_password') }
  end

  # make sure that overriding the mysqld settings keeps the defaults for everything else
  context 'with overrides' do
    let(:params) {{ :override_options => { 'mysqld' => { 'socket' => '/var/lib/mysql/mysql.sock' } } }}
    it do
      should contain_file('/etc/my.cnf').with({
        :mode => '0644',
      }).with_content(/basedir/)
    end
  end

  context 'with remove_default_accounts set' do
    let (:params) {{ :remove_default_accounts => true }}
    it { should contain_class('mysql::server::account_security') }
  end

  context 'mysql::server::install' do
    let(:params) {{ :package_ensure => 'present', :name => 'mysql-server' }}
    it do
      should contain_package('mysql-server').with({
      :ensure => :present,
      :name   => 'mysql-server',
    })
    end
  end

  context 'mysql::server::config' do
    it do
      should contain_file('/etc/mysql').with({
        :ensure => :directory,
        :mode   => '0755',
      })
    end

    it do
      should contain_file('/etc/mysql/conf.d').with({
        :ensure => :directory,
        :mode   => '0755',
      })
    end

    it do
      should contain_file('/etc/my.cnf').with({
        :mode => '0644',
      })
    end
  end

  context 'mysql::server::service' do
    context 'with defaults' do
      it { should contain_service('mysqld') }
    end

    context 'service_enabled set to false' do
      let(:params) {{ :service_enabled => false }}

      it do
        should contain_service('mysqld').with({
          :ensure => :stopped
        })
      end
    end
  end

  context 'mysql::server::root_password' do
    describe 'when defaults' do
      it { should_not contain_mysql_user('root@localhost') }
      it { should_not contain_file('/root/.my.cnf') }
    end
    describe 'when set' do
      let(:params) {{:root_password => 'SET' }}
      it { should contain_mysql_user('root@localhost') }
      it { should contain_file('/root/.my.cnf') }
    end

  end

end
