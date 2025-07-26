package com.dcac.realestatemanager.utils

import java.security.MessageDigest

// HASHES A PLAINTEXT PASSWORD USING THE SHA-256 ALGORITHM
fun hashPassword(password: String): String {
    // GET AN INSTANCE OF THE SHA-256 MESSAGE DIGEST ALGORITHM
    val digest = MessageDigest.getInstance("SHA-256")

    // CONVERT THE INPUT PASSWORD STRING TO A BYTE ARRAY AND COMPUTE THE HASH
    val hashBytes = digest.digest(password.toByteArray())

    // CONVERT THE HASHED BYTE ARRAY INTO A HEXADECIMAL STRING
    // "%02x".format(it) ➝ ensures each byte is converted to a two-character hex string (with leading 0 if necessary)
    return hashBytes.joinToString("") { "%02x".format(it) }
}
