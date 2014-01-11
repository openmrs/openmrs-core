require 'spec_helper'

describe 'mysql::server::backup' do

  let(:default_params) {
    { 'backupuser'         => 'testuser',
      'backuppassword'     => 'testpass',
      'backupdir'          => '/tmp',
      'backuprotate'       => '25',
      'delete_before_dump' => true,
    }
  }
  context 'standard conditions' do
    let(:params) { default_params }

    it { should contain_mysql_user('testuser@localhost')}

    it { should contain_mysql_grant('testuser@localhost/*.*').with(
      :privileges => ["SELECT", "RELOAD", "LOCK TABLES", "SHOW VIEW"]
    )}

    it { should contain_cron('mysql-backup').with(
      :command => '/usr/local/sbin/mysqlbackup.sh',
      :ensure  => 'present'
    )}

    it { should contain_file('mysqlbackup.sh').with(
      :path   => '/usr/local/sbin/mysqlbackup.sh',
      :ensure => 'present'
    ) }

    it { should contain_file('mysqlbackupdir').with(
      :path   => '/tmp',
      :ensure => 'directory'
    )}

    it 'should have compression by default' do
      verify_contents(subject, 'mysqlbackup.sh', [
        ' --all-databases | bzcat -zc > ${DIR}/${PREFIX}`date +%Y%m%d-%H%M%S`.sql.bz2',
      ])
    end

    it 'should have 25 days of rotation' do
      # MySQL counts from 0 I guess.
      should contain_file('mysqlbackup.sh').with_content(/.*ROTATE=24.*/)
    end
  end

  context 'with compression disabled' do
    let(:params) do
      { :backupcompress => false }.merge(default_params)
    end

    it { should contain_file('mysqlbackup.sh').with(
      :path   => '/usr/local/sbin/mysqlbackup.sh',
      :ensure => 'present'
    ) }

    it 'should be able to disable compression' do
      verify_contents(subject, 'mysqlbackup.sh', [
        ' --all-databases > ${DIR}/${PREFIX}`date +%Y%m%d-%H%M%S`.sql',
      ])
    end
  end

  context 'with database list specified' do
    let(:params) do
      { :backupdatabases => ['mysql'] }.merge(default_params)
    end

    it { should contain_file('mysqlbackup.sh').with(
      :path   => '/usr/local/sbin/mysqlbackup.sh',
      :ensure => 'present'
    ) }

    it 'should have a backup file for each database' do
      content = catalogue.resource('file','mysqlbackup.sh').send(:parameters)[:content]
      content.should match(' mysql | bzcat -zc \${DIR}\\\${PREFIX}mysql_`date')
#      verify_contents(subject, 'mysqlbackup.sh', [
#        ' mysql | bzcat -zc ${DIR}/${PREFIX}mysql_`date +%Y%m%d-%H%M%S`.sql',
#      ])
    end 
  end
  
  context 'with file per database' do
    let(:params) do
      default_params.merge({ :file_per_database => true })
    end
    
    it 'should loop through backup all databases' do
      verify_contents(subject, 'mysqlbackup.sh', [
        'mysql -s -r -N -e \'SHOW DATABASES\' | while read dbname',
        'do',
        '  mysqldump -u${USER} -p${PASS} --opt --flush-logs --single-transaction \\',
        '    ${dbname} | bzcat -zc > ${DIR}/${PREFIX}${dbname}_`date +%Y%m%d-%H%M%S`.sql.bz2',
        'done',
      ])
    end
    
    context 'with compression disabled' do
      let(:params) do
        default_params.merge({ :file_per_database => true, :backupcompress => false })
      end
      
      it 'should loop through backup all databases without compression' do
        verify_contents(subject, 'mysqlbackup.sh', [
          '    ${dbname} > ${DIR}/${PREFIX}${dbname}_`date +%Y%m%d-%H%M%S`.sql',
        ])
      end
    end
  end
end
