class apt_get::update {

    exec { "apt-get update" :
        path    => "/usr/bin"
    }
}
