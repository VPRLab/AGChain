import React, { Component } from 'react';
import { Form, Button, Table} from "react-bootstrap";
import { Container} from 'reactstrap';
import { Route,Link, NavLink } from 'react-router-dom'
import './App.css';
import web3 from './web3';
import contract from './contract';
import Ping from 'ping.js';
import axios from 'axios'; 


const headers= ['Package Name', 'Version','Certificate Serial Num.','Original URL', 'Download',"Repackage Check"]

class showApps extends React.Component{

    state = {
        apps: []
    }

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
            var ipfsHash = metadata['ipfsHash']
            var pkgName = metadata['packageName']
            var version = metadata['version']
            var url = metadata['url']
            var rpkg = metadata['repkg_status']
            var cert_ID = metadata['cert_ID']
            console.log(cert_ID)
            var apk = []
            apk.push(pkgName)
            apk.push(version)
            apk.push(cert_ID)
            apk.push(url)
            apk.push(ipfsHash)
            apk.push(rpkg)
            app.push(apk)
          }
          object.setState({
            apps: app
          })
        });
        console.log(this.state.apps);
    }

    IPFSDown = async (event,pkgName,ipfsHash) => {
        console.log("pkge name: "+ pkgName)
        console.log('开始下载');
        console.log("IPFS hash: " + ipfsHash);
        const IPFS = require('ipfs-core')

        const ipfs = await IPFS.create()
        const cid = ipfsHash;

        for await (const file of ipfs.get(cid)) {
            console.log(file.type, file.path)

            if (!file.content) {
                console.log("Fail");
            }

            const content = []

            for await (const chunk of file.content) {
                content.push(chunk)
            }
            const blob = new Blob(content);
            console.log("blob: " + blob)
            var elementA = document.createElement('a');
            elementA.download = pkgName+".apk";
            elementA.style.display = 'none';
            elementA.href = URL.createObjectURL(blob);
            document.body.appendChild(elementA);
            elementA.click();
            document.body.removeChild(elementA);
        }
    }

    render() {
        return (
            <div className = "App">
                <div className = 'header' style={{width: '100%'}}>
                    <div style={{width: '75%', float:'left'}}>
                        <h2> <b>APKChain</b></h2> 
                    </div>
                    <div style={{width: '25%', float:'left', marginTop: '10px'}}>
                        <a href='/'><Button className= "btn" >Home Page</Button></a>
                    </div>
                    <hr style= {{borderWidth: '3px',  width: '100%'}}></hr>
                    <h2> <b>App Exploration</b></h2>
                    <p> <b>You need to have a metamask account to explore the App information</b></p>
                </div>

                <Container>
                    <div style={{maxWidth:"100%"}}>
                        <table  border="1" className="table">
                            <thead>
                                <tr style={{textAlign: "center"}}>
                                    {
                                        headers.map((head,index)=>
                                    <th key={index}>{head}</th>)
                                    }
                                </tr>
                            </thead>
                            <tbody>
                                {this.state.apps.map((row,index)=>{
                                    return(<tr key={index}>
                                        {
                                            row.map((cell,index)=>{
                                                if (index != 4 && index !=3 ){    
                                                    if (cell == "Fail"){
                                                        return <td style={{color:'red'}}>{cell}</td>
                                                    }else if(cell == "Pass"){
                                                        return <td style={{color:'green'}}>{cell}</td>
                                                    }else {
                                                        return <td>{cell}</td>
                                                    }
                                                }
                                                if (index ==4){
                                                    return <td><button style={{marginLeft: '2%', fontSize: '12px'}} onClick={(ev) => {this.IPFSDown(ev,row[0],cell)}}>Download</button></td>
                                                }
                                                if (index == 3){
                                                    return <td style={{width:'20%', height: '50px'}}>{cell}</td>
                                                }                                         
                                            })                                                         
                                        }
                                    </tr>)
                                })}
                            </tbody>
                        </table>
                    </div>
                </Container>
            </div>
        );
    }
}
export default showApps;

// row[0],cell