package utils

import org.ergoplatform.explorer.client.{DefaultApi}
import org.ergoplatform.ErgoBox
import org.ergoplatform.restapi.client.{Asset, ErgoTransactionOutput, Registers}
import org.ergoplatform.explorer.client.model.OutputInfo
import org.ergoplatform.appkit.impl.ScalaBridge
import java.util
import scala.collection.JavaConverters._
import org.ergoplatform.appkit.InputBox
import org.ergoplatform.restapi.client.ErgoTransactionInput
import org.ergoplatform.Input

object ExplorerUtils {

    def getBoxById(boxId: String, explorerClient: DefaultApi): ErgoBox = {
        val box = explorerClient.getApiV1BoxesP1(boxId).execute().body()
        val tokens = new util.ArrayList[Asset](box.getAssets.size())
        for (asset <- box.getAssets().asScala) {
            tokens.add(new Asset().tokenId(asset.getTokenId()).amount(asset.getAmount()))
        }
        val registers = new Registers
        for (registerEntry <- box.getAdditionalRegisters.entrySet.asScala) {
            registers.put(registerEntry.getKey, registerEntry.getValue.serializedValue)
        }
        val boxConversion: ErgoTransactionOutput = new ErgoTransactionOutput()
            .transactionId(box.getTransactionId)
            .boxId(box.getBoxId())
            .value(box.getValue)
            .ergoTree(box.getErgoTree)
            .creationHeight(box.getCreationHeight)
            .additionalRegisters(registers)
            .assets(tokens)
            .index(box.getIndex)
        ScalaBridge.isoErgoTransactionOutput.to(boxConversion)
    }

    def getOutputOneBoxIdFromTransactionId(transactionId: String, explorerClient: DefaultApi): String = {
        val transaction = explorerClient.getApiV1TransactionsP1(transactionId).execute().body()
        val outputZeroBoxId = transaction.getOutputs().get(1).getBoxId()
        outputZeroBoxId
    }
}