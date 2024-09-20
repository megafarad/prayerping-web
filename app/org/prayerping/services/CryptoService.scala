package org.prayerping.services

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.io.pem.{ PemObject, PemWriter }
import play.api.Configuration

import java.io.StringWriter
import java.security.spec.KeySpec
import java.security.{ Key, KeyPair, KeyPairGenerator, PrivateKey, PublicKey, SecureRandom, Security }
import java.util.Base64
import javax.crypto.spec.{ PBEKeySpec, SecretKeySpec }
import javax.crypto.{ Cipher, SecretKeyFactory }
import javax.inject.{ Inject, Singleton }

@Singleton
class CryptoService @Inject() (configuration: Configuration) {

  Security.addProvider(new BouncyCastleProvider())

  def generateKeyPair(salt: Array[Byte]): (String, String) = {
    val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC")
    keyGen.initialize(2048)

    val keyPair: KeyPair = keyGen.generateKeyPair()

    val publicKey = keyPair.getPublic
    val privateKey = keyPair.getPrivate

    val publicKeyPem = convertToPem(publicKey)
    val privateKeyPem = convertToPem(privateKey)

    (publicKeyPem, encrypt(privateKeyPem, generateSecretKey(salt)))

  }

  private def convertToPem(key: Key): String = {
    val stringWriter = new StringWriter()
    val pemWriter = new PemWriter(stringWriter)
    val pemObjectType = key match {
      case _: PrivateKey => "PRIVATE KEY"
      case _ => "PUBLIC KEY"
    }
    pemWriter.writeObject(new PemObject(pemObjectType, key.getEncoded))
    pemWriter.close()
    stringWriter.toString
  }

  def generateSecretKey(salt: Array[Byte]): SecretKeySpec = {
    val secret = configuration.get[String]("play.http.secret.key")
    val spec: KeySpec = new PBEKeySpec(secret.toCharArray, salt, 65536, 256) // 256-bit AES key
    val f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")

    new SecretKeySpec(f.generateSecret(spec).getEncoded, "AES")
  }

  def generateSalt: Array[Byte] = new SecureRandom().generateSeed(16)

  def encrypt(data: String, secretKey: SecretKeySpec): String = {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val encrypted = cipher.doFinal(data.getBytes("UTF-8"))
    Base64.getEncoder.encodeToString(encrypted)
  }

  def decrypt(encryptedData: String, secretKey: SecretKeySpec): String = {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    val decodedData = Base64.getDecoder.decode(encryptedData)
    new String(cipher.doFinal(decodedData), "UTF-8")
  }

}
