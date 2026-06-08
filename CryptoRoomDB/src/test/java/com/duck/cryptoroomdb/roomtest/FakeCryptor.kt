package com.duck.cryptoroomdb.roomtest

import com.duck.cryptoroomdb.interfaces.Decryptor
import com.duck.cryptoroomdb.interfaces.Encryptor

/**
 * Test-only reversible "cipher". Not real encryption — it just has to be a deterministic,
 * invertible transform so tests can assert that:
 *  * the value stored in the DB is **not** the plain text ([encrypt] changes it), and
 *  * reading it back yields the original ([decrypt] undoes [encrypt]).
 *
 * Implemented as a string reversal with a marker prefix so the "encrypted" form is
 * obviously different from the input when inspected in a failing assertion.
 */
class FakeCryptor : Encryptor, Decryptor {

    override fun encrypt(plainValue: String): String = PREFIX + plainValue.reversed()

    override fun decrypt(encryptedValue: String): String =
        encryptedValue.removePrefix(PREFIX).reversed()

    companion object {
        const val PREFIX = "enc:"
    }
}
