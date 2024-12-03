pragma solidity >=0.4.22;
pragma experimental ABIEncoderV2;

contract agchain {
 // new version 
 address private owner;
 string[] pkgNames;
 mapping(address => bool) public whitelisted;
 event store_app(
     string ipfsHash, 
     string packageName, 
     string version,
     string url,
     string cert_ID,
     string repkg_status
    );
 
 
 constructor() {
     owner = msg.sender;
     whitelisted[msg.sender] = true;
 }
 
 modifier onlyOwner(){
     require(msg.sender==owner,"Only the owner can withdraw the balance.");
     _;
 }
 
 modifier onlyWhiteList(){
    require(whitelisted[msg.sender], "You are not in the white list");
    _;
 }

//if this is an entirly new app 
function storeNewApp(string memory ipfsHash, string memory pckgName, string memory version, string memory url, string memory cert_ID, string memory repkg_status) public onlyWhiteList()
 {
   pkgNames.push(pckgName);
   emit store_app(ipfsHash,pckgName,version,url,cert_ID,repkg_status);
 }
 
 function addWhiteList(address newServer) public onlyOwner(){
     whitelisted[newServer]= true;
 }
 
 function getAllPkgName() public view returns(string[] memory){
     return pkgNames;
 }
 
 function withdraw() public onlyOwner(){
     uint contract_balance = address(this).balance;
     payable(msg.sender).transfer(contract_balance);
 }
 
}