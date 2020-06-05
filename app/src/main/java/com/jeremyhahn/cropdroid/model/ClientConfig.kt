package com.jeremyhahn.cropdroid.model

import com.jeremyhahn.cropdroid.utils.JsonWebToken

data class ClientConfig(var hostname: String, var secure: Int, var token: String, var jwt: JsonWebToken?) {

    override fun toString() : String {
        return "ClientConfig[hostname=$hostname secure=$secure token=$token jwt=$jwt]"
    }
}
