import React from 'react';
import {HashRouter, Route, Switch} from 'react-router-dom';

import App from './App';
import showApps from './showApps';


const BasicRoute = () => (
    <HashRouter>
        <Switch>
            <Route exact path="/" component={App}/>
            <Route exact path="/showApps" component={showApps}/>
        </Switch>
    </HashRouter>
);


export default BasicRoute;