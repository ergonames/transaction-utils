package utils

import types.{ErgoName, ErgoNameHash}

import io.getblok.getblok_plasma.{PlasmaParameters, ByteConversion}


object RegistrySync {

    def syncEmptyRegistry(): PlasmaMap[ErgoNameHash, ErgoId] = {
        val registry = new PlasmaMap[ErgoNameHash, ErgoId](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)
        registry
    }

    def syncRegistry(initialTransactionId: String): PlasmaMap[ErgoNameHash, ErgoId] = {
        val registry = syncEmptyRegistry()
        val firstSpendTransactionId = ""
        if (firstSpendTransactionId == null) {
            registry
        }
        registry
    }

}