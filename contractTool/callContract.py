from web3 import Web3, HTTPProvider
import sys
import datetime
import os
from dotenv import load_dotenv



true = True
false = False
config = {
    "abi": [
        {
            "inputs": [
                {
                    "internalType": "address",
                    "name": "newServer",
                    "type": "address"
                }
            ],
            "name": "addWhiteList",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "DonateGasFee",
            "outputs": [],
            "stateMutability": "payable",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "string",
                    "name": "ipfsHash",
                    "type": "string"
                },
                {
                    "internalType": "string",
                    "name": "pckgName",
                    "type": "string"
                },
                {
                    "internalType": "string",
                    "name": "version",
                    "type": "string"
                },
                {
                    "internalType": "string",
                    "name": "url",
                    "type": "string"
                },
                {
                    "internalType": "string",
                    "name": "cert_ID",
                    "type": "string"
                },
                {
                    "internalType": "string",
                    "name": "repkg_status",
                    "type": "string"
                }
            ],
            "name": "store_estimate",
            "outputs": [],
            "stateMutability": "payable",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "string",
                    "name": "ipfsHash",
                    "type": "string"
                },
                {
                    "internalType": "string",
                    "name": "pckgName",
                    "type": "string"
                },
                {
                    "internalType": "string",
                    "name": "version",
                    "type": "string"
                },
                {
                    "internalType": "string",
                    "name": "url",
                    "type": "string"
                },
                {
                    "internalType": "string",
                    "name": "cert_ID",
                    "type": "string"
                },
                {
                    "internalType": "string",
                    "name": "repkg_status",
                    "type": "string"
                }
            ],
            "name": "storeNewApp",
            "outputs": [],
            "stateMutability": "payable",
            "type": "function"
        },
        {
            "inputs": [],
            "stateMutability": "payable",
            "type": "constructor"
        },
        {
            "anonymous": false,
            "inputs": [
                {
                    "indexed": false,
                    "internalType": "string",
                    "name": "ipfsHash",
                    "type": "string"
                },
                {
                    "indexed": false,
                    "internalType": "string",
                    "name": "packageName",
                    "type": "string"
                },
                {
                    "indexed": false,
                    "internalType": "string",
                    "name": "version",
                    "type": "string"
                },
                {
                    "indexed": false,
                    "internalType": "string",
                    "name": "url",
                    "type": "string"
                },
                {
                    "indexed": false,
                    "internalType": "string",
                    "name": "cert_ID",
                    "type": "string"
                },
                {
                    "indexed": false,
                    "internalType": "string",
                    "name": "repkg_status",
                    "type": "string"
                }
            ],
            "name": "store_app",
            "type": "event"
        },
        {
            "inputs": [],
            "name": "withdraw",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "getAllPkgName",
            "outputs": [
                {
                    "internalType": "string[]",
                    "name": "",
                    "type": "string[]"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        }
    ],
    "address": "0xcf5235A05f3E2Ec7d65fA5c03550D77c27C20FE5"
}

INFURA_api = "https://rinkeby.infura.io/v3/6b68a5fb6c284b6aba5ba51137fea6b4"

web3 = Web3(HTTPProvider(INFURA_api))
contract_instance = web3.eth.contract(address=config['address'],abi=config['abi'])

def storeApp(txn,txn_hash,gas_fee, priv_key):
    try:
        txn_re = web3.eth.getTransaction(txn_hash)
        to_address = txn_re['to']
        if (to_address == '0xcf5235A05f3E2Ec7d65fA5c03550D77c27C20FE5'):   
            if (txn_re['value'] > gas_fee):  
                signed_txn = web3.eth.account.signTransaction(txn, private_key=priv_key)
                res = web3.eth.sendRawTransaction(signed_txn.rawTransaction).hex()
                txn_receipt = web3.eth.waitForTransactionReceipt(res)
                return txn_receipt
            else:
                print("NoEnoughFee")
                return False
        else:
            print('WrongAddress')
            return False
    except:
        print('TransactionNotFound')
        return False

    
    


if __name__ == '__main__':
    a = []
    for i in range(1, len(sys.argv)):
        a.append((sys.argv[i]))
    ipfs_hash = a[0]
    pkg_name = a[1]
    version = a[2]
    url = a[3]
    cert_ID = a[4]
    rpkg_status = a[5]
    txn_hash = a[6]
    load_dotenv()

    my_address = os.getenv('MY_ADDRESS')
    priv_key = os.getenv('PRIVATE_KEY')
    try:
        gas_fee = contract_instance.functions.store_estimate(ipfs_hash, \
            pkg_name, version, url, \
            cert_ID, rpkg_status
        ).estimateGas()
        txn = contract_instance.functions.storeNewApp(ipfs_hash, \
            pkg_name, version, url, \
            cert_ID, rpkg_status
        ).buildTransaction(
            {
                'chainId': 4,
                'nonce':web3.eth.getTransactionCount(my_address),
                'gas':7600000,
                'value':Web3.toWei(0,'ether'),
                'gasPrice':web3.eth.gasPrice,
            }
        )
        print(storeApp(txn,txn_hash,gas_fee, priv_key))
    except:
        print("DuplicateApk")
