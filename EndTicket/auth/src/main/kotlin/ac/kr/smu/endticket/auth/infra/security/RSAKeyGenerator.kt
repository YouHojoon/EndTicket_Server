//package ac.kr.smu.endTicket.auth.infra.security
//
//import com.nimbusds.jose.jwk.RSAKey
//import com.nimbusds.jose.util.X509CertUtils
//import org.bouncycastle.asn1.x500.X500Name
//import org.springframework.boot.autoconfigure.ssl.JksSslBundleProperties
//import java.io.File
//import java.io.FileOutputStream
//import java.math.BigInteger
//import java.security.KeyPairGenerator
//import java.security.KeyStore
//import java.security.SecureRandom
//import java.security.cert.Certificate
//import java.security.cert.X509Certificate
//import java.security.interfaces.RSAPrivateKey
//import java.security.interfaces.RSAPublicKey
//import java.util.*
//
//class RSAKeyGenerator(
//    private val properties: JksSslBundleProperties,
//) {
//    fun loadOrGenerateRSAKey(): RSAKey {
//        val file = File(properties.keystore.location)
//
//        if (file.exists()) {
//            val keyStore = KeyStore.getInstance(file, properties.keystore.password.toCharArray())
//            return RSAKey.load(keyStore, properties.key.alias,properties.key.password.toCharArray())
//        }
//
//        return generateRSAKey().also { saveRSAKey(it) }
//    }
//
//    private fun generateRSAKey(): RSAKey =
//        try {
//            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
//            keyPairGenerator.initialize(2048)
//            val keyPair = keyPairGenerator.generateKeyPair()
//            val publicKey = keyPair.public as RSAPublicKey
//            val privateKey = keyPair.private as RSAPrivateKey
//
//            RSAKey
//                .Builder(publicKey)
//                .privateKey(privateKey)
//                .keyID(properties.key.alias)
//                .build()
//
//        } catch (ex: Exception) {
//            throw IllegalStateException(ex)
//        }
//
//    private fun saveRSAKey(pair: RSAKey) {
//        // 키스토어 객체 생성
//        val keyStore = KeyStore.getInstance(properties.keystore.provider)
//        val cert = generateCertificate(pair)
//
//        keyStore.load(null, null) // 초기화
//        keyStore
//            .setKeyEntry(
//                properties.key.alias,
//                pair.toRSAPrivateKey(),
//                properties.key.password.toCharArray(),
//                arrayOf<Certificate>(cert),
//            )
//
//        FileOutputStream(properties.keystore.location).use { fos ->
//            keyStore.store(fos, properties.keystore.password.toCharArray())
//        }
//    }
//
//    private fun generateCertificate(rsaKey: RSAKey): X509Certificate {
//        val startDate = Date()
//        val owner = X500Name("CN=EndTicket, L=Seoul, C=KR")
//        val serial = BigInteger(64, SecureRandom())
//        val endDate = Date(startDate.time + 365 * 86400000L)
//        val info = X509CertInfo()
//
//        info.validity = CertificateValidity(startDate, endDate)
//        info.serialNumber = CertificateSerialNumber(serial)
//        info.subject = owner
//        info.issuer = owner
//        info.key = CertificateX509Key(rsaKey.toRSAPublicKey())
//        info.version = CertificateVersion(CertificateVersion.V3)
//
//        var algo = AlgorithmId(AlgorithmId.SHA256withRSA_oid)
//        info.algorithmId = CertificateAlgorithmId(algo)
//
//        return X509CertImpl.newSigned(info, rsaKey.toRSAPrivateKey(), algo.name)
//    }
//}
