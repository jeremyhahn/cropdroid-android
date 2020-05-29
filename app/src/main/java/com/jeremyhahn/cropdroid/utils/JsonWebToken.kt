package com.jeremyhahn.cropdroid.utils

import android.content.Context
import android.util.Log
import com.jeremyhahn.cropdroid.model.Organization
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.bouncycastle.util.io.pem.PemReader
import java.security.KeyFactory
import java.security.spec.EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

// https://stackoverflow.com/questions/45684632/sign-jwt-with-privatekey-from-android-fingerprint-api
class JsonWebToken(context: Context, token: String) {

    private val keyfile = "rsa.pub"
    private val algorithm = "RSA"
    val claims: Claims

    init {
        var reader = PemReader(context!!.getAssets().open(keyfile).bufferedReader())
        val pemObject = reader.readPemObject()
        val content: ByteArray = pemObject.getContent()
        reader.close()

        val kf = KeyFactory.getInstance(algorithm)
        val keySpec: EncodedKeySpec = X509EncodedKeySpec(content)
        var publicKey = kf.generatePublic(keySpec)

        claims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).body
        Log.d("JsonWebToken", claims.toString())
    }

    fun sid(): Long {
        return claims.get("sid", java.lang.Long::class.java).toLong()
    }

    fun uid(): Long {
        return claims.get("uid", java.lang.Long::class.java).toLong()
    }

    fun email(): String {
        return claims.get("email", String::class.java)
    }

    fun organizations(): List<Organization> {
        return OrganizationParser.parse(claims.get("organizations", String::class.java).toString(), true)
        /*
        val orgs = claims.get("organizations", List::class.java)
        Log.d("organizations:", orgs.toString())
        return ArrayList<Organization>(0)
         */
    }

    fun exp(): Int {
        return claims.get("exp", Integer::class.java).toInt()
    }

    fun iat(): Int {
        return claims.get("iat", Integer::class.java).toInt()
    }

    fun iss(): String {
        return claims.get("iss", String::class.java)
    }

    override fun toString() : String {
        return claims.toString()
    }
}