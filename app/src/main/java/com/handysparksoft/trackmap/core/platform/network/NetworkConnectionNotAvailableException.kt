package com.handysparksoft.trackmap.core.platform.network

import java.io.IOException

class NetworkConnectionNotAvailableException(message: String = "No network connection available!") :
    IOException(message)
