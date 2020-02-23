package com.jeremyhahn.cropdroid.utils

import android.content.Context
import android.util.Log
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import org.bouncycastle.util.io.pem.PemReader
import java.security.KeyFactory
import java.security.spec.EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

// https://stackoverflow.com/questions/45684632/sign-jwt-with-privatekey-from-android-fingerprint-api
class JsonWebToken(context: Context) {

    private val keyfile = "rsa.pub"
    private val algorithm = "RSA"
    val context: Context?

    init {
        this.context = context
    }

    fun parse(token: String) : Jws<Claims> {

        var reader = PemReader(context!!.getAssets().open(keyfile).bufferedReader())
        val pemObject = reader.readPemObject()
        val content: ByteArray = pemObject.getContent()
        reader.close()

        val kf = KeyFactory.getInstance(algorithm)
        val keySpec: EncodedKeySpec = X509EncodedKeySpec(content)
        var publicKey = kf.generatePublic(keySpec)

        var claims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token)
        Log.d("JsonWebToken", claims.toString())

        return claims
    }
}