class git::package {
    package { 'git':
        ensure => latest,
        require => Exec['apt-get update'],
    }
}

class git {
    include git::package
}
