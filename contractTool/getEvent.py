from web3 import Web3, HTTPProvider
import sys
import ipfshttpclient

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

def getEvents():
    event_filter = contract_instance.events.store_app().createFilter(fromBlock=0)
    events = event_filter.get_all_entries()
    ipfs_hashes = []
    for event in events:
        args = event['args']
        ipfs_hash = args['ipfsHash']
        ipfs_hashes.append(ipfs_hash)
    return ipfs_hashes

if  __name__ == '__main__':
    hashes = getEvents()
    client = ipfshttpclient.connect()
    for ipfs in hashes:
        res = client.cat(ipfs)
        file_path = ipfs + ".apk"
        with open(file_path,"wb+") as f:
            f.write(res)
    client.pin.ls(type='all')
