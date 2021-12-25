package com.jeremyhahn.cropdroid.utils

import android.content.Context
import android.util.Log
import com.jeremyhahn.cropdroid.config.FarmParser
import com.jeremyhahn.cropdroid.config.OrganizationParser
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.model.Farm
import com.jeremyhahn.cropdroid.model.Organization
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.bouncycastle.util.io.pem.PemReader
import java.io.StringReader
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class JsonWebToken(context: Context, connection: Connection) {

    private val algorithm = "RSA"
    private val token: String
    private val publicKey: PublicKey
    var claims: Claims? = null

    init {
        val pubkey = connection.pubkey.replace("\\n", "\n")
            .replace("\"", "").trim()

        token = connection.token

        var reader = PemReader(StringReader(pubkey))
        val pemObject = reader.readPemObject()
        val content: ByteArray = pemObject.getContent()
        reader.close()

        val kf = KeyFactory.getInstance(algorithm)
        val keySpec: EncodedKeySpec = X509EncodedKeySpec(content)
        publicKey = kf.generatePublic(keySpec)
    }

    fun parse() {
        claims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).body
        Log.d("JsonWebToken", claims.toString())
    }

    fun sid(): Long {
        return claims!!.get("sid", java.lang.Long::class.java).toLong()
    }

    fun uid(): Long {
        return claims!!.get("uid", java.lang.Long::class.java).toLong()
    }

    fun email(): String {
        return claims!!.get("email", String::class.java)
    }

    fun organizations(): List<Organization> {
        return OrganizationParser.parse(claims!!.get("organizations", String::class.java).toString(), true)
    }

    fun farms(): List<Farm> {
        return FarmParser.parse(claims!!.get("farms", String::class.java), 0, true)
    }

    fun exp(): Int {
        return claims!!.get("exp", Integer::class.java).toInt()
    }

    fun iat(): Int {
        return claims!!.get("iat", Integer::class.java).toInt()
    }

    fun iss(): String {
        return claims!!.get("iss", String::class.java)
    }

    override fun toString() : String {
        return if(claims != null) {
            claims!!.toString()
        } else {
            ""
        }
    }
}