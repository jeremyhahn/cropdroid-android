package com.jeremyhahn.cropdroid.utils

import android.content.Context
import android.content.res.AssetManager
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

class JsonWebToken(context: Context) {

    private val keyfile = "rsa.pub.der"
    private val algorithm = "RSA"
    val context: Context?

    init {
        this.context = context
    }

    fun parse(token: String) : Jws<Claims> {
        val am: AssetManager = context!!.getAssets()
        val jwtIs: InputStream = am.open(keyfile)
        val spec = X509EncodedKeySpec(IOUtils.toByteArray(jwtIs))
        val kf = KeyFactory.getInstance(algorithm)
        val publicKey: PublicKey = kf.generatePublic(spec)
        return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token)
    }
}