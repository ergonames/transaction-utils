package types

import org.ergoplatform.appkit.ErgoId

case class RegistrationInfo(
    mintTransactionId: String,
    spentTransactionId: String,
    ergoNameRegistered: ErgoNameHash,
    ergoNameTokenId: ErgoId
)