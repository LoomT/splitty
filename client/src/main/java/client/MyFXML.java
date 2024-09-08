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

import com.google.inject.Injector;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static client.utils.CommonFunctions.getHighContrastEffect;

public class MyFXML {

    private final Injector injector;

    /**
     * @param injector injector of the app
     */
    public MyFXML(Injector injector) {
        this.injector = injector;
    }

    /**
     * Constructs a scene and controller for it
     *
     * @param c unused obj
     * @param resources the resources for the module
     * @param isHighContrast for enabling high contrast
     * @param parts path parts
     * @param <T> class
     * @return fxml controller and scene pair
     */
    public <T> Pair<T, Parent> load(Class<T> c, ResourceBundle resources,
                                    boolean isHighContrast, String... parts) {
        try {
            var loader = new FXMLLoader(getLocation(parts), resources, null,
                    new MyFactory(), StandardCharsets.UTF_8);
            Parent parent = loader.load();

            if (isHighContrast) parent.setEffect(getHighContrastEffect());

            T ctrl = loader.getController();
            return new Pair<>(ctrl, parent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Concatenates the parts into a relative path and converts it into a URL
     *
     * @param parts relative path parts
     * @return location of resource
     */
    private URL getLocation(String... parts) {
        var path = Path.of("", parts).toString();
        URL url = MyFXML.class.getClassLoader().getResource(path);
        if(url != null)
            return url;
        URL url2;
        try {
            url2 = new File(path).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Can't find the FXML file: " + path, e);
        }
        return url2;
    }

    /**
     * Returns a controller for a given type I think
     */
    private class MyFactory implements BuilderFactory, Callback<Class<?>, Object> {

        @Override
        @SuppressWarnings("rawtypes")
        public Builder<?> getBuilder(Class<?> type) {
            return new Builder() {
                @Override
                public Object build() {
                    return injector.getInstance(type);
                }
            };
        }

        /**
         * ¯\_(ツ)_/¯
         *
         * @param type ¯\_(ツ)_/¯
         * @return some instance
         */
        @Override
        public Object call(Class<?> type) {
            return injector.getInstance(type);
        }
    }
}