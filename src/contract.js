import web3 from './web3';
//access our local copy to contract deployed on rinkeby testnet
//use your own contract address
const address = '0x50B4223Af3c893E83DC6Ad5dF0332E84DC74117A';  //   0xBa5dfF52678f3542707862e96bBa6CB6F34A8e11
//use the ABI from your contract
const abi = [
	{
		"inputs": [],
		"stateMutability": "nonpayable",
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
		"stateMutability": "nonpayable",
		"type": "function"
	},
	{
		"inputs": [
			{
				"internalType": "address",
				"name": "",
				"type": "address"
			}
		],
		"name": "whitelisted",
		"outputs": [
			{
				"internalType": "bool",
				"name": "",
				"type": "bool"
			}
		],
		"stateMutability": "view",
		"type": "function"
	},
	{
		"inputs": [],
		"name": "withdraw",
		"outputs": [],
		"stateMutability": "nonpayable",
		"type": "function"
	}
]
export default new web3.eth.Contract(abi, address);
