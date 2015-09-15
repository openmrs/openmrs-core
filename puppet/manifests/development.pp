stage { 'first':
    before => Stage['second'],
}

stage { 'second':
    before => Stage['main']
}

class { "nodejs":
  stage => second
}

class { "apt_get::update":
    stage  => first,
}

class { "jdk":
    stage => second,
}

include apt
include apt_get::update
include jdk
include maven
include git
include vim
include mysql
include avahi-daemon
include nodejs
include grunt
include bower
include firefox
include xvfb
