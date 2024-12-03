import './App.css';
import React, { Component } from 'react';
import { Button, Table} from "react-bootstrap";
import { Container} from 'reactstrap';
import Ping from 'ping.js';
import axios from 'axios';
import contract from './contract';
import web3 from './web3';

class App extends Component {

  state = {
    ipfsHash:null,
    getIPFShash: '',
    buffer:'',
    ethAddress:'',
    filePath: '',
    url: '',
    version: 0,
    metaAccounts: null,
    txReceipt: '',
    packageName: '',
    certificates: [],
    ping_res: 0,
    apps: [],
    returnedVersion: ''  
  };

  constructor(props){
    super(props);
    this.p = new Ping();
  }

  async componentDidMount(){
    var object = this;
    await contract.getPastEvents('store_app',{
      filter:{},
      fromBlock: 0
    },function(error,event){
      var app = []
      for (let i = 0; i < event.length; i++) {
        var metadata = event[i]["returnValues"]
        app.push(metadata)
      }
      object.setState({
        apps: app
      })
    });
    console.log(this.state.apps);
  }

  handleGetAppName = (event) => {
    this.setState({
      packageName : event.target.value,
    })
  };

  handleGetUrl = (event) => {
    this.setState({
      url : event.target.value,
    })
  };


  handleGetVersion = (event) => {
    this.setState({
      version : event.target.value,
    })
  };

  convertToBuffer = async(reader) =>{
    //file is converted to a buffer for upload to IPFS
    const buffer = await Buffer.from(reader.result);
    //set this buffer -using es6 syntax
    this.setState({buffer});
  };

  Download = async(event) => {
    event.preventDefault();
    var EncodedURL = encodeURIComponent(this.state.url);
    console.log(EncodedURL)
    var price = await web3.eth.getGasPrice();
    var price_int = parseInt(price);

    const host = "https://www.agchain.ltd:8443/agchain/DownloadServlet?url="
    const host2 = "https://www.agchain.ltd:8443/agchain/callContractServlet?txn="
    const ethAddress= await contract.options.address;
    this.setState({ethAddress})
    try{
      const accounts = await web3.eth.getAccounts();
      this.setState({metaAccounts:accounts})
      if (accounts.length==0){
        window.alert("Please allow our site to access your accounts. (You can set this in Metamask -> Setting -> Connection) ");
        return;
      }
      console.log('Sending from Metamask account: ' + accounts[0]);
    }catch(error){
      console.log(error);
      window.alert("You haven't configured the crypto wallet app of Ethereum, Please get the Metamask add-on first and sign up/log in the MetaMask account.");
      return;
    }


    var test_IPFS = "QmVAtuhLT5P3ZJjfndousDHFHDnqUGuvTmwxRJJWZVCKNHwLPhssd";
    var test_pkg_name = "com.ss.android.ugc.aweme.lite";
    var test_version = "10.2.0";
    var test_url = "https://shouji.baidu.com/software/26993492.html";
    var test_cert_ID = "1325320554";
    var test_repkg_check = "Pass";

    var object_1 = this;
    window.alert("Please do not click the \"Upload APK\" button again utill this task is finished. During the progress, there will be a pop-up window that requires you to pay the transaction fee. Please click confirm.");
    var final_url_1 = host + EncodedURL;
    axios.get(final_url_1)
    .then(response => {
      var content = response.data;
      console.log("Response data: " + content);
      var resu = content.params.Result;
      object_1.setState({filePath: content.params.FilePath});
      if(resu == "success"){
        var md5_check = content.params.Md5;
        var duplicate = content.params.duplicate;
        if(md5_check == "checked" || md5_check == "NoReference"){
          if(duplicate=="false"){
            if (md5_check == "NoReference"){
              window.alert("We didn't get any offical md5 value of this apk, there exists potential risk.")
            }
            var gasResult = contract.methods.store_estimate(test_IPFS,test_pkg_name,test_version,test_url,test_cert_ID,test_repkg_check).estimateGas({
              from:  object_1.state.metaAccounts[0] 
            });
            gasResult.then(function(result){
              console.log("The estimate gas is: " + result);
              var fee = price_int * result;

              contract.methods.DonateGasFee().send({
                from: object_1.state.metaAccounts[0],
                value: 	fee
              },(error,transactionHash) =>{
                console.log(transactionHash);
                var final_url_2 = host2 + transactionHash + "&path="+object_1.state.filePath +"&url="+EncodedURL;
                console.log("final_url_2 is: "+ final_url_2);

                axios.get(final_url_2)
                .then(resp => {
                  var cont = resp.data;
                  var call_resu = cont.params.call_result;
                  var resu = cont.params.Result;
                  if(resu == "Success"){
                    object_1.setState({ipfsHash: cont.params.Hash}); // "QmNgeMTLCP9hzzMFctPiFiYwWQQkKjxV4ngcb2AcWXrAgF"
                    object_1.setState({packageName: cont.params.package_name}); //  "vmovier.com.activity"
                    object_1.setState({returnedVersion: cont.params.version_number});
                    if(call_resu=="success"){
                      window.alert("APK Uploaded Successfully");
                    }else if(call_resu=="NoEnoughFee"){
                      window.alert("Your Transaction fee is not enough.");
                    }else if(call_resu=="WrongAddress" || call_resu=="TransactionNotFound"){
                      window.alert("You didn't pay transaction fee to our account.");
                    }
                  }else{
                    window.alert("The transaction ID you send has been used before.");
                  }
                }).catch(error => console.log(error)) // axios second fetch
              })//DonateGas
            }); //estimateGas


          }else if (duplicate=="true"){
            window.alert("This APK has been uploaded before, please find it in the Explore Page or download it directly by entering the package name and version");
          }

        }else if(md5_check == "Failed"){
          window.alert("The hash value of downloaded APK file doesn't match the official hash value got from the given app market.\
                        We will not upload this file since this APK file has been tampered.")
        } //MD5 check 
      }else{
        window.alert("Resource Fetch Failed.");
      }


    }).catch(error => console.log(error)) // axios first fetch

  }

  IPFSDownlaod = async (event) => {
    console.log('开始下载');
    var pkgName = this.state.packageName;
    var ver = this.state.version;
    var apks = this.state.apps;
    for (let i = 0; i < apks.length; i++) {
      if(apks[i]["packageName"] == pkgName && apks[i]["version"] == ver){
        window.alert("We have this apk" + apks[i]["ipfsHash"]);
        var gateway = ["https://ipfs.io","https://hashnews.k1ic.com","https://ipfs.jeroendeneef.com","https://ipfs.jbb.one"];
        var final_gateway = "";
        var minimum = 1000;
        var pings = [0,0,0,0];
        await this.p.ping("https://ipfs.io", (err, data) => { 
            pings[0] = data/1000;
        });
        await this.p.ping("https://hashnews.k1ic.com", (err, data) => { 
            pings[1] = data/1000;
        });
        await this.p.ping("https://ipfs.jeroendeneef.com", (err, data) => { 
            pings[2] = data/1000;
        });
        await this.p.ping("https://ipfs.jbb.one", (err, data) => { 
            pings[3] = data/1000;
        });

        setTimeout(function(){
          for(var i =0; i<pings.length;i++){
            if(pings[i]<minimum){
              minimum = pings[i];
              final_gateway = gateway[i];
            }
          }
          console.log("final gateway: " + final_gateway);
          console.log("ping response time: " + minimum);
          var final_url = final_gateway+"/ipfs/"+this.state.getIPFShash;
          console.log(final_url);
          axios.get(final_url)
          .then(response=>{
              var content = response.data;
              const blob = new Blob([content]);
              console.log(blob)
              var elementA = document.createElement('a');
              elementA.download = pkgName+ ".apk";
              elementA.style.display = 'none';
              elementA.href = URL.createObjectURL(blob);
              document.body.appendChild(elementA);
              elementA.click();
              document.body.removeChild(elementA);
          })
        },2000);
      }else {
        window.alert("We don't have this apk" + pkgName);
      }
    }
     
  };



  render() {
    return(
      <div className= "App">
        <header className="App-header">
          <h1> AGChain</h1>
          <h2> A Blockchain Based Gateway For App Download Delegation</h2>
          <a href='/#/showApps'><Button bsStyle= 'primary' style={{marginTop: "50px", width: "100%"}}>Explore App</Button></a>
        </header>
        <Container>
          <h2 style={{marginTop: "50px"}}><b> Upload App with the Original Market URL </b></h2>
          <br></br>
          <label for="URL">URL</label>
          <input
            type = 'text'
            id = 'URL'
            placeholder = {'Please enter App url'}
            value={this.state.url}
            onChange={this.handleGetUrl}
            style={{ marginRight: '3%', 
            marginLeft: '1%',
            borderRadius: 5, 
            borderWidth: 1, 
            padding: 5,
            borderStyle: 'solid',
            borderColor: '#797D7F'}}
          />
          <br></br>
          <Button 
          onClick = {this.Download}
          style={{
            marginTop: '1%',
          }}
          > 
          Upload APK
          </Button>

          <hr style= {{borderWidth: '1px', color: 'gray', width: '100%', borderStyle: 'dotted'}}></hr>
          <h2><b> Secure Download App from AGChain's IPFS Storage </b></h2>
          <br></br>
          <label for="AppName">Package Name</label>
          <input
              type = 'text'
              id = 'AppName'
              placeholder = {'Please enter package name'}
              value = {this.state.packageName}
              onChange={this.handleGetAppName}
              style={{ marginRight: '3%', 
              marginLeft: '1%',
              borderRadius: 5, 
              borderWidth: 1, 
              width: '16%',
              padding: 5,
              borderStyle: 'solid',
              borderColor: '#797D7F'}}
            />
          <label for="version">Version</label>
            <input
              type = 'text'
              id = 'version'
              value = {this.state.version}
              onChange={this.handleGetVersion}
              style={{ marginRight: '3%',
              width: '5%', 
              marginLeft:'1%',
              borderRadius: 5, 
              borderWidth: 1, 
              padding: 5,
              borderStyle: 'solid',
              borderColor: '#797D7F'}}
            />
          <br></br>
          <br></br>
          <Button 
            onClick = {this.IPFSDownlaod}
            > 
          Download APK
          </Button>
          <hr style= {{borderWidth: '1px', color: 'gray', width: '100%', borderStyle: 'dotted'}}></hr>

          <Table bordered responsive>
            <thead>
              <tr>
                <th>APK Storage Receipt Category</th>
                <th>Values</th>
              </tr>
            </thead>
            
            <tbody>
              <tr>
                <td>IPFS Hash # stored on Eth Contract</td>
                <td>{this.state.ipfsHash}</td>
              </tr>
              <tr>
                <td>Package Name</td>
                <td>{this.state.packageName}</td>
              </tr>
              <tr>
                <td>APK Version</td>
                <td>{this.state.returnedVersion}</td>
              </tr>
                  
              </tbody>
          </Table>
        </Container>
      </div>
    );
  }
}
export default App;

