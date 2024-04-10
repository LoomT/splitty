/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client;

import client.scenes.*;
import client.MockClass.MainCtrlInterface;
import client.utils.*;
import client.utils.currency.CurrencyConverter;
import client.utils.currency.FileManager;
import client.utils.currency.FileManagerImpl;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

public class MyModule implements Module {

    /**
     * Binds classes to scopes and/or other classes or instances for injection
     *
     * @param binder guice binder
     */
    @Override
    public void configure(Binder binder) {
        binder.bind(MainCtrlInterface.class).to(MainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(StartScreenCtrl.class).in(Scopes.SINGLETON);
        binder.bind(AddCustomTransactionCtrl.class).in(Scopes.SINGLETON);
        binder.bind(AddExpenseCtrl.class).in(Scopes.SINGLETON);
        binder.bind(AdminLoginCtrl.class).in(Scopes.SINGLETON);
        binder.bind(EditParticipantsCtrl.class).in(Scopes.SINGLETON);
        binder.bind(EditTitleCtrl.class).in(Scopes.SINGLETON);
        binder.bind(EventPageCtrl.class).in(Scopes.SINGLETON);
        binder.bind(UserConfig.class).in(Scopes.SINGLETON);
        binder.bind(ServerUtils.class).to(ServerUtilsImpl.class).in(Scopes.SINGLETON);
        binder.bind(LanguageConf.class).in(Scopes.SINGLETON);
        binder.bind(IOInterface.class).toInstance(new FileIO(UserConfig.class.getClassLoader()
                .getResource("client/config.properties")));
        binder.bind(Websocket.class).to(WebsocketImpl.class).in(Scopes.SINGLETON);
        binder.bind(FileManager.class).to(FileManagerImpl.class).in(Scopes.SINGLETON);
        binder.bind(CurrencyConverter.class).in(Scopes.SINGLETON);
    }
}