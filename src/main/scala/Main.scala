import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.explorer.client.{ExplorerApiClient, DefaultApi}

import utils.ExplorerUtils.{getBoxById, getOutputOneBoxIdFromTransactionId}
import utils.DatabaseUtils.getMostRecentMintTransactionId
import org.ergoplatform.ErgoBox
import org.bitbucket.inkytonik.kiama.parsing.Input
import special.collection.Coll
import sigmastate.serialization.ErgoTreeSerializer
import io.getblok.getblok_plasma.collections.PlasmaMap
import types.ErgoNameHash
import utils.RegistrySync.syncRegistry
import types.ErgoName
import io.getblok.getblok_plasma.collections.ProvenResult
import io.getblok.getblok_plasma.collections.OpResult
import io.getblok.getblok_plasma.collections.Proof

object Main {

  val explorerApiUrl = "https://api-testnet.ergoplatform.com"
  val initialTransactionId = "e271e7cb9b9c7932546e8a5746c91cb1c0f1114ff173a90e1fe979170f71c579"
  val liveMode = false

  val registryContractAddressRaw = "24fZfBxxFZYG6ErBfvit6VhzwagencNc2mVwnHugTA5yA78tLvfTPsEz8AucKAEwJarwT9N1mtfxLnWD8WchdQdxr3su3MszYXSniUpUf3T13rXRBRTdx4qUimoGJhGPYASjrqaR4V6SnkpkkHkp62onP5fPbNfxdQbuo3uCVmmBQd6Xt3t8NEEVHFqNrCVT1pp4q"

  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      println("No arguments provided.")
      return
    }
    val proxyBoxIdRaw = args(0)

    val ergoToolConfig = ErgoToolConfig.load("config.json")
    val ergoNodeConfig = ergoToolConfig.getNode()

    val ergoClient = RestApiErgoClient.create(ergoNodeConfig, explorerApiUrl)
    val explorerClient = new ExplorerApiClient(explorerApiUrl).createService(classOf[DefaultApi])

    val lastInsertionTransactionId = getMostRecentMintTransactionId()
    val contractBoxToSpendId = getOutputOneBoxIdFromTransactionId(lastInsertionTransactionId, explorerClient)

    val transactionInfo = ergoClient.execute((ctx: BlockchainContext) => {
      val prover = ctx.newProverBuilder()
        .withMnemonic(
          SecretString.create(ergoNodeConfig.getWallet.getMnemonic),
          SecretString.create(ergoNodeConfig.getWallet.getPassword)
        )
        .withEip3Secret(0)
        .build()
      val senderAddress = prover.getEip3Addresses().get(0)
      val registryContractAddress = Address.create(registryContractAddressRaw)

      val contractBox = ctx.getBoxesById(contractBoxToSpendId)(0)
      val proxyBox = ctx.getBoxesById(proxyBoxIdRaw)(0)

      val contractBoxRegisters = contractBox.getRegisters()
      val registry = contractBoxRegisters.get(0)

      val proxyBoxRegisters = proxyBox.getRegisters()
      val ergoNameRaw = proxyBoxRegisters.get(0)
      val receiverAddressRaw = proxyBoxRegisters.get(1)

      val ergoNameToRegister = new String(ergoNameRaw.getValue.asInstanceOf[Coll[Byte]].toArray)
      val receiverAddressBytesRaw = receiverAddressRaw.getValue.asInstanceOf[Coll[Byte]].toArray
      val recieverAddressErgoTree = ErgoTreeSerializer.DefaultSerializer.deserializeErgoTree(receiverAddressBytesRaw)
      val receiverAddress = Address.fromErgoTree(recieverAddressErgoTree, ctx.getNetworkType())

      val tokenMap: PlasmaMap[ErgoNameHash, ErgoId] = syncRegistry(initialTransactionId)
      val ergoname: ErgoNameHash = ErgoName(ergoNameToRegister).toErgoNameHash
      val tokenId = contractBox.getId()
      val ergonameData: Seq[(ErgoNameHash, ErgoId)] = Seq(ergoname -> tokenId)
      val result: ProvenResult[ErgoId] = tokenMap.insert(ergonameData: _*)
      val opResults: Seq[OpResult[ErgoId]] = result.response
      val proof: Proof = result.proof

      val contractBoxWithContextVars = contractBox.withContextVars(
        ContextVar.of(0.toByte, ErgoValue.of(ergoname.hashedName)),
        ContextVar.of(1.toByte, proof.ergoValue),
        ContextVar.of(2.toByte, ergoNameToRegister.getBytes())
      )

      val boxesToSpend: java.util.List[InputBox] = new java.util.ArrayList[InputBox]()
      boxesToSpend.add(contractBoxWithContextVars)
      boxesToSpend.add(proxyBox)

      val tokenToMint = new Eip4Token(tokenId.toString(), 1L, ergoNameToRegister, "Test ErgoName Token", 0)

      val mintOutputBox = ctx.newTxBuilder().outBoxBuilder()
        .value(Parameters.MinChangeValue)
        .contract(receiverAddress.toErgoContract())
        .mintToken(tokenToMint)
        .build()

      val registryOutputBox = ctx.newTxBuilder().outBoxBuilder()
        .value(Parameters.MinChangeValue)
        .contract(registryContractAddress.toErgoContract())
        .registers(
          tokenMap.ergoValue
        )
        .build()

      val tx = ctx.newTxBuilder()
        .boxesToSpend(boxesToSpend)
        .outputs(mintOutputBox, registryOutputBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(senderAddress.getErgoAddress())
        .build()
      
      val signedTx = prover.sign(tx)
      val txJson = signedTx.toJson(true)
      if (liveMode) {
        ctx.sendTransaction(signedTx)
      }
      txJson
    })
    println(transactionInfo)
  }
}