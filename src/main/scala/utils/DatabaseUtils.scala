package utils

import types.{ErgoName, ErgoNameHash, RegistrationInfo}

import java.sql.{Connection, DriverManager, ResultSet}
import org.ergoplatform.appkit.ErgoId

object DatabaseUtils {

    val databasePath: String = "jdbc:postgresql://localhost:5432/ergonames?user=ergonames&password=ergonames"

    def readRegistryInsertion(mintTransactionId: String): RegistrationInfo = {
        val connection = DriverManager.getConnection(databasePath)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(s"SELECT * FROM registry WHERE mint_transaction_id = '$mintTransactionId'")
        if (resultSet.next()) {
            val registrationInfo = RegistrationInfo(
                resultSet.getString("mint_transaction_id"),
                resultSet.getString("spent_transaction_id"),
                ErgoNameHash(resultSet.getBytes("ergoname_registered")),
                ErgoId.create(resultSet.getBytes("ergoname_token_id"))
            )
            connection.close()
            registrationInfo
        } else {
            connection.close()
            null
        }
    }

}