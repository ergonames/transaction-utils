package types

import io.getblok.getblok_plasma.ByteConversion

case class ErgoNameHash(hashedName: Array[Byte])

object ErgoNameHash {

    implicit val nameConversion: ByteConversion[ErgoNameHash] = new ByteConversion[ErgoNameHash] {
        override def convertToBytes(t: ErgoNameHash): Array[Byte] = t.hashedName
        override def convertFromBytes(bytes: Array[Byte]): ErgoNameHash = ErgoNameHash(bytes)
    }
}